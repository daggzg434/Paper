package com.verify.service.Controller;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.verify.service.ServiceApplication;
import com.verify.service.Base.BaseController;
import com.verify.service.Entity.UserInfo;
import com.verify.service.Entity.UserSoft;
import com.verify.service.Model.InjectModel;
import com.verify.service.Result.CreateSoftResult;
import com.verify.service.Result.UserSoftResult;
import com.verify.service.Utils.AutoXml;
import com.verify.service.Utils.InputToByte;
import com.verify.service.Utils.SingleRun;
import com.verify.service.Utils.ZipUtils;
import com.verify.service.Utils.axml.AutoXml.ManifestAppName;
import com.verify.service.Utils.axml.AutoXml.ManifestParse;
import com.verify.service.annotation.BasicCheck;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.Opcodes;
import org.jf.dexlib2.analysis.ClassPath;
import org.jf.dexlib2.analysis.DexClassProvider;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;
import org.jf.dexlib2.iface.DexFile;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.immutable.ImmutableClassDef;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction10x;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction35c;
import org.jf.dexlib2.writer.io.MemoryDataStore;
import org.jf.dexlib2.writer.pool.DexPool;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/apis/inject"})
@Component
public class InjectController extends BaseController {
   @Scheduled(
      cron = "0 0 4 * * *"
   )
   public void free() {
      System.out.println("定时释放状态信息");
      statelist.clear();
      runnableMap.clear();
   }

   @PostMapping({"/create_soft"})
   @ResponseBody
   @BasicCheck
   public Object create_soft(InjectModel inject, HttpServletRequest request) {
      try {
         String url = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
         if (inject.getFile() == null) {
            return new UserSoftResult(404, "文件无效");
         } else {
            UserInfo info = this.userInfoRepository.findByToken(inject.getToken());
            String uuid = UUID.randomUUID().toString();
            File temp_zip = new File(ServiceApplication.temp + File.separator + uuid + ".zip");
            FileOutputStream temp = new FileOutputStream(temp_zip);
            temp.write(InputToByte.toByte(inject.getFile().getInputStream()));
            temp.close();
            (new Thread(new InjectController.InjectRun(info, temp_zip, inject, uuid, url))).start();
            Map<Integer, ByteArrayOutputStream> map = new HashMap();
            map.put(-1, (Object)null);
            statelist.put(uuid, map);
            return new UserSoftResult(200, uuid);
         }
      } catch (Exception var9) {
         return new UserSoftResult(404, var9.getMessage());
      }
   }

   private void WritrError(Exception e, String uuid) {
      e.printStackTrace();

      try {
         StringBuffer logbuff = new StringBuffer();
         logbuff.append(e.getMessage() + "\n");
         StackTraceElement[] var4 = e.getStackTrace();
         int var5 = var4.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            StackTraceElement stackTraceElement = var4[var6];
            logbuff.append(stackTraceElement.toString() + "\n");
         }

         FileOutputStream log = new FileOutputStream(ServiceApplication.error + File.separator + uuid + ".log");
         log.write(logbuff.toString().getBytes());
         log.close();
         Map<Integer, ByteArrayOutputStream> map = new HashMap();
         ByteArrayOutputStream stream = new ByteArrayOutputStream();
         if (e.getMessage() == null) {
            stream.write("null error".getBytes());
         } else {
            stream.write(e.getMessage().getBytes());
         }

         map.put(404, stream);
         statelist.put(uuid, map);
      } catch (Exception var8) {
      }

   }

   @PostMapping({"/getstate"})
   @ResponseBody
   public CreateSoftResult getstate(@RequestParam("uuid") String uuid) {
      System.out.println("当前处理队列:" + ServiceApplication.executor.getActiveCount());
      if (statelist.containsKey(uuid)) {
         Iterator var2 = ((Map)statelist.get(uuid)).entrySet().iterator();

         while(var2.hasNext()) {
            Entry<Integer, ByteArrayOutputStream> vo = (Entry)var2.next();
            if ((Integer)vo.getKey() == -1) {
               return new CreateSoftResult((Integer)vo.getKey(), "排队中,当前队列" + ServiceApplication.executor.getQueue().size(), uuid);
            }

            if ((Integer)vo.getKey() == 0) {
               return new CreateSoftResult((Integer)vo.getKey(), "处理中...", uuid);
            }

            if ((Integer)vo.getKey() == 2) {
               return new CreateSoftResult((Integer)vo.getKey(), "任务已完成", uuid);
            }

            if ((Integer)vo.getKey() != 2) {
               ((Map)statelist.get(uuid)).clear();
               statelist.remove(uuid);
               return new CreateSoftResult((Integer)vo.getKey(), new String(((ByteArrayOutputStream)vo.getValue()).toByteArray()), uuid);
            }
         }
      }

      return new CreateSoftResult(404, "无此任务ID", uuid);
   }

   @PostMapping({"/download"})
   @ResponseBody
   public Object getstate(@RequestParam("uuid") String uuid, HttpServletResponse response) {
      if (statelist.containsKey(uuid)) {
         Iterator var3 = ((Map)statelist.get(uuid)).entrySet().iterator();

         while(var3.hasNext()) {
            Entry<Integer, ByteArrayOutputStream> vo = (Entry)var3.next();
            if ((Integer)vo.getKey() == 2) {
               try {
                  response.getOutputStream().write(((ByteArrayOutputStream)vo.getValue()).toByteArray());
                  response.getOutputStream().flush();
               } catch (IOException var6) {
                  var6.printStackTrace();
               }
            }
         }
      }

      return new CreateSoftResult(404, "无此任务ID", "");
   }

   @PostMapping({"/freed"})
   @ResponseBody
   public void freed(@RequestParam("uuid") String uuid) {
      if (statelist.containsKey(uuid)) {
         System.out.println("释放资源" + uuid);
         ((Map)statelist.get(uuid)).clear();
         statelist.remove(uuid);
      }

      if (runnableMap.containsKey(uuid)) {
         System.out.println("释放进程" + uuid);
         if (!((Future)runnableMap.get(uuid)).isDone()) {
            ((Future)runnableMap.get(uuid)).cancel(true);
         }
      }

   }

   private class InjectRun implements Runnable {
      private UserInfo info;
      private File temp_zip;
      private InjectModel model;
      private String uuid;
      private String url;

      public InjectRun(UserInfo info, File temp_zip, InjectModel model, String uuid, String url) {
         this.info = info;
         this.temp_zip = temp_zip;
         this.model = model;
         this.uuid = uuid;
         this.url = url;
      }

      public void run() {
         Map<Integer, ByteArrayOutputStream> map = new HashMap();
         map.put(0, (Object)null);
         BaseController.statelist.put(this.uuid, map);
         ZipFile zipFile = null;
         UserInfo userInfo = this.info;

         try {
            zipFile = new ZipFile(this.temp_zip);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream);
            byte[] xml = InputToByte.toByte(zipFile.getInputStream(new ZipEntry("AndroidManifest.xml")));
            byte[] ico = InputToByte.toByte(zipFile.getInputStream(new ZipEntry("ico.png")));
            String packageName = ManifestParse.parseManifestPackageName(zipFile.getInputStream(new ZipEntry("AndroidManifest.xml")));
            int Ver = ManifestParse.parseManifestVer(zipFile.getInputStream(new ZipEntry("AndroidManifest.xml")));
            ByteArrayOutputStream conf = new ByteArrayOutputStream();
            Properties properties = new Properties();
            List<String> dexlist = new ArrayList();
            Enumeration enumeration = zipFile.entries();

            while(enumeration.hasMoreElements()) {
               ZipEntry zipEntry = (ZipEntry)enumeration.nextElement();
               if (zipEntry.getName().startsWith("classes") && zipEntry.getName().endsWith(".dex")) {
                  dexlist.add(zipEntry.getName());
               }
            }

            DexPool dexPool;
            byte[] outxml;
            if (this.model.isGlobal()) {
               System.out.println("全局模式处理中......");
               outxml = ManifestAppName.parseManifest(new ByteArrayInputStream(xml), InjectController.this.ApplicationName);
               outxml = AutoXml.Auto(outxml, "inject.xml", properties);
               ZipUtils.putNextEntry(zipOutputStream, "AndroidManifest.xml", outxml);
               if (ManifestAppName.customApplication) {
                  DexBackedDexFile dexBackedDexFile = DexBackedDexFile.fromInputStream(Opcodes.getDefault(), new ByteArrayInputStream(InputToByte.toByte(new FileInputStream(ServiceApplication.temp_static + File.separator + "inject.dex"))));
                  ClassDef classDef = (new ClassPath(Lists.newArrayList(new DexClassProvider[]{new DexClassProvider(dexBackedDexFile)}), false, dexBackedDexFile.getClasses().size())).getClassDef("L" + InjectController.this.ApplicationName.replace(".", "/") + ";");
                  List<Method> newMethod = new ArrayList();
                  classDef.getMethods().forEach((method) -> {
                     ArrayList newInsts;
                     int regIndex;
                     if ("onCreate".equals(method.getName())) {
                        newInsts = new ArrayList();
                        regIndex = method.getImplementation().getRegisterCount();
                        newInsts.add(new ImmutableInstruction35c(Opcode.INVOKE_DIRECT, 1, regIndex - 1, 0, 0, 0, 0, new ImmutableMethod("Lcrc642c631ec79b49b81e/App;", "n_onCreate", (ImmutableList)null, "V", AccessFlags.PUBLIC.getValue(), (ImmutableSet)null, (ImmutableMethodImplementation)null)));
                        newInsts.add(new ImmutableInstruction35c(Opcode.INVOKE_SUPER, 1, regIndex - 1, 0, 0, 0, 0, new ImmutableMethod("L" + ManifestAppName.customApplicationName.replace(".", "/") + ";", "onCreate", (ImmutableList)null, "V", AccessFlags.PUBLIC.getValue(), (ImmutableSet)null, (ImmutableMethodImplementation)null)));
                        newInsts.add(new ImmutableInstruction10x(Opcode.RETURN_VOID));
                        ImmutableMethodImplementation newmix = new ImmutableMethodImplementation(regIndex, newInsts, method.getImplementation().getTryBlocks(), method.getImplementation().getDebugItems());
                        newMethod.add(new ImmutableMethod(method.getDefiningClass(), method.getName(), method.getParameters(), method.getReturnType(), method.getAccessFlags(), method.getAnnotations(), newmix));
                     } else if ("<init>".equals(method.getName())) {
                        newInsts = new ArrayList();
                        regIndex = method.getImplementation().getRegisterCount();
                        newInsts.add(new ImmutableInstruction35c(Opcode.INVOKE_DIRECT, 1, regIndex - 1, 0, 0, 0, 0, new ImmutableMethod("L" + ManifestAppName.customApplicationName.replace(".", "/") + ";", "<init>", (ImmutableList)null, "V", AccessFlags.PUBLIC.getValue(), (ImmutableSet)null, (ImmutableMethodImplementation)null)));
                        List<MethodParameter> params = new ArrayList();
                        params.add(new ImmutableMethodParameter("Landroid/content/Context;", (ImmutableSet)null, (String)null));
                        ImmutableMethod immutableMethod = new ImmutableMethod("Lmono/MonoPackageManager;", "setContext", params, "V", AccessFlags.PUBLIC.getValue(), (Set)null, (MethodImplementation)null);
                        newInsts.add(new ImmutableInstruction35c(Opcode.INVOKE_STATIC, 1, regIndex - 1, 0, 0, 0, 0, immutableMethod));
                        newInsts.add(new ImmutableInstruction10x(Opcode.RETURN_VOID));
                        ImmutableMethodImplementation newmi = new ImmutableMethodImplementation(regIndex, newInsts, method.getImplementation().getTryBlocks(), method.getImplementation().getDebugItems());
                        newMethod.add(new ImmutableMethod(method.getDefiningClass(), method.getName(), method.getParameters(), method.getReturnType(), method.getAccessFlags(), method.getAnnotations(), newmi));
                     } else {
                        newMethod.add(method);
                     }

                  });
                  ClassDef classDefx = new ImmutableClassDef(classDef.getType(), classDef.getAccessFlags(), "L" + ManifestAppName.customApplicationName.replace(".", "/") + ";", classDef.getInterfaces(), classDef.getSourceFile(), classDef.getAnnotations(), classDef.getFields(), newMethod);
                  DexPool dexPoolx = new DexPool(Opcodes.getDefault());
                  dexPoolx.internClass(classDefx);
                  dexBackedDexFile.getClasses().forEach((defx) -> {
                     if (!("L" + InjectController.this.ApplicationName.replace(".", "/") + ";").equals(defx.getType())) {
                        dexPoolx.internClass(defx);
                     }

                  });
                  MemoryDataStore dataStore = new MemoryDataStore();
                  dexPoolx.writeTo(dataStore);
                  byte[] out = Arrays.copyOf(dataStore.getBuffer(), dataStore.getSize());
                  ZipUtils.putNextEntry(zipOutputStream, "classes" + (dexlist.size() + 1) + ".dex", out);
               } else {
                  ZipUtils.putNextEntry(zipOutputStream, "classes" + (dexlist.size() + 1) + ".dex", InputToByte.toByte(new FileInputStream(ServiceApplication.temp_static + File.separator + "inject.dex")));
               }
            } else if (this.model.getSingle().isEmpty()) {
               if (this.model.isActivity()) {
                  System.out.println("页面模式处理中......");
                  outxml = AutoXml.Auto(xml, "activity.xml", properties);
                  ZipUtils.putNextEntry(zipOutputStream, "AndroidManifest.xml", outxml);
                  ZipUtils.putNextEntry(zipOutputStream, "classes" + (dexlist.size() + 1) + ".dex", InputToByte.toByte(new FileInputStream(ServiceApplication.temp_static + File.separator + "inject.dex")));
               }
            } else {
               System.out.println("单例模式处理中......");
               outxml = AutoXml.Auto(xml, "inject.xml", properties);
               ZipUtils.putNextEntry(zipOutputStream, "AndroidManifest.xml", outxml);
               dexPool = new DexPool(Opcodes.getDefault());
               boolean flag = false;
               ClassDef curr_ClassDef = null;
               ClassDef newClassDef = null;
               String curr_clz = "L" + this.model.getSingle().replace(".", "/") + ";";
               String curr_dex = null;
               List<Method> newMethodList = new ArrayList();
               HashMap<Integer, HashSet<ClassDef>> dexmap = new HashMap();
               HashSet<ClassDef> classDefs = new HashSet();

               for(int i = 0; i < dexlist.size(); ++i) {
                  DexFile dexBackedDexFile_temp = DexBackedDexFile.fromInputStream(Opcodes.getDefault(), new BufferedInputStream(new BufferedInputStream(zipFile.getInputStream(new ZipEntry((String)dexlist.get(i))))));
                  classDefs.addAll(dexBackedDexFile_temp.getClasses());
                  HashSet<ClassDef> tmp = new HashSet();
                  tmp.addAll(dexBackedDexFile_temp.getClasses());
                  dexmap.put(i, tmp);
               }

               List<ClassDef> temp = new ArrayList(classDefs);
               int ix = 0;

               while(true) {
                  if (ix >= temp.size()) {
                     if (curr_ClassDef == null) {
                        throw new Exception("ClassDef Not Find");
                     }

                     Iterator iter = dexmap.entrySet().iterator();

                     while(iter.hasNext()) {
                        Entry entry = (Entry)iter.next();
                        HashSet<ClassDef> c = (HashSet)entry.getValue();
                        if (c.contains(curr_ClassDef)) {
                           curr_dex = (String)dexlist.get((Integer)entry.getKey());
                        }
                     }

                     if (!flag) {
                        throw new Exception("onCreate Not Find");
                     }

                     newClassDef = new ImmutableClassDef(curr_ClassDef.getType(), curr_ClassDef.getAccessFlags(), curr_ClassDef.getSuperclass(), curr_ClassDef.getInterfaces(), curr_ClassDef.getSourceFile(), curr_ClassDef.getAnnotations(), curr_ClassDef.getFields(), newMethodList);
                     dexPool.internClass(newClassDef);
                     MemoryDataStore dataStorex = new MemoryDataStore();
                     dexPool.writeTo(dataStorex);
                     byte[] outx = Arrays.copyOf(dataStorex.getBuffer(), dataStorex.getSize());
                     ZipUtils.putNextEntry(zipOutputStream, curr_dex, outx);
                     ZipUtils.putNextEntry(zipOutputStream, "classes" + (dexlist.size() + 1) + ".dex", InputToByte.toByte(new FileInputStream(ServiceApplication.temp_static + File.separator + "inject.dex")));
                     break;
                  }

                  ClassDef def = (ClassDef)temp.get(ix);
                  if (def.getType().equals(curr_clz)) {
                     List<String> method_name = new ArrayList();
                     def.getMethods().forEach((met) -> {
                        method_name.add(met.getName());
                     });
                     if (method_name.contains("onCreate")) {
                        curr_ClassDef = def;
                        flag = true;
                        Iterator var27 = def.getMethods().iterator();

                        while(var27.hasNext()) {
                           Method m = (Method)var27.next();
                           if (m.getName().equals("onCreate")) {
                              newMethodList.add(SingleRun.Auto(m));
                           } else {
                              newMethodList.add(m);
                           }
                        }
                     } else {
                        curr_clz = def.getSuperclass();
                        ix = 0;
                     }
                  }

                  ++ix;
               }
            }

            if (ico != null && packageName != null) {
               FileOutputStream ico_out = new FileOutputStream(ServiceApplication.ico + File.separator + this.uuid);
               ico_out.write(ico);
               ico_out.close();
            }

            UserSoft soft = InjectController.this.userSoftRepository.findBySoftpackagenameAndUserid(packageName, this.info.getId());
            dexPool = null;
            String Appkey;
            if (soft != null) {
               Appkey = soft.getAppkey();
               soft.setIco_name(this.uuid);
               soft.setVersion(Ver);
               InjectController.this.userSoftRepository.save(soft);
            } else {
               soft = new UserSoft();
               soft.setSoftpackagename(packageName);
               soft.setSoft_name(this.model.getName());
               soft.setIco_name(this.uuid);
               soft.setUserid(this.info.getId());
               soft.setVersion(Ver);
               Appkey = soft.getAppkey();
               InjectController.this.userSoftRepository.save(soft);
            }

            properties.setProperty("AppKey", Appkey);
            properties.setProperty("Ver", "" + Ver);
            properties.setProperty("Url", this.url);
            properties.store(conf, InjectController.this.hello);
            ZipUtils.putNextEntry(zipOutputStream, "assets/Conf.dat", Base64.getEncoder().encode(conf.toByteArray()));
            zipOutputStream.close();
            Map<Integer, ByteArrayOutputStream> mapx = new HashMap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write(outputStream.toByteArray());
            mapx.put(2, stream);
            BaseController.statelist.put(this.uuid, mapx);
            userInfo.setInject_count(this.info.getInject_count() + 1);
            InjectController.this.userInfoRepository.save(userInfo);
         } catch (ZipException var45) {
            InjectController.this.WritrError(var45, this.uuid);
         } catch (FileNotFoundException var46) {
            InjectController.this.WritrError(var46, this.uuid);
         } catch (IOException var47) {
            InjectController.this.WritrError(var47, this.uuid);
         } catch (InterruptedException var48) {
            var48.printStackTrace();
         } catch (Exception var49) {
            InjectController.this.WritrError(var49, this.uuid);
         } finally {
            BaseController.runnableMap.remove(this.uuid);

            try {
               zipFile.close();
               this.temp_zip.delete();
            } catch (IOException var44) {
               var44.printStackTrace();
            }

         }

      }
   }
}
