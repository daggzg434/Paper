package com.verify.service.Controller;

import by.radioegor146.NativeObfuscator;
import com.verify.service.ServiceApplication;
import com.verify.service.Base.BaseController;
import com.verify.service.Entity.UserInfo;
import com.verify.service.Model.DexStringEncryModel;
import com.verify.service.Result.UserSoftResult;
import com.verify.service.Utils.Dex2Jar;
import com.verify.service.Utils.InputToByte;
import com.verify.service.annotation.BasicCheck;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@RestController
@RequestMapping({"/apis/dex"})
public class DexObfController extends BaseController {
   @PostMapping({"/string_en"})
   @BasicCheck
   @ResponseBody
   public Object string_en(DexStringEncryModel stringEncryModel) {
      try {
         if (stringEncryModel.getFile() == null) {
            return new UserSoftResult(404, "文件无效");
         } else {
            UserInfo info = this.userInfoRepository.findByToken(stringEncryModel.getToken());
            if (info.getName().equals(this.test_username)) {
               return new UserSoftResult(404, "抱歉测试账号无法使用此功能");
            } else {
               String uuid = UUID.randomUUID().toString();
               File temp_zip = new File(ServiceApplication.temp + File.separator + uuid + ".zip");
               FileOutputStream temp = new FileOutputStream(temp_zip);
               temp.write(InputToByte.toByte(stringEncryModel.getFile().getInputStream()));
               temp.close();
               Future future = ServiceApplication.executor.submit(new DexObfController.StringEncryRun(uuid, temp_zip, stringEncryModel));
               runnableMap.put(uuid, future);
               Map<Integer, ByteArrayOutputStream> map = new HashMap();
               map.put(-1, (Object)null);
               statelist.put(uuid, map);
               return new UserSoftResult(200, uuid);
            }
         }
      } catch (Exception var8) {
         return new UserSoftResult(404, var8.getMessage());
      }
   }

   private void WriteSo(File dir, ZipOutputStream zipOutputStream) throws IOException {
      File[] var3 = dir.listFiles();
      int var4 = var3.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         File so = var3[var5];
         if (so.isDirectory()) {
            this.WriteSo(so, zipOutputStream);
         } else if (so.isFile()) {
            zipOutputStream.putNextEntry(new ZipEntry("lib/" + (new File(so.getParent())).getName() + "/" + so.getName()));
            zipOutputStream.write(InputToByte.toByte(new FileInputStream(so)));
            zipOutputStream.closeEntry();
         }
      }

   }

   private void WriteError(Exception e, String uuid) {
      e.printStackTrace();

      try {
         Map<Integer, ByteArrayOutputStream> map = new HashMap();
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         if (e.getMessage() == null) {
            stream.write("null error".getBytes());
         } else {
            stream.write(e.getMessage().getBytes());
         }

         map.put(404, stream);
         statelist.put(uuid, map);
      } catch (Exception var5) {
      }

   }

   private String createPro(File in, File out) {
      StringBuffer buffer = new StringBuffer();
      buffer.append("-injars " + in.getAbsolutePath() + "\n");
      buffer.append("-outjars " + out.getAbsolutePath() + "\n");
      buffer.append("-keep class ** {*;}\n");
      buffer.append("-verbose\n");
      buffer.append("-android\n");
      buffer.append("-dontoptimize\n");
      buffer.append("-encryptstrings **\n");
      buffer.append("-ignorewarnings\n");
      buffer.append("-dontwarn **\n");
      buffer.append("-allowaccessmodification\n");
      buffer.append("-forceprocessing\n");
      return buffer.toString();
   }

   private void createDexGuardEnClassPro(File in, File out, String config, File out_config) throws IOException {
      StringBuffer buffer = new StringBuffer();
      List<String> keep = new ArrayList();
      String[] var7 = config.split("\n");
      int var8 = var7.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         String conf = var7[var9];
         buffer.append("-encryptclasses " + conf + "\n");
         keep.add(conf.replace(".", "/") + ".class");
      }

      ZipFile zipFile = new ZipFile(in);
      Enumeration srcEntries = zipFile.entries();

      while(srcEntries.hasMoreElements()) {
         ZipEntry srcentry = (ZipEntry)srcEntries.nextElement();
         if (!srcentry.isDirectory() && srcentry.getName().endsWith(".class") && !keep.contains(srcentry.getName())) {
            buffer.append("-keep class " + srcentry.getName().replace("/", ".").replace(".class", "") + " {*;}\n");
         }
      }

      buffer.append("-injars " + in.getAbsolutePath() + "\n");
      buffer.append("-outjars " + out.getAbsolutePath() + "\n");
      buffer.append("-android\n");
      buffer.append("-dontshrink\n");
      buffer.append("-dontoptimize\n");
      buffer.append("-ignorewarnings\n");
      buffer.append("-dontwarn **\n");
      buffer.append("-allowaccessmodification\n");
      buffer.append("-forceprocessing\n");
      FileOutputStream fileOutputStream = new FileOutputStream(out_config);
      fileOutputStream.write(buffer.toString().getBytes());
      fileOutputStream.close();
   }

   private void createAllatoriXml(File config, File in, File out) throws ParserConfigurationException, TransformerException {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      DocumentBuilder db = factory.newDocumentBuilder();
      Document document = db.newDocument();
      document.setXmlStandalone(true);
      Element root = document.createElement("config");
      Element Input = document.createElement("input");
      Element Keep = document.createElement("jar");
      Keep.setAttribute("in", in.getAbsolutePath());
      Keep.setAttribute("out", out.getAbsolutePath());
      Input.appendChild(Keep);
      Keep = document.createElement("keep-names");
      Element String_encryption = document.createElement("class");
      String_encryption.setAttribute("template", "class *");
      Element String_encryption_type = document.createElement("field");
      String_encryption_type.setAttribute("template", "*");
      Element String_encryption_version = document.createElement("method");
      String_encryption_version.setAttribute("template", "*(**)");
      Keep.appendChild(String_encryption);
      Keep.appendChild(String_encryption_type);
      Keep.appendChild(String_encryption_version);
      String_encryption = document.createElement("property");
      String_encryption.setAttribute("name", "string-encryption");
      String_encryption.setAttribute("value", "maximum-with-warnings");
      String_encryption_type = document.createElement("property");
      String_encryption_type.setAttribute("name", "string-encryption-type");
      String_encryption_type.setAttribute("value", "strong");
      String_encryption_version = document.createElement("property");
      String_encryption_version.setAttribute("name", "string-encryption-version");
      String_encryption_version.setAttribute("value", "v4");
      Element Control_flow_obfuscation = document.createElement("property");
      Control_flow_obfuscation.setAttribute("name", "control-flow-obfuscation");
      Control_flow_obfuscation.setAttribute("value", "enable");
      Element Extensive_flow_obfuscation = document.createElement("property");
      Extensive_flow_obfuscation.setAttribute("name", "extensive-flow-obfuscation");
      Extensive_flow_obfuscation.setAttribute("value", "maximum");
      root.appendChild(Input);
      root.appendChild(Keep);
      root.appendChild(String_encryption);
      root.appendChild(String_encryption_type);
      root.appendChild(String_encryption_version);
      root.appendChild(Control_flow_obfuscation);
      root.appendChild(Extensive_flow_obfuscation);
      document.appendChild(root);
      TransformerFactory tff = TransformerFactory.newInstance();
      Transformer tf = tff.newTransformer();
      tf.setOutputProperty("indent", "yes");
      tf.transform(new DOMSource(document), new StreamResult(config));
   }

   public synchronized boolean exec(String[] command) throws IOException, InterruptedException {
      Process process = Runtime.getRuntime().exec(command);
      BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

      while(in.readLine() != null) {
      }

      BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      String line = null;

      while((line = buffer.readLine()) != null) {
         System.out.println(line);
      }

      int exit = process.waitFor();
      if (exit == 0) {
         return true;
      } else {
         return exit != 0 ? false : false;
      }
   }

   public synchronized boolean exec(String command) throws IOException, InterruptedException {
      Process process = Runtime.getRuntime().exec(command);
      BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));

      while(in.readLine() != null) {
      }

      BufferedReader buffer = new BufferedReader(new InputStreamReader(process.getErrorStream()));
      String line = null;

      while((line = buffer.readLine()) != null) {
         System.out.println(line);
      }

      int exit = process.waitFor();
      if (exit == 0) {
         return true;
      } else {
         return exit != 0 ? false : false;
      }
   }

   private class StringEncryRun implements Callable<Void> {
      public String uuid;
      private File temp_file;
      private DexStringEncryModel model;
      private List<File> Jars = new ArrayList();
      private List<File> outJars = new ArrayList();
      private List<File> outDexs = new ArrayList();

      public StringEncryRun(String uuid, File temp_file, DexStringEncryModel model) {
         this.uuid = uuid;
         this.temp_file = temp_file;
         this.model = model;
      }

      public Void call() {
         Map<Integer, ByteArrayOutputStream> map = new HashMap();
         map.put(0, (Object)null);
         BaseController.statelist.put(this.uuid, map);
         ZipFile zipFile = null;
         boolean var30 = false;

         Iterator var49;
         File dexx;
         label1014: {
            label1015: {
               label1016: {
                  label1017: {
                     label1018: {
                        label1019: {
                           label1020: {
                              try {
                                 var30 = true;
                                 zipFile = new ZipFile(this.temp_file);
                                 ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                 ZipOutputStream zipOutputStream = new ZipOutputStream(byteArrayOutputStream);
                                 List<String> dexlist = new ArrayList();
                                 Enumeration enumeration = zipFile.entries();

                                 while(enumeration.hasMoreElements()) {
                                    ZipEntry zipEntry = (ZipEntry)enumeration.nextElement();
                                    if (zipEntry.getName().startsWith("classes") && zipEntry.getName().endsWith(".dex")) {
                                       dexlist.add(zipEntry.getName());
                                    }
                                 }

                                 if (this.model.getEncry_type() == 4) {
                                    if (dexlist.size() > 1) {
                                       throw new Exception("当前不支持多Dex包进行加固");
                                    }

                                    if (this.model.getConfig().isEmpty()) {
                                       throw new ParserConfigurationException("未配置加密Class规则");
                                    }

                                    if (this.model.getConfig().split("\n").length > 50) {
                                       throw new ParserConfigurationException("需要加固的类过多，目前由于服务器机能限制最多加固50个类");
                                    }
                                 }

                                 Iterator var51 = dexlist.iterator();

                                 while(var51.hasNext()) {
                                    String dex = (String)var51.next();
                                    Dex2Jar.from(zipFile.getInputStream(new ZipEntry(dex)), new File(this.temp_file.getAbsolutePath() + dex + ".jar"));
                                    this.Jars.add(new File(this.temp_file.getAbsolutePath() + dex + ".jar"));
                                 }

                                 File jarx;
                                 File config;
                                 File outJar;
                                 label941:
                                 switch(this.model.getEncry_type()) {
                                 case 1:
                                    var51 = this.Jars.iterator();

                                    while(true) {
                                       if (!var51.hasNext()) {
                                          break label941;
                                       }

                                       jarx = (File)var51.next();
                                       config = new File(this.temp_file.getAbsolutePath() + "-success.jar");
                                       if (!DexObfController.this.exec(new String[]{"java", "-jar", ServiceApplication.temp_static + File.separator + "dexguard.jar", DexObfController.this.createPro(jarx, config)})) {
                                          throw new Exception("加密失败");
                                       }

                                       this.outJars.add(config);
                                    }
                                 case 2:
                                    var51 = this.Jars.iterator();

                                    while(true) {
                                       if (!var51.hasNext()) {
                                          break label941;
                                       }

                                       jarx = (File)var51.next();
                                       config = new File(jarx.getAbsolutePath() + "-success.jar");
                                       outJar = new File(jarx.getAbsolutePath() + ".xml");
                                       DexObfController.this.createAllatoriXml(outJar, jarx, config);
                                       if (!DexObfController.this.exec(new String[]{"java", "-jar", ServiceApplication.temp_static + File.separator + "allatori.jar", outJar.getAbsolutePath()})) {
                                          outJar.delete();
                                          throw new Exception("加密失败");
                                       }

                                       this.outJars.add(config);
                                       outJar.delete();
                                    }
                                 case 3:
                                    if (this.model.getConfig().isEmpty()) {
                                       throw new ParserConfigurationException("未配置加密Class规则");
                                    }

                                    var51 = this.Jars.iterator();

                                    while(true) {
                                       if (!var51.hasNext()) {
                                          break label941;
                                       }

                                       jarx = (File)var51.next();
                                       config = new File(this.temp_file.getAbsolutePath() + ".pro");
                                       outJar = new File(this.temp_file.getAbsolutePath() + "-success.jar");
                                       DexObfController.this.createDexGuardEnClassPro(jarx, outJar, this.model.getConfig(), config);
                                       if (!DexObfController.this.exec(new String[]{"java", "-jar", ServiceApplication.temp_static + File.separator + "dexguard.jar", "-include " + config.getAbsolutePath()})) {
                                          config.delete();
                                          throw new Exception("加密失败");
                                       }

                                       this.outJars.add(outJar);
                                       config.delete();
                                    }
                                 case 4:
                                    System.out.println("开始加固");
                                    File jar = (File)this.Jars.get(0);
                                    NativeObfuscator instance = new NativeObfuscator();
                                    String out = jar.getParent() + File.separator + this.uuid + File.separator + "jni";
                                    instance.process(Paths.get(jar.getAbsolutePath()), Paths.get(out), new ArrayList(), true, this.model.getConfig());
                                    if (!DexObfController.this.exec(new String[]{"ndk-build", "-C", (new File(out)).getAbsolutePath()})) {
                                       throw new Exception("加固失败");
                                    }

                                    String libs = jar.getParent() + File.separator + this.uuid + File.separator + "libs";
                                    if ((new File(libs)).exists()) {
                                       this.outJars.add(new File(out + File.separator + jar.getName()));
                                       DexObfController.this.WriteSo(new File(libs), zipOutputStream);
                                    }
                                    break;
                                 default:
                                    throw new Exception("无效模式");
                                 }

                                 var51 = this.outJars.iterator();

                                 while(var51.hasNext()) {
                                    jarx = (File)var51.next();
                                    if (!DexObfController.this.exec(new String[]{"java", "-jar", ServiceApplication.temp_static + File.separator + "dx.jar", "--dex", "--no-strict", "--output=" + jarx.getAbsolutePath() + ".dex", jarx.getAbsolutePath()})) {
                                       throw new Exception("Dex编译错误");
                                    }

                                    this.outDexs.add(new File(jarx.getAbsolutePath() + ".dex"));
                                 }

                                 for(int i = 0; i < this.outDexs.size(); ++i) {
                                    if (i != 0) {
                                       zipOutputStream.putNextEntry(new ZipEntry("classes" + i + ".dex"));
                                    } else {
                                       zipOutputStream.putNextEntry(new ZipEntry("classes.dex"));
                                    }

                                    zipOutputStream.write(InputToByte.toByte(new FileInputStream((File)this.outDexs.get(i))));
                                    zipOutputStream.closeEntry();
                                 }

                                 zipOutputStream.close();
                                 Map<Integer, ByteArrayOutputStream> mapx = new HashMap();
                                 ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                 stream.write(byteArrayOutputStream.toByteArray());
                                 mapx.put(2, stream);
                                 BaseController.statelist.put(this.uuid, mapx);
                                 var30 = false;
                                 break label1014;
                              } catch (InterruptedException var40) {
                                 var40.printStackTrace();
                                 var30 = false;
                              } catch (ZipException var41) {
                                 DexObfController.this.WriteError(var41, this.uuid);
                                 var30 = false;
                                 break label1020;
                              } catch (FileNotFoundException var42) {
                                 DexObfController.this.WriteError(var42, this.uuid);
                                 var30 = false;
                                 break label1019;
                              } catch (ParserConfigurationException var43) {
                                 DexObfController.this.WriteError(var43, this.uuid);
                                 var30 = false;
                                 break label1018;
                              } catch (IOException var44) {
                                 DexObfController.this.WriteError(var44, this.uuid);
                                 var30 = false;
                                 break label1017;
                              } catch (TransformerException var45) {
                                 DexObfController.this.WriteError(var45, this.uuid);
                                 var30 = false;
                                 break label1016;
                              } catch (Exception var46) {
                                 DexObfController.this.WriteError(var46, this.uuid);
                                 var30 = false;
                                 break label1015;
                              } finally {
                                 if (var30) {
                                    BaseController.runnableMap.remove(this.uuid);
                                    if (zipFile != null) {
                                       try {
                                          zipFile.close();
                                       } catch (IOException var31) {
                                          var31.printStackTrace();
                                       }
                                    }

                                    Iterator var11 = this.Jars.iterator();

                                    File dexxx;
                                    while(var11.hasNext()) {
                                       dexxx = (File)var11.next();
                                       dexxx.delete();
                                    }

                                    var11 = this.outJars.iterator();

                                    while(var11.hasNext()) {
                                       dexxx = (File)var11.next();
                                       dexxx.delete();
                                    }

                                    var11 = this.outDexs.iterator();

                                    while(var11.hasNext()) {
                                       dexxx = (File)var11.next();
                                       dexxx.delete();
                                    }

                                    this.temp_file.delete();
                                 }
                              }

                              BaseController.runnableMap.remove(this.uuid);
                              if (zipFile != null) {
                                 try {
                                    zipFile.close();
                                 } catch (IOException var38) {
                                    var38.printStackTrace();
                                 }
                              }

                              var49 = this.Jars.iterator();

                              while(var49.hasNext()) {
                                 dexx = (File)var49.next();
                                 dexx.delete();
                              }

                              var49 = this.outJars.iterator();

                              while(var49.hasNext()) {
                                 dexx = (File)var49.next();
                                 dexx.delete();
                              }

                              var49 = this.outDexs.iterator();

                              while(var49.hasNext()) {
                                 dexx = (File)var49.next();
                                 dexx.delete();
                              }

                              this.temp_file.delete();
                              return null;
                           }

                           BaseController.runnableMap.remove(this.uuid);
                           if (zipFile != null) {
                              try {
                                 zipFile.close();
                              } catch (IOException var37) {
                                 var37.printStackTrace();
                              }
                           }

                           var49 = this.Jars.iterator();

                           while(var49.hasNext()) {
                              dexx = (File)var49.next();
                              dexx.delete();
                           }

                           var49 = this.outJars.iterator();

                           while(var49.hasNext()) {
                              dexx = (File)var49.next();
                              dexx.delete();
                           }

                           var49 = this.outDexs.iterator();

                           while(var49.hasNext()) {
                              dexx = (File)var49.next();
                              dexx.delete();
                           }

                           this.temp_file.delete();
                           return null;
                        }

                        BaseController.runnableMap.remove(this.uuid);
                        if (zipFile != null) {
                           try {
                              zipFile.close();
                           } catch (IOException var36) {
                              var36.printStackTrace();
                           }
                        }

                        var49 = this.Jars.iterator();

                        while(var49.hasNext()) {
                           dexx = (File)var49.next();
                           dexx.delete();
                        }

                        var49 = this.outJars.iterator();

                        while(var49.hasNext()) {
                           dexx = (File)var49.next();
                           dexx.delete();
                        }

                        var49 = this.outDexs.iterator();

                        while(var49.hasNext()) {
                           dexx = (File)var49.next();
                           dexx.delete();
                        }

                        this.temp_file.delete();
                        return null;
                     }

                     BaseController.runnableMap.remove(this.uuid);
                     if (zipFile != null) {
                        try {
                           zipFile.close();
                        } catch (IOException var35) {
                           var35.printStackTrace();
                        }
                     }

                     var49 = this.Jars.iterator();

                     while(var49.hasNext()) {
                        dexx = (File)var49.next();
                        dexx.delete();
                     }

                     var49 = this.outJars.iterator();

                     while(var49.hasNext()) {
                        dexx = (File)var49.next();
                        dexx.delete();
                     }

                     var49 = this.outDexs.iterator();

                     while(var49.hasNext()) {
                        dexx = (File)var49.next();
                        dexx.delete();
                     }

                     this.temp_file.delete();
                     return null;
                  }

                  BaseController.runnableMap.remove(this.uuid);
                  if (zipFile != null) {
                     try {
                        zipFile.close();
                     } catch (IOException var34) {
                        var34.printStackTrace();
                     }
                  }

                  var49 = this.Jars.iterator();

                  while(var49.hasNext()) {
                     dexx = (File)var49.next();
                     dexx.delete();
                  }

                  var49 = this.outJars.iterator();

                  while(var49.hasNext()) {
                     dexx = (File)var49.next();
                     dexx.delete();
                  }

                  var49 = this.outDexs.iterator();

                  while(var49.hasNext()) {
                     dexx = (File)var49.next();
                     dexx.delete();
                  }

                  this.temp_file.delete();
                  return null;
               }

               BaseController.runnableMap.remove(this.uuid);
               if (zipFile != null) {
                  try {
                     zipFile.close();
                  } catch (IOException var33) {
                     var33.printStackTrace();
                  }
               }

               var49 = this.Jars.iterator();

               while(var49.hasNext()) {
                  dexx = (File)var49.next();
                  dexx.delete();
               }

               var49 = this.outJars.iterator();

               while(var49.hasNext()) {
                  dexx = (File)var49.next();
                  dexx.delete();
               }

               var49 = this.outDexs.iterator();

               while(var49.hasNext()) {
                  dexx = (File)var49.next();
                  dexx.delete();
               }

               this.temp_file.delete();
               return null;
            }

            BaseController.runnableMap.remove(this.uuid);
            if (zipFile != null) {
               try {
                  zipFile.close();
               } catch (IOException var32) {
                  var32.printStackTrace();
               }
            }

            var49 = this.Jars.iterator();

            while(var49.hasNext()) {
               dexx = (File)var49.next();
               dexx.delete();
            }

            var49 = this.outJars.iterator();

            while(var49.hasNext()) {
               dexx = (File)var49.next();
               dexx.delete();
            }

            var49 = this.outDexs.iterator();

            while(var49.hasNext()) {
               dexx = (File)var49.next();
               dexx.delete();
            }

            this.temp_file.delete();
            return null;
         }

         BaseController.runnableMap.remove(this.uuid);
         if (zipFile != null) {
            try {
               zipFile.close();
            } catch (IOException var39) {
               var39.printStackTrace();
            }
         }

         var49 = this.Jars.iterator();

         while(var49.hasNext()) {
            dexx = (File)var49.next();
            dexx.delete();
         }

         var49 = this.outJars.iterator();

         while(var49.hasNext()) {
            dexx = (File)var49.next();
            dexx.delete();
         }

         var49 = this.outDexs.iterator();

         while(var49.hasNext()) {
            dexx = (File)var49.next();
            dexx.delete();
         }

         this.temp_file.delete();
         return null;
      }
   }
}
