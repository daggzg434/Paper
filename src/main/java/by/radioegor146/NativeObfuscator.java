package by.radioegor146;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.jar.Attributes.Name;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.LookupSwitchInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.MultiANewArrayInsnNode;
import org.objectweb.asm.tree.TableSwitchInsnNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import ru.gravit.launchserver.asm.ClassMetadataReader;
import ru.gravit.launchserver.asm.SafeClassWriter;

public class NativeObfuscator {
   private static final Pattern PATTERN = Pattern.compile("([^a-zA-Z_0-9])");
   private static final Map<Integer, String> INSTRUCTIONS = new HashMap();
   private static final Properties CPP_SNIPPETS = new Properties();
   private static final String[] CPP_TYPES = new String[]{"void", "jboolean", "jchar", "jbyte", "jshort", "jint", "jfloat", "jlong", "jdouble", "jarray", "jobject", "jobject"};
   private static final String[] JAVA_DESCRIPTORS = new String[]{"V", "Z", "C", "B", "S", "I", "F", "J", "D", "Ljava/lang/Object;", "Ljava/lang/Object;", "Ljava/lang/Object;"};
   private static final int[] TYPE_TO_STACK = new int[]{1, 1, 1, 1, 1, 1, 1, 2, 2, 0, 0, 0};
   private static final int[] STACK_TO_STACK = new int[]{1, 1, 1, 2, 2, 0, 0, 0, 0};
   private final HashMap<String, Integer> stringPool = new HashMap();
   private final HashMap<String, Integer> cachedClasses = new HashMap();
   private final HashMap<CachedMethodInfo, Integer> cachedMethods = new HashMap();
   private final HashMap<CachedFieldInfo, Integer> cachedFields = new HashMap();
   private StringBuilder ifaceStaticNativeMethodsSb = new StringBuilder();
   private StringBuilder nativeMethodsSb = new StringBuilder();
   private Map<String, InvokeDynamicInsnNode> invokeDynamics = new HashMap();
   private int currentLength = 0;
   private final List<ClassNode> readyIfaceStaticClasses = new ArrayList();
   private ClassNode currentIfaceStaticClass;
   private int currentClassId;
   private int nativeDirId = 0;

   private String escapeCppNameString(String value) {
      Matcher m = PATTERN.matcher(value);
      StringBuffer sb = new StringBuffer(value.length());

      while(m.find()) {
         m.appendReplacement(sb, String.valueOf(m.group(1).charAt(0)));
      }

      m.appendTail(sb);
      String output = sb.toString();
      if (output.length() > 0 && output.charAt(0) >= '0' && output.charAt(0) <= '9') {
         output = "_" + output;
      }

      return output;
   }

   private Map<String, String> createMap(Object... parts) {
      HashMap<String, String> tokens = new HashMap();

      for(int i = 0; i < parts.length; i += 2) {
         tokens.put(parts[i].toString(), parts[i + 1].toString());
      }

      return tokens;
   }

   private String dynamicFormat(String string, Map<String, String> tokens) {
      String patternString = "\\$(" + String.join("|", (Iterable)tokens.keySet().stream().map((x) -> {
         return this.unicodify(x);
      }).collect(Collectors.toList())) + ")";
      Pattern pattern = Pattern.compile(patternString);
      Matcher matcher = pattern.matcher(string);
      StringBuffer sb = new StringBuffer();

      while(matcher.find()) {
         matcher.appendReplacement(sb, Matcher.quoteReplacement((String)tokens.get(matcher.group(1))));
      }

      matcher.appendTail(sb);
      return sb.toString();
   }

   private String dynamicRawFormat(String string, Map<String, String> tokens) {
      if (tokens.isEmpty()) {
         return string;
      } else {
         String patternString = "(" + String.join("|", (Iterable)tokens.keySet().stream().map((x) -> {
            return this.unicodify(x);
         }).collect(Collectors.toList())) + ")";
         Pattern pattern = Pattern.compile(patternString);
         Matcher matcher = pattern.matcher(string);
         StringBuffer sb = new StringBuffer();

         while(matcher.find()) {
            matcher.appendReplacement(sb, Matcher.quoteReplacement((String)tokens.get(matcher.group(1))));
         }

         matcher.appendTail(sb);
         return sb.toString();
      }
   }

   private String getStringPooledString(String value) {
      if (!this.stringPool.containsKey(value)) {
         this.stringPool.put(value, this.currentLength);
         this.currentLength += value.getBytes(StandardCharsets.UTF_8).length + 1;
      }

      return "((char *)(string_pool + " + this.stringPool.get(value) + "LL))";
   }

   private String getCachedClassPointer(String name) {
      if (!this.cachedClasses.containsKey(name)) {
         this.cachedClasses.put(name, this.cachedClasses.size());
      }

      return "(cclasses[" + this.cachedClasses.get(name) + "])";
   }

   private String getCachedMethodPointer(String clazz, String name, String desc, boolean isStatic) {
      if (!this.cachedMethods.containsKey(new CachedMethodInfo(clazz, name, desc, isStatic))) {
         this.cachedMethods.put(new CachedMethodInfo(clazz, name, desc, isStatic), this.cachedMethods.size());
      }

      return "(cmethods[" + this.cachedMethods.get(new CachedMethodInfo(clazz, name, desc, isStatic)) + "].load())";
   }

   private String getCachedFieldPointer(String clazz, String name, String desc, boolean isStatic) {
      if (!this.cachedFields.containsKey(new CachedFieldInfo(clazz, name, desc, isStatic))) {
         this.cachedFields.put(new CachedFieldInfo(clazz, name, desc, isStatic), this.cachedFields.size());
      }

      return "(cfields[" + this.cachedFields.get(new CachedFieldInfo(clazz, name, desc, isStatic)) + "].load())";
   }

   private int getCachedMethodId(String clazz, String name, String desc, boolean isStatic) {
      if (!this.cachedMethods.containsKey(new CachedMethodInfo(clazz, name, desc, isStatic))) {
         this.cachedMethods.put(new CachedMethodInfo(clazz, name, desc, isStatic), this.cachedMethods.size());
      }

      return (Integer)this.cachedMethods.get(new CachedMethodInfo(clazz, name, desc, isStatic));
   }

   private int getCachedFieldId(String clazz, String name, String desc, boolean isStatic) {
      if (!this.cachedFields.containsKey(new CachedFieldInfo(clazz, name, desc, isStatic))) {
         this.cachedFields.put(new CachedFieldInfo(clazz, name, desc, isStatic), this.cachedFields.size());
      }

      return (Integer)this.cachedFields.get(new CachedFieldInfo(clazz, name, desc, isStatic));
   }

   private String unicodify(String string) {
      StringBuilder result = new StringBuilder();
      char[] var3 = string.toCharArray();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         char c = var3[var5];
         result.append("\\u").append(String.format("%04x", Integer.valueOf(c)));
      }

      return result.toString();
   }

   private String dynamicStringPoolFormat(String key, Map<String, String> tokens) throws Exception {
      String value = CPP_SNIPPETS.getProperty(key);
      if (value == null) {
         throw new Exception(key + " not found");
      } else {
         String[] stringVars = CPP_SNIPPETS.getProperty(key + "_S_VARS") != null && !CPP_SNIPPETS.getProperty(key + "_S_VARS").equals("") ? CPP_SNIPPETS.getProperty(key + "_S_VARS").split(",") : new String[0];
         HashMap<String, String> vars = new HashMap();
         String[] var6 = stringVars;
         int var7 = stringVars.length;

         for(int var8 = 0; var8 < var7; ++var8) {
            String var = var6[var8];
            if (var.startsWith("#")) {
               vars.put(var, CPP_SNIPPETS.getProperty(key + "_S_CONST_" + var.substring(1)));
            } else {
               if (!var.startsWith("$")) {
                  throw new Exception("Unknown format modifier: " + var);
               }

               vars.put(var, tokens.get(var.substring(1)));
            }
         }

         vars.entrySet().stream().filter((varx) -> {
            return varx.getValue() == null;
         }).forEachOrdered((varx) -> {
            try {
               throw new Exception(key + " - " + (String)varx.getKey() + " is null");
            } catch (Exception var3) {
               var3.printStackTrace();
            }
         });
         HashMap<String, String> replaceTokens = new HashMap();
         vars.entrySet().forEach((varx) -> {
            replaceTokens.put(varx.getKey(), this.getStringPooledString((String)varx.getValue()));
         });
         tokens.entrySet().forEach((varx) -> {
            if (!replaceTokens.containsKey("$" + (String)varx.getKey())) {
               replaceTokens.put("$" + (String)varx.getKey(), varx.getValue());
            }

         });
         return this.dynamicRawFormat(value, replaceTokens);
      }
   }

   static <T> Stream<T> reverse(Stream<T> input) {
      Object[] temp = input.toArray();
      return IntStream.range(0, temp.length).mapToObj((i) -> {
         return temp[temp.length - i - 1];
      });
   }

   private void setupNewIfaceStaticClass(Boolean forAndroid) {
      if (this.currentIfaceStaticClass != null && this.currentIfaceStaticClass.methods.size() > 0) {
         this.readyIfaceStaticClasses.add(this.currentIfaceStaticClass);
      }

      this.currentIfaceStaticClass = new ClassNode();
      this.currentIfaceStaticClass.sourceFile = "synthetic";
      this.currentIfaceStaticClass.name = "native" + this.nativeDirId + "/interfacestatic/Methods" + this.readyIfaceStaticClasses.size();
      if (forAndroid) {
         this.currentIfaceStaticClass.version = 50;
      } else {
         this.currentIfaceStaticClass.version = 52;
      }

      this.currentIfaceStaticClass.superName = "java/lang/Object";
      this.currentIfaceStaticClass.access = 1;
   }

   private String visitMethod(ClassNode classNode, MethodNode methodNode, int index) throws Exception {
      if ((methodNode.access & 1024) <= 0 && (methodNode.access & 256) <= 0) {
         if (methodNode.name.equals("<init>")) {
            return "";
         } else {
            StringBuilder outputSb = new StringBuilder("// ");
            outputSb.append(methodNode.name).append(methodNode.desc).append("\n");
            String methodName = "";
            String var7 = methodNode.name;
            byte var8 = -1;
            switch(var7.hashCode()) {
            case -1944711511:
               if (var7.equals("<clinit>")) {
                  var8 = 1;
               }
               break;
            case 1818100338:
               if (var7.equals("<init>")) {
                  var8 = 0;
               }
            }

            MethodNode proxifiedResult;
            switch(var8) {
            case 0:
               proxifiedResult = new MethodNode(327680, 4370, "native_special_init" + index, methodNode.desc, methodNode.signature, new String[0]);
               classNode.methods.add(proxifiedResult);
               methodName = methodName + "native_special_init";
               break;
            case 1:
               proxifiedResult = new MethodNode(327680, 4362, "native_special_clinit" + index, methodNode.desc, methodNode.signature, new String[0]);
               classNode.methods.add(proxifiedResult);
               methodName = methodName + "native_special_clinit";
               break;
            default:
               proxifiedResult = methodNode;
               methodNode.access |= 256;
               methodName = methodName + "native_" + methodNode.name;
            }

            methodName = methodName + index;
            methodName = "__ngen_" + methodName.replace("/", "_");
            methodName = this.escapeCppNameString(methodName);
            int returnTypeSort = Type.getReturnType(methodNode.desc).getSort();
            Type[] args = Type.getArgumentTypes(methodNode.desc);
            MethodNode nativeMethod = null;
            int currentLine;
            int invokeSpecialId;
            if ((classNode.access & 512) > 0) {
               if (this.currentIfaceStaticClass.methods.size() > 16384) {
                  throw new Exception("too many static interface methods");
               }

               StringBuilder resultProcType = new StringBuilder("(");
               Type[] var11 = args;
               currentLine = args.length;

               for(invokeSpecialId = 0; invokeSpecialId < currentLine; ++invokeSpecialId) {
                  Type t = var11[invokeSpecialId];
                  resultProcType.append(JAVA_DESCRIPTORS[t.getSort()]);
               }

               resultProcType.append(")").append(JAVA_DESCRIPTORS[returnTypeSort]);
               String outerJavaMethodName = "iface_static_" + this.currentClassId + "_" + index;
               nativeMethod = new MethodNode(327680, 4361, outerJavaMethodName, resultProcType.toString(), (String)null, new String[0]);
               this.currentIfaceStaticClass.methods.add(nativeMethod);
               this.ifaceStaticNativeMethodsSb.append("            { (char *)").append(this.getStringPooledString(outerJavaMethodName)).append(", (char *)").append(this.getStringPooledString(resultProcType.toString())).append(", (void *)&").append(methodName).append(" },\n");
            } else {
               this.nativeMethodsSb.append("            { (char *)").append(this.getStringPooledString(proxifiedResult.name)).append(", (char *)").append(this.getStringPooledString(methodNode.desc)).append(", (void *)&").append(methodName).append(" },\n");
            }

            outputSb.append(CPP_TYPES[returnTypeSort]).append(" ").append("JNICALL").append(" ").append(methodName).append("(").append("JNIEnv *env").append(", ").append((methodNode.access & 8) > 0 ? "jclass clazz" : "jobject obj");
            if (args.length > 0) {
               outputSb.append(", ");
            }

            int localIndex;
            for(localIndex = 0; localIndex < args.length; ++localIndex) {
               outputSb.append(CPP_TYPES[args[localIndex].getSort()]).append(" ").append("arg").append(localIndex).append(localIndex == args.length - 1 ? "" : ", ");
            }

            outputSb.append(") {").append("\n");
            if (methodNode.maxStack > 0) {
               outputSb.append("    ").append("utils::jvm_stack<").append(methodNode.maxStack).append("> cstack;").append("\n");
            }

            if (methodNode.maxLocals > 0) {
               outputSb.append("    ").append("utils::local_vars<").append(methodNode.maxLocals).append("> clocals;").append("\n");
            }

            outputSb.append("    ").append("std::unordered_set<jobject> refs;").append("\n");
            outputSb.append("\n");
            localIndex = 0;
            if ((methodNode.access & 8) == 0) {
               outputSb.append("    ").append(this.dynamicStringPoolFormat("LOCAL_LOAD_ARG_9", this.createMap("index", localIndex, "arg", "obj"))).append("\n");
               ++localIndex;
            }

            for(int i = 0; i < args.length; ++i) {
               outputSb.append("    ").append(this.dynamicStringPoolFormat("LOCAL_LOAD_ARG_" + args[i].getSort(), this.createMap("index", localIndex, "arg", "arg" + i))).append("\n");
               localIndex += args[i].getSize();
            }

            outputSb.append("\n");
            List<TryCatchBlockNode> currentTryCatches = new ArrayList();
            currentLine = -1;
            invokeSpecialId = -1;
            List<Integer> currentStack = new ArrayList();
            List<Integer> currentLocals = new ArrayList();
            if ((methodNode.access & 8) == 0) {
               currentLocals.add(TYPE_TO_STACK[10]);
            }

            Type[] var16 = args;
            int var17 = args.length;

            for(int var18 = 0; var18 < var17; ++var18) {
               Type localArg = var16[var18];
               currentLocals.add(TYPE_TO_STACK[localArg.getSort()]);
            }

            int type;
            int currentLp;
            Type returnType;
            for(int insnIndex = 0; insnIndex < methodNode.instructions.size(); ++insnIndex) {
               if (methodNode.name.equals("<init>") && invokeSpecialId < 0) {
                  if (methodNode.instructions.get(insnIndex).getOpcode() == 183) {
                     invokeSpecialId = insnIndex;
                  }
               } else {
                  AbstractInsnNode insnNode = methodNode.instructions.get(insnIndex);
                  switch(insnNode.getType()) {
                  case 8:
                     outputSb.append(((LabelNode)insnNode).getLabel()).append(": ;").append("\n");
                     reverse(methodNode.tryCatchBlocks.stream().filter((node) -> {
                        return node.start.equals(insnNode);
                     })).forEachOrdered(currentTryCatches::add);
                     methodNode.tryCatchBlocks.stream().filter((node) -> {
                        return node.end.equals(insnNode);
                     }).forEachOrdered(currentTryCatches::remove);
                     continue;
                  case 14:
                     FrameNode frameNode = (FrameNode)insnNode;
                     Object stack;
                     Iterator var49;
                     label390:
                     switch(frameNode.type) {
                     case -1:
                     case 0:
                        currentLocals.clear();
                        currentStack.clear();
                        var49 = frameNode.local.iterator();

                        while(var49.hasNext()) {
                           stack = var49.next();
                           if (stack instanceof String) {
                              currentLocals.add(TYPE_TO_STACK[10]);
                           } else if (stack instanceof LabelNode) {
                              currentLocals.add(TYPE_TO_STACK[10]);
                           } else {
                              currentLocals.add(STACK_TO_STACK[(Integer)stack]);
                           }
                        }

                        var49 = frameNode.stack.iterator();

                        while(true) {
                           if (!var49.hasNext()) {
                              break label390;
                           }

                           stack = var49.next();
                           if (stack instanceof String) {
                              currentStack.add(TYPE_TO_STACK[10]);
                           } else if (stack instanceof LabelNode) {
                              currentStack.add(TYPE_TO_STACK[10]);
                           } else {
                              currentStack.add(STACK_TO_STACK[(Integer)stack]);
                           }
                        }
                     case 1:
                        var49 = frameNode.local.iterator();

                        while(true) {
                           if (!var49.hasNext()) {
                              break label390;
                           }

                           stack = var49.next();
                           if (stack instanceof String) {
                              currentLocals.add(TYPE_TO_STACK[10]);
                           } else if (stack instanceof LabelNode) {
                              currentLocals.add(TYPE_TO_STACK[10]);
                           } else {
                              currentLocals.add(STACK_TO_STACK[(Integer)stack]);
                           }
                        }
                     case 2:
                        for(currentLp = 0; currentLp < frameNode.local.size(); ++currentLp) {
                           currentLocals.remove(currentLocals.size() - 1);
                        }

                        currentStack.clear();
                     case 3:
                     default:
                        break;
                     case 4:
                        if (frameNode.stack.get(0) instanceof String) {
                           currentStack.add(TYPE_TO_STACK[10]);
                        } else if (frameNode.stack.get(0) instanceof LabelNode) {
                           currentStack.add(TYPE_TO_STACK[10]);
                        } else {
                           currentStack.add(STACK_TO_STACK[(Integer)frameNode.stack.get(0)]);
                        }
                     }

                     Iterator var50;
                     if (currentStack.stream().anyMatch((x) -> {
                        return x == 0;
                     })) {
                        currentLp = 0;
                        outputSb.append("    ");

                        for(var50 = currentStack.iterator(); var50.hasNext(); currentLp += Math.max(1, type)) {
                           type = (Integer)var50.next();
                           if (type == 0) {
                              outputSb.append("refs.erase(cstack.refs[" + currentLp + "]); ");
                           }
                        }

                        outputSb.append("\n");
                     }

                     if (currentLocals.stream().anyMatch((x) -> {
                        return x == 0;
                     })) {
                        currentLp = 0;
                        outputSb.append("    ");

                        for(var50 = currentLocals.iterator(); var50.hasNext(); currentLp += Math.max(1, type)) {
                           type = (Integer)var50.next();
                           if (type == 0) {
                              outputSb.append("refs.erase(clocals.refs[" + currentLp + "]); ");
                           }
                        }

                        outputSb.append("\n");
                     }

                     outputSb.append("    utils::clear_refs(env, refs);\n");
                     continue;
                  case 15:
                     outputSb.append("    ").append("// Line ").append(((LineNumberNode)insnNode).line).append(":").append("\n");
                     currentLine = ((LineNumberNode)insnNode).line;
                     continue;
                  }

                  StringBuilder tryCatch = new StringBuilder("\n");
                  if (currentTryCatches.size() > 0) {
                     tryCatch.append("    ").append(this.dynamicStringPoolFormat("TRYCATCH_START", this.createMap())).append("\n");

                     for(int i = currentTryCatches.size() - 1; i >= 0; --i) {
                        TryCatchBlockNode tryCatchBlock = (TryCatchBlockNode)currentTryCatches.get(i);
                        if (tryCatchBlock.type == null) {
                           tryCatch.append("    ").append(this.dynamicStringPoolFormat("TRYCATCH_ANY_L", this.createMap("rettype", CPP_TYPES[returnTypeSort], "handler_block", tryCatchBlock.handler.getLabel().toString()))).append("\n");
                           break;
                        }

                        tryCatch.append("    ").append(this.dynamicStringPoolFormat("TRYCATCH_CHECK", this.createMap("rettype", CPP_TYPES[returnTypeSort], "exception_class_ptr", this.getCachedClassPointer(tryCatchBlock.type), "handler_block", tryCatchBlock.handler.getLabel().toString()))).append("\n");
                     }

                     tryCatch.append("    ").append(this.dynamicStringPoolFormat("TRYCATCH_END", this.createMap("rettype", CPP_TYPES[returnTypeSort])));
                  } else {
                     tryCatch.append("    ").append(this.dynamicStringPoolFormat("TRYCATCH_EMPTY", this.createMap("rettype", CPP_TYPES[returnTypeSort])));
                  }

                  outputSb.append("    ");
                  String insnName = (String)INSTRUCTIONS.getOrDefault(insnNode.getOpcode(), "NOTFOUND");
                  HashMap<String, String> props = new HashMap();
                  props.put("line", String.valueOf(currentLine));
                  props.put("trycatchhandler", tryCatch.toString());
                  props.put("rettype", CPP_TYPES[returnTypeSort]);
                  String trimmedTryCatchBlock = tryCatch.toString().trim().replace("\n", " ");
                  int switchIndex;
                  if (insnNode instanceof FieldInsnNode) {
                     insnName = insnName + "_" + Type.getType(((FieldInsnNode)insnNode).desc).getSort();
                     if (insnNode.getOpcode() == 178 || insnNode.getOpcode() == 179) {
                        props.put("class_ptr", this.getCachedClassPointer(((FieldInsnNode)insnNode).owner));
                     }

                     switchIndex = this.getCachedFieldId(((FieldInsnNode)insnNode).owner, ((FieldInsnNode)insnNode).name, ((FieldInsnNode)insnNode).desc, insnNode.getOpcode() == 178 || insnNode.getOpcode() == 179);
                     outputSb.append("if (!cfields[").append(switchIndex).append("].load()) { cfields[").append(switchIndex).append("].store(env->Get").append(insnNode.getOpcode() != 178 && insnNode.getOpcode() != 179 ? "" : "Static").append("FieldID(").append(this.getCachedClassPointer(((FieldInsnNode)insnNode).owner)).append(", ").append(this.getStringPooledString(((FieldInsnNode)insnNode).name)).append(", ").append(this.getStringPooledString(((FieldInsnNode)insnNode).desc)).append(")); ").append(trimmedTryCatchBlock).append("  } ");
                     props.put("fieldid", this.getCachedFieldPointer(((FieldInsnNode)insnNode).owner, ((FieldInsnNode)insnNode).name, ((FieldInsnNode)insnNode).desc, insnNode.getOpcode() == 178 || insnNode.getOpcode() == 179));
                  }

                  if (insnNode instanceof IincInsnNode) {
                     props.put("incr", String.valueOf(((IincInsnNode)insnNode).incr));
                     props.put("var", String.valueOf(((IincInsnNode)insnNode).var));
                  }

                  if (insnNode instanceof IntInsnNode) {
                     props.put("operand", String.valueOf(((IntInsnNode)insnNode).operand));
                     if (insnNode.getOpcode() == 188) {
                        insnName = insnName + "_" + ((IntInsnNode)insnNode).operand;
                     }
                  }

                  ArrayList argSorts;
                  int methodId;
                  int var31;
                  String cppCode;
                  int methodId;
                  if (insnNode instanceof InvokeDynamicInsnNode) {
                     cppCode = "invokedynamic$" + methodNode.name + "$" + this.invokeDynamics.size();
                     this.invokeDynamics.put(cppCode, (InvokeDynamicInsnNode)insnNode);
                     Type returnType = Type.getReturnType(((InvokeDynamicInsnNode)insnNode).desc);
                     Type[] argTypes = Type.getArgumentTypes(((InvokeDynamicInsnNode)insnNode).desc);
                     insnName = "INVOKESTATIC_" + returnType.getSort();
                     StringBuilder argsBuilder = new StringBuilder();
                     argSorts = new ArrayList();
                     List<Integer> argSorts = new ArrayList();
                     methodId = -1;
                     Type[] var30 = argTypes;
                     var31 = argTypes.length;

                     for(int var32 = 0; var32 < var31; ++var32) {
                        Type argType = var30[var32];
                        int currentOffset = methodId;
                        methodId -= argType.getSize();
                        argSorts.add(currentOffset);
                        argSorts.add(argType.getSort());
                     }

                     for(methodId = 0; methodId < argSorts.size(); ++methodId) {
                        argsBuilder.append(", ").append(this.dynamicStringPoolFormat("INVOKE_ARG_" + argSorts.get(methodId), this.createMap("index", String.valueOf(argSorts.get(methodId)))));
                     }

                     outputSb.append(this.dynamicStringPoolFormat("INVOKE_POPCNT", this.createMap("count", String.valueOf(-methodId - 1)))).append(" ");
                     props.put("class_ptr", this.getCachedClassPointer(classNode.name));
                     methodId = this.getCachedMethodId(classNode.name, cppCode, ((InvokeDynamicInsnNode)insnNode).desc, true);
                     outputSb.append("if (!cmethods[").append(methodId).append("].load()) { cmethods[").append(methodId).append("].store(env->GetStaticMethodID(").append(this.getCachedClassPointer(classNode.name)).append(", ").append(this.getStringPooledString(cppCode)).append(", ").append(this.getStringPooledString(((InvokeDynamicInsnNode)insnNode).desc)).append(")); ").append(trimmedTryCatchBlock).append("  } ");
                     props.put("methodid", this.getCachedMethodPointer(classNode.name, cppCode, ((InvokeDynamicInsnNode)insnNode).desc, true));
                     props.put("args", argsBuilder.toString());
                  }

                  if (insnNode instanceof JumpInsnNode) {
                     props.put("label", String.valueOf(((JumpInsnNode)insnNode).label.getLabel()));
                  }

                  if (insnNode instanceof LdcInsnNode) {
                     Object cst = ((LdcInsnNode)insnNode).cst;
                     if (cst instanceof String) {
                        insnName = insnName + "_STRING";
                        props.put("cst", String.valueOf(((LdcInsnNode)insnNode).cst));
                     } else if (cst instanceof Integer) {
                        insnName = insnName + "_INT";
                        props.put("cst", String.valueOf(((LdcInsnNode)insnNode).cst));
                     } else if (cst instanceof Long) {
                        insnName = insnName + "_LONG";
                        props.put("cst", String.valueOf(((LdcInsnNode)insnNode).cst));
                     } else if (cst instanceof Float) {
                        insnName = insnName + "_FLOAT";
                        props.put("cst", String.valueOf(((LdcInsnNode)insnNode).cst));
                        float cstVal = (Float)cst;
                        if (cst.toString().equals("NaN")) {
                           props.put("cst", "NAN");
                        } else if (cstVal == Float.POSITIVE_INFINITY) {
                           props.put("cst", "HUGE_VALF");
                        } else if (cstVal == Float.NEGATIVE_INFINITY) {
                           props.put("cst", "-HUGE_VALF");
                        }
                     } else if (cst instanceof Double) {
                        insnName = insnName + "_DOUBLE";
                        props.put("cst", String.valueOf(((LdcInsnNode)insnNode).cst));
                        double cstVal = (Double)cst;
                        if (cst.toString().equals("NaN")) {
                           props.put("cst", "NAN");
                        } else if (cstVal == Double.POSITIVE_INFINITY) {
                           props.put("cst", "HUGE_VAL");
                        } else if (cstVal == Double.NEGATIVE_INFINITY) {
                           props.put("cst", "-HUGE_VAL");
                        }
                     } else {
                        if (!(cst instanceof Type)) {
                           throw new UnsupportedOperationException();
                        }

                        insnName = insnName + "_CLASS";
                        props.put("cst_ptr", this.getCachedClassPointer(((LdcInsnNode)insnNode).cst.toString()));
                     }
                  }

                  if (insnNode instanceof LookupSwitchInsnNode) {
                     outputSb.append(this.dynamicStringPoolFormat("LOOKUPSWITCH_START", this.createMap())).append("\n");

                     for(switchIndex = 0; switchIndex < ((LookupSwitchInsnNode)insnNode).labels.size(); ++switchIndex) {
                        outputSb.append("    ").append("    ").append(this.dynamicStringPoolFormat("LOOKUPSWITCH_PART", this.createMap("key", String.valueOf(((LookupSwitchInsnNode)insnNode).keys.get(switchIndex)), "label", String.valueOf(((LabelNode)((LookupSwitchInsnNode)insnNode).labels.get(switchIndex)).getLabel())))).append("\n");
                     }

                     outputSb.append("    ").append("    ").append(this.dynamicStringPoolFormat("LOOKUPSWITCH_DEFAULT", this.createMap("label", String.valueOf(((LookupSwitchInsnNode)insnNode).dflt.getLabel())))).append("\n");
                     outputSb.append("    ").append(this.dynamicStringPoolFormat("LOOKUPSWITCH_END", this.createMap())).append("\n");
                  } else {
                     if (insnNode instanceof MethodInsnNode) {
                        returnType = Type.getReturnType(((MethodInsnNode)insnNode).desc);
                        Type[] argTypes = Type.getArgumentTypes(((MethodInsnNode)insnNode).desc);
                        insnName = insnName + "_" + returnType.getSort();
                        StringBuilder argsBuilder = new StringBuilder();
                        List<Integer> argOffsets = new ArrayList();
                        argSorts = new ArrayList();
                        int stackOffset = -1;
                        Type[] var67 = argTypes;
                        methodId = argTypes.length;

                        for(var31 = 0; var31 < methodId; ++var31) {
                           Type argType = var67[var31];
                           int currentOffset = stackOffset;
                           stackOffset -= argType.getSize();
                           argOffsets.add(currentOffset);
                           argSorts.add(argType.getSort());
                        }

                        if (insnNode.getOpcode() != 185 && insnNode.getOpcode() != 183 && insnNode.getOpcode() != 182) {
                           for(methodId = 0; methodId < argOffsets.size(); ++methodId) {
                              argsBuilder.append(", ").append(this.dynamicStringPoolFormat("INVOKE_ARG_" + argSorts.get(methodId), this.createMap("index", String.valueOf(argOffsets.get(methodId)))));
                           }

                           if (-stackOffset - 1 != 0) {
                              outputSb.append(this.dynamicStringPoolFormat("INVOKE_POPCNT", this.createMap("count", String.valueOf(-stackOffset - 1)))).append(" ");
                           }

                           props.put("class_ptr", this.getCachedClassPointer(((MethodInsnNode)insnNode).owner));
                           methodId = this.getCachedMethodId(((MethodInsnNode)insnNode).owner, ((MethodInsnNode)insnNode).name, ((MethodInsnNode)insnNode).desc, true);
                           outputSb.append("if (!cmethods[").append(methodId).append("].load()) { cmethods[").append(methodId).append("].store(env->GetStaticMethodID(").append(this.getCachedClassPointer(((MethodInsnNode)insnNode).owner)).append(", ").append(this.getStringPooledString(((MethodInsnNode)insnNode).name)).append(", ").append(this.getStringPooledString(((MethodInsnNode)insnNode).desc)).append(")); ").append(trimmedTryCatchBlock).append("  } ");
                           props.put("methodid", this.getCachedMethodPointer(((MethodInsnNode)insnNode).owner, ((MethodInsnNode)insnNode).name, ((MethodInsnNode)insnNode).desc, true));
                           props.put("args", argsBuilder.toString());
                        } else {
                           for(methodId = 0; methodId < argOffsets.size(); ++methodId) {
                              argsBuilder.append(", ").append(this.dynamicStringPoolFormat("INVOKE_ARG_" + argSorts.get(methodId), this.createMap("index", String.valueOf((Integer)argOffsets.get(methodId) - 1))));
                           }

                           if (stackOffset != 0) {
                              outputSb.append(this.dynamicStringPoolFormat("INVOKE_POPCNT", this.createMap("count", String.valueOf(-stackOffset)))).append(" ");
                           }

                           if (insnNode.getOpcode() == 183) {
                              props.put("class_ptr", this.getCachedClassPointer(((MethodInsnNode)insnNode).owner));
                           }

                           methodId = this.getCachedMethodId(((MethodInsnNode)insnNode).owner, ((MethodInsnNode)insnNode).name, ((MethodInsnNode)insnNode).desc, false);
                           outputSb.append("if (!cmethods[").append(methodId).append("].load()) { cmethods[").append(methodId).append("].store(env->GetMethodID(").append(this.getCachedClassPointer(((MethodInsnNode)insnNode).owner)).append(", ").append(this.getStringPooledString(((MethodInsnNode)insnNode).name)).append(", ").append(this.getStringPooledString(((MethodInsnNode)insnNode).desc)).append(")); ").append(trimmedTryCatchBlock).append("  } ");
                           props.put("methodid", this.getCachedMethodPointer(((MethodInsnNode)insnNode).owner, ((MethodInsnNode)insnNode).name, ((MethodInsnNode)insnNode).desc, false));
                           props.put("object_offset", "-1");
                           props.put("args", argsBuilder.toString());
                        }
                     }

                     if (insnNode instanceof MultiANewArrayInsnNode) {
                        props.put("count", String.valueOf(((MultiANewArrayInsnNode)insnNode).dims));
                        props.put("desc", ((MultiANewArrayInsnNode)insnNode).desc);
                     }

                     if (!(insnNode instanceof TableSwitchInsnNode)) {
                        if (insnNode instanceof TypeInsnNode) {
                           props.put("desc", ((TypeInsnNode)insnNode).desc);
                           props.put("desc_ptr", this.getCachedClassPointer(((TypeInsnNode)insnNode).desc));
                        }

                        if (insnNode instanceof VarInsnNode) {
                           props.put("var", String.valueOf(((VarInsnNode)insnNode).var));
                        }

                        cppCode = CPP_SNIPPETS.getProperty(insnName);
                        if (cppCode == null) {
                           throw new Exception("insn not found: " + insnName);
                        }

                        cppCode = this.dynamicStringPoolFormat(insnName, props);
                        outputSb.append(cppCode);
                        outputSb.append("\n");
                     } else {
                        outputSb.append(this.dynamicStringPoolFormat("TABLESWITCH_START", this.createMap())).append("\n");

                        for(switchIndex = 0; switchIndex < ((TableSwitchInsnNode)insnNode).labels.size(); ++switchIndex) {
                           outputSb.append("    ").append("    ").append(this.dynamicStringPoolFormat("TABLESWITCH_PART", this.createMap("index", String.valueOf(((TableSwitchInsnNode)insnNode).min + switchIndex), "label", String.valueOf(((LabelNode)((TableSwitchInsnNode)insnNode).labels.get(switchIndex)).getLabel())))).append("\n");
                        }

                        outputSb.append("    ").append("    ").append(this.dynamicStringPoolFormat("TABLESWITCH_DEFAULT", this.createMap("label", String.valueOf(((TableSwitchInsnNode)insnNode).dflt.getLabel())))).append("\n");
                        outputSb.append("    ").append(this.dynamicStringPoolFormat("TABLESWITCH_END", this.createMap())).append("\n");
                     }
                  }
               }
            }

            outputSb.append("    return (").append(CPP_TYPES[returnTypeSort]).append(") 0;\n");
            outputSb.append("}\n\n");
            methodNode.localVariables.clear();
            methodNode.tryCatchBlocks.clear();
            String var43 = methodNode.name;
            byte var45 = -1;
            switch(var43.hashCode()) {
            case -1944711511:
               if (var43.equals("<clinit>")) {
                  var45 = 1;
               }
               break;
            case 1818100338:
               if (var43.equals("<init>")) {
                  var45 = 0;
               }
            }

            InsnList list;
            int var55;
            Type[] var66;
            switch(var45) {
            case 0:
               list = new InsnList();

               for(currentLp = 0; currentLp <= invokeSpecialId; ++currentLp) {
                  list.add(methodNode.instructions.get(currentLp));
               }

               list.add(new VarInsnNode(25, 0));
               currentLp = 1;
               var66 = args;
               type = args.length;

               for(var55 = 0; var55 < type; ++var55) {
                  returnType = var66[var55];
                  list.add(new VarInsnNode(returnType.getOpcode(21), currentLp));
                  currentLp += returnType.getSize();
               }

               list.add(new MethodInsnNode(182, classNode.name, "native_special_init" + index, methodNode.desc));
               list.add(new InsnNode(177));
               methodNode.instructions = list;
               break;
            case 1:
               methodNode.instructions.clear();
               methodNode.instructions.add(new LdcInsnNode(this.currentClassId));
               methodNode.instructions.add(new MethodInsnNode(184, "native" + this.nativeDirId + "/Loader", "registerNativesForClass", "(I)V"));
               methodNode.instructions.add(new MethodInsnNode(184, classNode.name, "native_special_clinit" + index, methodNode.desc));
               if ((classNode.access & 512) > 0) {
                  proxifiedResult.instructions.add(new MethodInsnNode(184, this.currentIfaceStaticClass.name, nativeMethod.name, nativeMethod.desc));
                  proxifiedResult.instructions.add(new InsnNode(177));
               }

               methodNode.instructions.add(new InsnNode(177));
               break;
            default:
               methodNode.instructions.clear();
               if ((classNode.access & 512) > 0) {
                  list = new InsnList();

                  for(currentLp = 0; currentLp <= invokeSpecialId; ++currentLp) {
                     list.add(methodNode.instructions.get(currentLp));
                  }

                  currentLp = 0;
                  var66 = args;
                  type = args.length;

                  for(var55 = 0; var55 < type; ++var55) {
                     returnType = var66[var55];
                     list.add(new VarInsnNode(returnType.getOpcode(21), currentLp));
                     currentLp += returnType.getSize();
                  }

                  methodNode.instructions.add(new MethodInsnNode(184, this.currentIfaceStaticClass.name, nativeMethod.name, nativeMethod.desc));
                  methodNode.instructions.add(new InsnNode(Type.getReturnType(methodNode.desc).getOpcode(172)));
               }
            }

            return outputSb.toString();
         }
      } else {
         return "";
      }
   }

   private void processIndy(ClassNode classNode, String methodName, InvokeDynamicInsnNode indy) {
      MethodNode indyWrapper = new MethodNode(327680, 4122, methodName, indy.desc, (String)null, new String[0]);
      int localVarsPosition = 0;
      Type[] var6 = Type.getArgumentTypes(indy.desc);
      int var7 = var6.length;

      for(int var8 = 0; var8 < var7; ++var8) {
         Type arg = var6[var8];
         indyWrapper.instructions.add(new VarInsnNode(arg.getOpcode(21), localVarsPosition));
         localVarsPosition += arg.getSize();
      }

      indyWrapper.instructions.add(new InvokeDynamicInsnNode(indy.name, indy.desc, indy.bsm, indy.bsmArgs));
      indyWrapper.instructions.add(new InsnNode(176));
      classNode.methods.add(indyWrapper);
   }

   private String writeStreamToString(InputStream stream) throws IOException {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      transfer(stream, baos);
      return new String(baos.toByteArray(), StandardCharsets.UTF_8);
   }

   private void writeStreamToFile(InputStream stream, Path path) throws IOException {
      byte[] buffer = new byte[4096];
      OutputStream outputStream = Files.newOutputStream(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
      Throwable var6 = null;

      try {
         int bytesRead;
         try {
            while((bytesRead = stream.read(buffer)) != -1) {
               outputStream.write(buffer, 0, bytesRead);
            }
         } catch (Throwable var15) {
            var6 = var15;
            throw var15;
         }
      } finally {
         if (outputStream != null) {
            if (var6 != null) {
               try {
                  outputStream.close();
               } catch (Throwable var14) {
                  var6.addSuppressed(var14);
               }
            } else {
               outputStream.close();
            }
         }

      }

   }

   private String getGetterForType(String desc) {
      if (desc.startsWith("[")) {
         return "env->FindClass(" + this.getStringPooledString(desc) + ")";
      } else {
         if (desc.endsWith(";")) {
            desc = desc.substring(1, desc.length() - 1);
         }

         return "utils::find_class_wo_static(env, " + this.getStringPooledString(desc.replace("/", ".")) + ")";
      }
   }

   public void process(Path inputJar, Path outputDir, List<Path> libs, Boolean forAndroid, String config) throws IOException {
      libs.add(inputJar);
      List<String> attr = new ArrayList();
      String[] var7 = config.split("\n");
      int var8 = var7.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         String s = var7[var9];
         attr.add(s.replace(".", "/"));
      }

      ClassMetadataReader metadataReader = new ClassMetadataReader((List)libs.stream().map((x) -> {
         try {
            return new JarFile(inputJar.toFile());
         } catch (IOException var3) {
            return null;
         }
      }).collect(Collectors.toList()));
      File jar = inputJar.toAbsolutePath().toFile();
      Files.createDirectories(outputDir);
      Files.createDirectories(outputDir.resolve("cpp"));
      Files.createDirectories(outputDir.resolve("cpp").resolve("output"));
      InputStream in = NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/native_jvm.cpp");
      Throwable var423 = null;

      try {
         this.writeStreamToFile(in, outputDir.resolve("cpp").resolve("native_jvm.cpp"));
      } catch (Throwable var406) {
         var423 = var406;
         throw var406;
      } finally {
         if (in != null) {
            if (var423 != null) {
               try {
                  in.close();
               } catch (Throwable var395) {
                  var423.addSuppressed(var395);
               }
            } else {
               in.close();
            }
         }

      }

      in = NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/native_jvm.hpp");
      var423 = null;

      try {
         this.writeStreamToFile(in, outputDir.resolve("cpp").resolve("native_jvm.hpp"));
      } catch (Throwable var405) {
         var423 = var405;
         throw var405;
      } finally {
         if (in != null) {
            if (var423 != null) {
               try {
                  in.close();
               } catch (Throwable var391) {
                  var423.addSuppressed(var391);
               }
            } else {
               in.close();
            }
         }

      }

      in = NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/native_jvm_output.hpp");
      var423 = null;

      try {
         this.writeStreamToFile(in, outputDir.resolve("cpp").resolve("native_jvm_output.hpp"));
      } catch (Throwable var404) {
         var423 = var404;
         throw var404;
      } finally {
         if (in != null) {
            if (var423 != null) {
               try {
                  in.close();
               } catch (Throwable var393) {
                  var423.addSuppressed(var393);
               }
            } else {
               in.close();
            }
         }

      }

      in = NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/string_pool.hpp");
      var423 = null;

      try {
         this.writeStreamToFile(in, outputDir.resolve("cpp").resolve("string_pool.hpp"));
      } catch (Throwable var403) {
         var423 = var403;
         throw var403;
      } finally {
         if (in != null) {
            if (var423 != null) {
               try {
                  in.close();
               } catch (Throwable var396) {
                  var423.addSuppressed(var396);
               }
            } else {
               in.close();
            }
         }

      }

      if (forAndroid) {
         in = NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/android/jvmti.h");
         var423 = null;

         try {
            this.writeStreamToFile(in, outputDir.resolve("cpp").resolve("jvmti.h"));
         } catch (Throwable var402) {
            var423 = var402;
            throw var402;
         } finally {
            if (in != null) {
               if (var423 != null) {
                  try {
                     in.close();
                  } catch (Throwable var398) {
                     var423.addSuppressed(var398);
                  }
               } else {
                  in.close();
               }
            }

         }
      }

      StringBuilder outputHeaderSb = new StringBuilder();
      StringBuilder outputHeaderIncludesSb = new StringBuilder();
      List<String> cmakeClassFiles = new ArrayList();
      List<String> cmakeMainFiles = new ArrayList();
      cmakeMainFiles.add("native_jvm.hpp");
      cmakeMainFiles.add("native_jvm.cpp");
      cmakeMainFiles.add("native_jvm_output.hpp");
      cmakeMainFiles.add("native_jvm_output.cpp");
      cmakeMainFiles.add("string_pool.hpp");
      cmakeMainFiles.add("string_pool.cpp");
      String projectName = "Armadillo";
      JarFile f = new JarFile(jar);
      Throwable var15 = null;

      Throwable var17;
      try {
         ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(outputDir.resolve(jar.getName()), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING));
         var17 = null;

         try {
            System.out.println("Processing " + jar + "...");

            while(true) {
               int currentNativeDirId = this.nativeDirId;
               if (!f.stream().anyMatch((x) -> {
                  return x.getName().startsWith("native" + currentNativeDirId);
               })) {
                  f.stream().forEach((e) -> {
                     try {
                        if (e.getName().equals("META-INF/MANIFEST.MF")) {
                           return;
                        }

                        if (!e.getName().endsWith(".class")) {
                           writeEntry(f, out, e);
                           return;
                        }

                        if (!attr.contains(e.getName().replace(".class", ""))) {
                           writeEntry(f, out, e);
                           return;
                        }

                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        InputStream in = f.getInputStream(e);
                        Throwable var13 = null;

                        try {
                           transfer(in, baos);
                        } catch (Throwable var66) {
                           var13 = var66;
                           throw var66;
                        } finally {
                           if (in != null) {
                              if (var13 != null) {
                                 try {
                                    in.close();
                                 } catch (Throwable var65) {
                                    var13.addSuppressed(var65);
                                 }
                              } else {
                                 in.close();
                              }
                           }

                        }

                        byte[] src = baos.toByteArray();
                        if (byteArrayToInt(Arrays.copyOfRange(src, 0, 4)) != -889275714) {
                           writeEntry(out, e.getName(), src);
                           return;
                        }

                        this.nativeMethodsSb = new StringBuilder();
                        this.ifaceStaticNativeMethodsSb = new StringBuilder();
                        this.invokeDynamics = new HashMap();
                        ClassReader classReader = new ClassReader(src);
                        ClassNode classNode = new ClassNode(327680);
                        classReader.accept(classNode, 0);
                        if (classNode.methods.stream().filter((x) -> {
                           return (x.access & 1280) == 0 && !x.name.equals("<init>");
                        }).count() == 0L) {
                           System.out.println("Skipping " + classNode.name);
                           writeEntry(out, e.getName(), src);
                           return;
                        }

                        System.out.println("Processing " + classNode.name);
                        if (!classNode.methods.stream().anyMatch((x) -> {
                           return x.name.equals("<clinit>");
                        })) {
                           classNode.methods.add(new MethodNode(327680, 8, "<clinit>", "()V", (String)null, new String[0]));
                        }

                        this.setupNewIfaceStaticClass(forAndroid);
                        this.cachedClasses.clear();
                        this.cachedMethods.clear();
                        this.cachedFields.clear();
                        BufferedWriter outputCppFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputDir.resolve("cpp").resolve("output").resolve(this.escapeCppNameString(classNode.name.replace('/', '_')).concat(".cpp")).toFile()), StandardCharsets.UTF_8));
                        Throwable var16 = null;

                        try {
                           BufferedWriter outputHppFile = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputDir.resolve("cpp").resolve("output").resolve(this.escapeCppNameString(classNode.name.replace('/', '_')).concat(".hpp")).toFile()), StandardCharsets.UTF_8));
                           Throwable var18 = null;

                           try {
                              StringBuilder insnsSb = new StringBuilder();
                              classNode.sourceFile = this.escapeCppNameString(classNode.name.replace('/', '_')) + ".cpp";

                              int i;
                              for(i = 0; i < classNode.methods.size(); ++i) {
                                 insnsSb.append(this.visitMethod(classNode, (MethodNode)classNode.methods.get(i), i).replace("\n", "\n    "));
                              }

                              if ((classNode.access & 512) > 0) {
                                 for(i = 0; i < classNode.methods.size(); ++i) {
                                    MethodNode var10000 = (MethodNode)classNode.methods.get(i);
                                    var10000.access &= -257;
                                 }
                              }

                              this.invokeDynamics.forEach((key, value) -> {
                                 this.processIndy(classNode, key, value);
                              });
                              classNode.version = 52;
                              ClassWriter classWriter = new SafeClassWriter(metadataReader, 327683);
                              classNode.accept(classWriter);
                              writeEntry(out, e.getName(), classWriter.toByteArray());
                              outputCppFile.append("#include \"../native_jvm.hpp\"\n");
                              outputHppFile.append("#include \"../native_jvm.hpp\"\n");
                              outputCppFile.append("#include \"../string_pool.hpp\"\n");
                              outputCppFile.append("#include \"").append(this.escapeCppNameString(classNode.name.replace('/', '_')).concat(".hpp")).append("\"\n");
                              cmakeClassFiles.add("output/" + this.escapeCppNameString(classNode.name.replace('/', '_')) + ".hpp");
                              cmakeClassFiles.add("output/" + this.escapeCppNameString(classNode.name.replace('/', '_')) + ".cpp");
                              outputHeaderIncludesSb.append("#include \"output/").append(this.escapeCppNameString(classNode.name.replace('/', '_')).concat(".hpp")).append("\"\n");
                              outputCppFile.append("\n");
                              outputCppFile.append("// ").append(classNode.name).append("\n");
                              outputCppFile.append("namespace native_jvm::classes::__ngen_").append(this.escapeCppNameString(classNode.name.replace("/", "_"))).append(" {\n\n");
                              outputCppFile.append("   signed char *string_pool;\n\n");
                              if (this.cachedClasses.size() > 0) {
                                 outputCppFile.append("    jclass cclasses[" + this.cachedClasses.size() + "];\n");
                              }

                              if (this.cachedMethods.size() > 0) {
                                 outputCppFile.append("    std::atomic<jmethodID> cmethods[" + this.cachedMethods.size() + "];\n");
                              }

                              if (this.cachedFields.size() > 0) {
                                 outputCppFile.append("    std::atomic<jfieldID> cfields[" + this.cachedFields.size() + "];\n");
                              }

                              outputCppFile.append("\n");
                              outputHppFile.append("\n");
                              outputHppFile.append("#ifndef ").append(this.escapeCppNameString(classNode.name.replace('/', '_')).concat("_hpp").toUpperCase()).append("_GUARD\n");
                              outputHppFile.append("\n");
                              outputHppFile.append("#define ").append(this.escapeCppNameString(classNode.name.replace('/', '_')).concat("_hpp").toUpperCase()).append("_GUARD\n");
                              outputHppFile.append("\n");
                              outputHppFile.append("// ").append(classNode.name).append("\n");
                              outputHppFile.append("namespace native_jvm::classes::__ngen_").append(this.escapeCppNameString(classNode.name.replace("/", "_"))).append(" {\n\n");
                              outputCppFile.append("    ");
                              outputCppFile.append(insnsSb);
                              outputCppFile.append("\n");
                              outputCppFile.append("    void __ngen_register_methods(JNIEnv *env, jvmtiEnv *jvmti_env) {\n");
                              outputHppFile.append("    void __ngen_register_methods(JNIEnv *env, jvmtiEnv *jvmti_env);\n");
                              outputCppFile.append("        string_pool = string_pool::get_pool();\n\n");
                              Iterator var21 = this.cachedClasses.entrySet().iterator();

                              while(var21.hasNext()) {
                                 Entry<String, Integer> clazz = (Entry)var21.next();
                                 outputCppFile.append("        if (jclass clazz = ").append(this.getGetterForType((String)clazz.getKey())).append(") { cclasses[" + clazz.getValue() + "] = (jclass) env->NewGlobalRef(clazz); env->DeleteLocalRef(clazz); }\n");
                              }

                              if (!this.cachedClasses.isEmpty()) {
                                 outputCppFile.append("\n");
                              }

                              if (this.nativeMethodsSb.length() > 0) {
                                 outputCppFile.append("        JNINativeMethod __ngen_methods[] = {\n");
                                 outputCppFile.append(this.nativeMethodsSb);
                                 outputCppFile.append("        };\n\n");
                                 outputCppFile.append("        jclass clazz = ").append(this.getGetterForType(classNode.name)).append(";\n");
                                 outputCppFile.append("        if (clazz) env->RegisterNatives(clazz, __ngen_methods, sizeof(__ngen_methods) / sizeof(__ngen_methods[0]));\n");
                                 outputCppFile.append("        if (env->ExceptionCheck()) { fprintf(stderr, \"Exception occured while registering native_jvm for %s\\n\", ").append(this.getStringPooledString(classNode.name.replace("/", "."))).append("); fflush(stderr); env->ExceptionDescribe(); env->ExceptionClear(); }\n");
                                 outputCppFile.append("\n");
                              }

                              if (this.ifaceStaticNativeMethodsSb.length() > 0) {
                                 outputCppFile.append("        JNINativeMethod __ngen_static_iface_methods[] = {\n");
                                 outputCppFile.append(this.ifaceStaticNativeMethodsSb);
                                 outputCppFile.append("        };\n\n");
                                 outputCppFile.append("        jclass clazz = utils::find_class_wo_static(env, ").append(this.getStringPooledString(this.currentIfaceStaticClass.name.replace("/", "."))).append(");\n");
                                 outputCppFile.append("        if (clazz) env->RegisterNatives(clazz, __ngen_static_iface_methods, sizeof(__ngen_static_iface_methods) / sizeof(__ngen_static_iface_methods[0]));\n");
                                 outputCppFile.append("        if (env->ExceptionCheck()) { fprintf(stderr, \"Exception occured while registering native_jvm for %s\\n\", ").append(this.getStringPooledString(classNode.name.replace("/", "."))).append("); fflush(stderr); env->ExceptionDescribe(); env->ExceptionClear(); }\n");
                              }

                              outputCppFile.append("    }\n");
                              outputCppFile.append("}");
                              outputHppFile.append("}\n\n#endif");
                              outputHeaderSb.append("        reg_methods[").append(this.currentClassId).append("] = &(native_jvm::classes::__ngen_").append(this.escapeCppNameString(classNode.name.replace("/", "_"))).append("::__ngen_register_methods);\n");
                           } catch (Throwable var67) {
                              var18 = var67;
                              throw var67;
                           } finally {
                              if (outputHppFile != null) {
                                 if (var18 != null) {
                                    try {
                                       outputHppFile.close();
                                    } catch (Throwable var64) {
                                       var18.addSuppressed(var64);
                                    }
                                 } else {
                                    outputHppFile.close();
                                 }
                              }

                           }
                        } catch (Throwable var69) {
                           var16 = var69;
                           throw var69;
                        } finally {
                           if (outputCppFile != null) {
                              if (var16 != null) {
                                 try {
                                    outputCppFile.close();
                                 } catch (Throwable var63) {
                                    var16.addSuppressed(var63);
                                 }
                              } else {
                                 outputCppFile.close();
                              }
                           }

                        }

                        ++this.currentClassId;
                     } catch (Exception var72) {
                        var72.printStackTrace(System.err);
                     }

                  });
                  Manifest mf = f.getManifest();
                  this.setupNewIfaceStaticClass(forAndroid);
                  Iterator var19 = this.readyIfaceStaticClasses.iterator();

                  SafeClassWriter classWriter;
                  while(var19.hasNext()) {
                     ClassNode ifaceStaticClass = (ClassNode)var19.next();
                     classWriter = new SafeClassWriter(metadataReader, 327683);
                     ifaceStaticClass.accept(classWriter);
                     writeEntry(out, ifaceStaticClass.name + ".class", classWriter.toByteArray());
                  }

                  ClassNode loaderClass = new ClassNode();
                  loaderClass.sourceFile = "Armadillo";
                  loaderClass.name = "native" + this.nativeDirId + "/Loader";
                  if (forAndroid) {
                     loaderClass.version = 50;
                  } else {
                     loaderClass.version = 52;
                  }

                  loaderClass.superName = "java/lang/Object";
                  loaderClass.access = 1;
                  MethodNode registerNativesForClassMethod;
                  if (forAndroid) {
                     registerNativesForClassMethod = new MethodNode(327680, 8, "<clinit>", "()V", (String)null, (String[])null);
                     registerNativesForClassMethod.instructions.add(new LdcInsnNode(projectName));
                     registerNativesForClassMethod.instructions.add(new MethodInsnNode(184, "java/lang/System", "loadLibrary", "(Ljava/lang/String;)V"));
                     registerNativesForClassMethod.instructions.add(new InsnNode(177));
                     loaderClass.methods.add(registerNativesForClassMethod);
                  }

                  registerNativesForClassMethod = new MethodNode(327680, 265, "registerNativesForClass", "(I)V", (String)null, new String[0]);
                  loaderClass.methods.add(registerNativesForClassMethod);
                  classWriter = new SafeClassWriter(metadataReader, 327683);
                  loaderClass.accept(classWriter);
                  writeEntry(out, "native" + this.nativeDirId + "/Loader.class", classWriter.toByteArray());
                  System.out.println("Jar file ready!");
                  if (!forAndroid) {
                     String mainClass = (String)mf.getMainAttributes().get(Name.MAIN_CLASS);
                     if (mainClass != null) {
                        System.out.println("Creating bootstrap classes...");
                        mf.getMainAttributes().put(Name.MAIN_CLASS, "native" + this.nativeDirId + "/Bootstrap");
                        ClassNode bootstrapClass = new ClassNode(327680);
                        bootstrapClass.sourceFile = "Armadillo";
                        bootstrapClass.name = "native" + this.nativeDirId + "/Bootstrap";
                        bootstrapClass.version = 52;
                        bootstrapClass.superName = "java/lang/Object";
                        bootstrapClass.access = 1;
                        MethodNode mainMethod = new MethodNode(327680, 9, "main", "([Ljava/lang/String;)V", (String)null, new String[0]);
                        mainMethod.instructions.add(new LdcInsnNode(projectName));
                        mainMethod.instructions.add(new MethodInsnNode(184, "java/lang/System", "loadLibrary", "(Ljava/lang/String;)V"));
                        mainMethod.instructions.add(new VarInsnNode(25, 0));
                        mainMethod.instructions.add(new MethodInsnNode(184, mainClass.replace(".", "/"), "main", "([Ljava/lang/String;)V"));
                        mainMethod.instructions.add(new InsnNode(177));
                        bootstrapClass.methods.add(mainMethod);
                        bootstrapClass.accept(classWriter);
                        writeEntry(out, "native" + this.nativeDirId + "/Bootstrap.class", classWriter.toByteArray());
                        System.out.println("Created!");
                     } else {
                        System.out.println("Main-Class not found - no bootstrap classes!");
                     }

                     out.putNextEntry(new ZipEntry("META-INF/MANIFEST.MF"));
                     mf.write(out);
                  }

                  out.closeEntry();
                  metadataReader.close();
                  break;
               }

               ++this.nativeDirId;
            }
         } catch (Throwable var416) {
            var17 = var416;
            throw var416;
         } finally {
            if (out != null) {
               if (var17 != null) {
                  try {
                     out.close();
                  } catch (Throwable var397) {
                     var17.addSuppressed(var397);
                  }
               } else {
                  out.close();
               }
            }

         }
      } catch (Throwable var418) {
         var15 = var418;
         throw var418;
      } finally {
         if (f != null) {
            if (var15 != null) {
               try {
                  f.close();
               } catch (Throwable var392) {
                  var15.addSuppressed(var392);
               }
            } else {
               f.close();
            }
         }

      }

      TreeMap<Integer, String> stringPoolSorted = new TreeMap();
      this.stringPool.entrySet().forEach((string) -> {
         stringPoolSorted.put(string.getValue(), string.getKey());
      });
      List<Byte> stringPoolResult = new ArrayList();
      stringPoolSorted.entrySet().forEach((string) -> {
         byte[] var2 = ((String)string.getValue()).getBytes(StandardCharsets.UTF_8);
         int var3 = var2.length;

         for(int var4 = 0; var4 < var3; ++var4) {
            byte b = var2[var4];
            stringPoolResult.add(b);
         }

         stringPoolResult.add((byte)0);
      });
      InputStream in = NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/string_pool.cpp");
      var17 = null;

      try {
         StringBuilder spValue = new StringBuilder("{ ");

         for(int i = 0; i < stringPoolResult.size(); ++i) {
            spValue.append(stringPoolResult.get(i)).append(i == stringPoolResult.size() - 1 ? "" : ", ");
         }

         spValue.append(" }");
         Files.write(outputDir.resolve("cpp").resolve("string_pool.cpp"), this.dynamicFormat(this.writeStreamToString(in), this.createMap("size", stringPoolResult.size() + "LL", "value", spValue.toString())).getBytes(StandardCharsets.UTF_8), new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING});
      } catch (Throwable var414) {
         var17 = var414;
         throw var414;
      } finally {
         if (in != null) {
            if (var17 != null) {
               try {
                  in.close();
               } catch (Throwable var399) {
                  var17.addSuppressed(var399);
               }
            } else {
               in.close();
            }
         }

      }

      if (!forAndroid) {
         in = NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/native_jvm_output.cpp");
         var17 = null;

         try {
            Files.write(outputDir.resolve("cpp").resolve("native_jvm_output.cpp"), this.dynamicFormat(this.writeStreamToString(in), this.createMap("register_code", outputHeaderSb, "includes", outputHeaderIncludesSb, "native_dir_id", this.nativeDirId, "class_count", this.currentClassId)).getBytes(StandardCharsets.UTF_8), new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING});
         } catch (Throwable var401) {
            var17 = var401;
            throw var401;
         } finally {
            if (in != null) {
               if (var17 != null) {
                  try {
                     in.close();
                  } catch (Throwable var390) {
                     var17.addSuppressed(var390);
                  }
               } else {
                  in.close();
               }
            }

         }
      } else {
         in = NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/android/native_jvm_output.cpp");
         var17 = null;

         try {
            Files.write(outputDir.resolve("cpp").resolve("native_jvm_output.cpp"), this.dynamicFormat(this.writeStreamToString(in), this.createMap("register_code", outputHeaderSb, "includes", outputHeaderIncludesSb, "native_dir_id", this.nativeDirId, "class_count", this.currentClassId)).getBytes(StandardCharsets.UTF_8), new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING});
         } catch (Throwable var400) {
            var17 = var400;
            throw var400;
         } finally {
            if (in != null) {
               if (var17 != null) {
                  try {
                     in.close();
                  } catch (Throwable var394) {
                     var17.addSuppressed(var394);
                  }
               } else {
                  in.close();
               }
            }

         }

         String application = "LOCAL_PATH:= $(call my-dir)\n\ninclude $(CLEAR_VARS)\nLOCAL_MODULE    := Armadillo\nLOCAL_CPPFLAGS += -std=c++17\nLOCAL_C_INCLUDES := $(LOCAL_PATH)/cpp\n\nLOCAL_SRC_FILES := $(wildcard $(LOCAL_PATH)/cpp/*.cpp) \\\n                   $(wildcard $(LOCAL_PATH)/cpp/*.hpp) \\\n\t\t\t\t   $(wildcard $(LOCAL_PATH)/cpp/output/*.cpp) \\\n\t\t\t\t   $(wildcard $(LOCAL_PATH)/cpp/output/*.hpp)\n\ninclude $(BUILD_SHARED_LIBRARY)\n";
         Files.write(outputDir.resolve("Android.mk"), application.getBytes(), new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING});
         application = "APP_STL := c++_static\nAPP_CPPFLAGS += -fvisibility=hidden\nAPP_PLATFORM := android-19\nAPP_ABI := armeabi-v7a arm64-v8a";
         Files.write(outputDir.resolve("Application.mk"), application.getBytes(), new OpenOption[]{StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING});
      }

   }

   private static void writeEntry(JarFile f, ZipOutputStream out, JarEntry e) throws IOException {
      out.putNextEntry(new JarEntry(e.getName()));
      InputStream in = f.getInputStream(e);
      Throwable var4 = null;

      try {
         transfer(in, out);
      } catch (Throwable var13) {
         var4 = var13;
         throw var13;
      } finally {
         if (in != null) {
            if (var4 != null) {
               try {
                  in.close();
               } catch (Throwable var12) {
                  var4.addSuppressed(var12);
               }
            } else {
               in.close();
            }
         }

      }

      out.closeEntry();
   }

   private static void writeEntry(ZipOutputStream out, String entryName, byte[] data) throws IOException {
      out.putNextEntry(new JarEntry(entryName));
      out.write(data, 0, data.length);
      out.closeEntry();
   }

   private static void transfer(InputStream in, OutputStream out) throws IOException {
      byte[] buffer = new byte[4096];

      for(int r = in.read(buffer, 0, 4096); r != -1; r = in.read(buffer, 0, 4096)) {
         out.write(buffer, 0, r);
      }

   }

   private static int byteArrayToInt(byte[] b) {
      if (b.length == 4) {
         return b[0] << 24 | (b[1] & 255) << 16 | (b[2] & 255) << 8 | b[3] & 255;
      } else {
         return b.length == 2 ? (b[0] & 255) << 8 | b[1] & 255 : 0;
      }
   }

   static {
      try {
         Field[] var6 = Opcodes.class.getFields();
         int var1 = var6.length;

         for(int var2 = 0; var2 < var1; ++var2) {
            Field f = var6[var2];
            INSTRUCTIONS.put((Integer)f.get((Object)null), f.getName());
         }

         CPP_SNIPPETS.load(NativeObfuscator.class.getClassLoader().getResourceAsStream("sources/cppsnippets.properties"));
      } catch (IllegalAccessException | IOException | IllegalArgumentException var5) {
         IllegalArgumentException ex = var5;

         try {
            throw new Exception(ex);
         } catch (Exception var4) {
            var4.printStackTrace();
         }
      }

   }
}
