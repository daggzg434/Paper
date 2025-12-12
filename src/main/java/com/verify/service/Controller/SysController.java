package com.verify.service.Controller;

import com.verify.service.Base.BaseController;
import com.verify.service.Entity.SysConfig;
import com.verify.service.Entity.SysRecard;
import com.verify.service.Entity.SysUpdate;
import com.verify.service.Entity.UserInfo;
import com.verify.service.Result.BaseResult;
import com.verify.service.Result.LzyResult;
import com.verify.service.Result.SysConfigResult;
import com.verify.service.Result.SysRecardResult;
import com.verify.service.Result.SysUpdateResult;
import com.verify.service.Result.SysUserInfoResult;
import com.verify.service.Result.UpdateResult;
import com.verify.service.annotation.AuthCheck;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class SysController extends BaseController {
   @RequestMapping({"/Sys"})
   @AuthCheck
   public String index(Model model) {
      model.addAttribute("name", this.hello);
      return "pages/index";
   }

   @RequestMapping({"/Sys/UserManage"})
   @AuthCheck
   public String UserManage(Model model) {
      model.addAttribute("name", this.hello);
      return "pages/user";
   }

   @RequestMapping({"/Sys/CreateRecard"})
   @AuthCheck
   public String CreateRecard(Model model) {
      model.addAttribute("name", this.hello);
      return "pages/create_recard";
   }

   @RequestMapping({"/Sys/RecardManage"})
   @AuthCheck
   public String RecardManage(Model model) {
      model.addAttribute("name", this.hello);
      return "pages/recard";
   }

   @RequestMapping({"/Sys/Config"})
   @AuthCheck
   public String SysConfig(Model model) {
      model.addAttribute("name", this.hello);
      List<SysConfig> list = this.sysConfigRepository.findAll();
      if (list.size() > 0) {
         SysConfig config = (SysConfig)list.get(0);
         if (config != null) {
            model.addAttribute("buy_url", config.getBuy_url());
            model.addAttribute("about", config.getAbout());
            model.addAttribute("help", config.getHelp());
            model.addAttribute("update_msg", config.getUpdate_msg());
            model.addAttribute("share", config.getShare());
            model.addAttribute("group", config.getGroup());
            model.addAttribute("full_url", config.getFull_url());
            model.addAttribute("hot_fix_url", config.getHot_fix_url());
            model.addAttribute("x86full_url", config.getX86full_url());
            model.addAttribute("x86hot_fix_url", config.getX86hot_fix_url());
         }
      }

      return "pages/config";
   }

   @RequestMapping({"/Sys/Update"})
   @AuthCheck
   public String Update(Model model) {
      model.addAttribute("name", this.hello);
      return "pages/update";
   }

   @PostMapping({"/Sys/UserManage/GetUser"})
   @AuthCheck
   @ResponseBody
   public SysUserInfoResult UserManageGetUser(@RequestParam("page") int page, @RequestParam("limit") int size) {
      Pageable pageable = PageRequest.of(page - 1, size, Direction.DESC, new String[]{"id"});
      Page<UserInfo> pp = this.userInfoRepository.findAll(pageable);
      List<UserInfo> list = pp.getContent();
      return new SysUserInfoResult(0, "", this.userInfoRepository.findAll().size(), list);
   }

   @PostMapping({"/Sys/UserManage/UserDel"})
   @AuthCheck
   @ResponseBody
   public BaseResult UserManageDelete(@RequestParam("username") String username) {
      UserInfo info = this.userInfoRepository.findByUsername(username);
      if (info == null) {
         return new BaseResult(404, "用户不存在");
      } else {
         this.userInfoRepository.delete(info);
         return new BaseResult(200, "删除成功");
      }
   }

   @PostMapping({"/Sys/UserManage/EditUser"})
   @AuthCheck
   @ResponseBody
   public BaseResult UserManageEdit(@RequestParam("username") String username, @RequestParam("email") String email, @RequestParam("expire_time") String time) throws ParseException {
      UserInfo info = this.userInfoRepository.findByUsername(username);
      if (info == null) {
         return new BaseResult(404, "用户不存在");
      } else {
         info.setEmail(email);
         info.setExpire_time((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse(time));
         this.userInfoRepository.save(info);
         return new BaseResult(200, "保存成功");
      }
   }

   @PostMapping({"/Sys/ReCardManage/GetReCard"})
   @AuthCheck
   @ResponseBody
   public SysRecardResult GetReCard(@RequestParam("page") int page, @RequestParam("limit") int size) {
      Pageable pageable = PageRequest.of(page - 1, size, Direction.DESC, new String[]{"id"});
      Page<SysRecard> pp = this.sysRecardRespository.findAll(pageable);
      List<SysRecard> list = pp.getContent();
      return new SysRecardResult(0, "", this.sysRecardRespository.findAll().size(), list);
   }

   @PostMapping({"/Sys/ReCardManage/ReCardDel"})
   @AuthCheck
   @ResponseBody
   public BaseResult ReCardDel(@RequestParam("recard") String recard) {
      SysRecard info = this.sysRecardRespository.findByRecard(recard);
      if (info == null) {
         return new BaseResult(404, "充值卡不存在");
      } else {
         this.sysRecardRespository.delete(info);
         return new BaseResult(200, "删除成功");
      }
   }

   @PostMapping({"/Sys/ReCardManage/EditReCard"})
   @AuthCheck
   @ResponseBody
   public BaseResult EditReCard(@RequestParam("recard") String recard, @RequestParam("frozen") int frozen, @RequestParam("mark") String mark) {
      SysRecard info = this.sysRecardRespository.findByRecard(recard);
      if (info == null) {
         return new BaseResult(404, "充值卡不存在");
      } else {
         info.setFrozen(frozen);
         info.setMark(mark);
         this.sysRecardRespository.save(info);
         return new BaseResult(200, "保存成功");
      }
   }

   @PostMapping({"/Sys/ReCardManage/DelAll"})
   @AuthCheck
   @ResponseBody
   public BaseResult DelAll(@RequestParam("ids") String ids) {
      String[] var2 = ids.split(",");
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         String id = var2[var4];
         this.sysRecardRespository.deleteById(Integer.parseInt(id));
      }

      return new BaseResult(200, "删除成功");
   }

   @PostMapping({"/Sys/CreateRecard/Card"})
   @AuthCheck
   @ResponseBody
   public BaseResult Card(@RequestParam("count") int count, @RequestParam("time") int time, @RequestParam("mark") String mark) {
      StringBuffer buffer = new StringBuffer();

      for(int i = 0; i < count; ++i) {
         SysRecard recard = new SysRecard();
         recard.setMark(mark);
         recard.setAll_minutes(time);
         recard.setRecard(this.createCode.RandomPoint());
         this.sysRecardRespository.save(recard);
         buffer.append("充值卡:" + recard.getRecard() + " 时长:" + time + "分钟|");
      }

      return new BaseResult(200, buffer.toString());
   }

   @PostMapping({"/Sys/Config/Save"})
   @AuthCheck
   @ResponseBody
   public BaseResult SaveConfig(@RequestBody SysConfig newconfig) {
      List<SysConfig> list = this.sysConfigRepository.findAll();
      if (list.size() > 0) {
         SysConfig config = (SysConfig)list.get(0);
         config.setAbout(newconfig.getAbout());
         config.setBuy_url(newconfig.getBuy_url());
         config.setHelp(newconfig.getHelp());
         config.setShare(newconfig.getShare());
         config.setUpdate_msg(newconfig.getUpdate_msg());
         config.setGroup(newconfig.getGroup());
         config.setFull_url(newconfig.getFull_url());
         config.setHot_fix_url(newconfig.getHot_fix_url());
         config.setX86full_url(newconfig.getX86full_url());
         config.setX86hot_fix_url(newconfig.getX86hot_fix_url());
         this.sysConfigRepository.save(config);
      } else {
         this.sysConfigRepository.save(newconfig);
      }

      return new BaseResult(200, "保存成功");
   }

   @PostMapping({"/Sys/Update/GetAll"})
   @AuthCheck
   @ResponseBody
   public SysUpdateResult GetAll(@RequestParam("page") int page, @RequestParam("limit") int size) {
      Pageable pageable = PageRequest.of(page - 1, size, Direction.DESC, new String[]{"id"});
      Page<SysUpdate> pp = this.sysUpdateRepository.findAll(pageable);
      List<SysUpdate> list = pp.getContent();
      return new SysUpdateResult(0, "", this.sysUpdateRepository.findAll().size(), list);
   }

   @PostMapping({"/Sys/Update/AddVersion"})
   @AuthCheck
   @ResponseBody
   public BaseResult AddVersion(@RequestParam("version") int version, @RequestParam("version_name") String version_name, @RequestParam("version_msg") String version_msg, @RequestParam("version_address") String version_address, @RequestParam("version_state") int version_state, @RequestParam("version_mode") int version_mode) {
      SysUpdate sysUpdate = new SysUpdate();
      sysUpdate.setVersion(version);
      sysUpdate.setVersion_name(version_name);
      sysUpdate.setVersion_msg(version_msg);
      sysUpdate.setVersion_address(version_address);
      sysUpdate.setVersion_state(version_state);
      sysUpdate.setVersion_mode(version_mode);
      this.sysUpdateRepository.save(sysUpdate);
      return new BaseResult(200, "添加成功");
   }

   @PostMapping({"/Sys/Update/EditVer"})
   @AuthCheck
   @ResponseBody
   public BaseResult EditVer(@RequestParam("id") int id, @RequestParam("version") int version, @RequestParam("version_name") String version_name, @RequestParam("version_msg") String version_msg, @RequestParam("version_address") String version_address, @RequestParam("version_state") int version_state, @RequestParam("version_mode") int version_mode) {
      SysUpdate sysUpdate = (SysUpdate)this.sysUpdateRepository.findById(id).get();
      sysUpdate.setVersion(version);
      sysUpdate.setVersion_name(version_name);
      sysUpdate.setVersion_msg(version_msg);
      sysUpdate.setVersion_address(version_address);
      sysUpdate.setVersion_state(version_state);
      sysUpdate.setVersion_mode(version_mode);
      this.sysUpdateRepository.save(sysUpdate);
      return new BaseResult(200, "修改成功");
   }

   @PostMapping({"/Sys/Update/VerDel"})
   @AuthCheck
   @ResponseBody
   public BaseResult VerDel(@RequestParam("id") int id) {
      this.sysUpdateRepository.deleteById(id);
      return new BaseResult(200, "删除成功");
   }

   @PostMapping({"/Sys/Info"})
   @ResponseBody
   public SysConfigResult GetSysInfo() {
      List<SysConfig> list = this.sysConfigRepository.findAll();
      return list.size() > 0 ? new SysConfigResult(200, "成功", (SysConfig)list.get(0)) : new SysConfigResult(404, "失败", (SysConfig)null);
   }

   @PostMapping({"/Sys/Ver/GetVer"})
   @ResponseBody
   public UpdateResult GetVer(@RequestParam("version") int version) {
      SysUpdate sysUpdate = this.sysUpdateRepository.findByVersion(version);
      PageRequest pageable;
      Page newVer;
      SysUpdate sys;
      if (sysUpdate == null) {
         pageable = PageRequest.of(0, 1, Direction.DESC, new String[]{"version"});
         newVer = this.sysUpdateRepository.findByVersionIsGreaterThan(version, pageable);
         if (newVer.getContent().size() == 0) {
            return new UpdateResult(404, "未找到最新版");
         } else {
            sys = (SysUpdate)newVer.getContent().get(0);
            this.Parsing(sys);
            return new UpdateResult(300, "获取成功", sys);
         }
      } else {
         pageable = PageRequest.of(0, 1, Direction.DESC, new String[]{"version"});
         newVer = this.sysUpdateRepository.findByVersionIsGreaterThan(version, pageable);
         if (newVer.getContent().size() == 0) {
            return new UpdateResult(200, "获取成功", sysUpdate);
         } else {
            sys = (SysUpdate)newVer.getContent().get(0);
            this.Parsing(sys);
            return new UpdateResult(300, "获取成功", sys);
         }
      }
   }

   @PostMapping({"/Sys/FullRes"})
   @ResponseBody
   public BaseResult GetFullRes() {
      List<SysConfig> list = this.sysConfigRepository.findAll();
      if (list.size() > 0) {
         String full = ((SysConfig)list.get(0)).getFull_url();
         if (full.startsWith("https://www.lanzous.com")) {
            LzyController lzyController = new LzyController();
            LzyResult lzyResult = lzyController.Parsing(full);
            if (lzyResult.getCode() == 200) {
               return !lzyResult.getData().getDow().contains(lzyResult.getData().getDom()) ? new BaseResult(200, lzyResult.getData().getDow()) : new BaseResult(200, full);
            } else {
               return new BaseResult(200, full);
            }
         } else {
            return new BaseResult(200, full);
         }
      } else {
         return new BaseResult(404, "失败");
      }
   }

   @PostMapping({"/Sys/HotRes"})
   @ResponseBody
   public BaseResult GetHotRes() {
      List<SysConfig> list = this.sysConfigRepository.findAll();
      if (list.size() > 0) {
         String hot_fix = ((SysConfig)list.get(0)).getHot_fix_url();
         if (hot_fix.startsWith("https://www.lanzous.com")) {
            LzyController lzyController = new LzyController();
            LzyResult lzyResult = lzyController.Parsing(hot_fix);
            if (lzyResult.getCode() == 200) {
               return !lzyResult.getData().getDow().contains(lzyResult.getData().getDom()) ? new BaseResult(200, lzyResult.getData().getDow()) : new BaseResult(200, hot_fix);
            } else {
               return new BaseResult(200, hot_fix);
            }
         } else {
            return new BaseResult(200, hot_fix);
         }
      } else {
         return new BaseResult(404, "失败");
      }
   }

   @PostMapping({"/Sys/X86FullRes"})
   @ResponseBody
   public BaseResult GetX86FullRes() {
      List<SysConfig> list = this.sysConfigRepository.findAll();
      if (list.size() > 0) {
         String full = ((SysConfig)list.get(0)).getX86full_url();
         if (full.startsWith("https://www.lanzous.com")) {
            LzyController lzyController = new LzyController();
            LzyResult lzyResult = lzyController.Parsing(full);
            if (lzyResult.getCode() == 200) {
               return !lzyResult.getData().getDow().contains(lzyResult.getData().getDom()) ? new BaseResult(200, lzyResult.getData().getDow()) : new BaseResult(200, full);
            } else {
               return new BaseResult(200, full);
            }
         } else {
            return new BaseResult(200, full);
         }
      } else {
         return new BaseResult(404, "失败");
      }
   }

   @PostMapping({"/Sys/X86HotRes"})
   @ResponseBody
   public BaseResult GetX86HotRes() {
      List<SysConfig> list = this.sysConfigRepository.findAll();
      if (list.size() > 0) {
         String hot_fix = ((SysConfig)list.get(0)).getX86hot_fix_url();
         if (hot_fix.startsWith("https://www.lanzous.com")) {
            LzyController lzyController = new LzyController();
            LzyResult lzyResult = lzyController.Parsing(hot_fix);
            if (lzyResult.getCode() == 200) {
               return !lzyResult.getData().getDow().contains(lzyResult.getData().getDom()) ? new BaseResult(200, lzyResult.getData().getDow()) : new BaseResult(200, hot_fix);
            } else {
               return new BaseResult(200, hot_fix);
            }
         } else {
            return new BaseResult(200, hot_fix);
         }
      } else {
         return new BaseResult(404, "失败");
      }
   }

   public void Parsing(SysUpdate sys) {
      if (sys.getVersion_address().startsWith("https://www.lanzous.com")) {
         LzyController lzyController = new LzyController();
         LzyResult lzyResult = lzyController.Parsing(sys.getVersion_address());
         if (lzyResult.getCode() == 200 && !lzyResult.getData().getDow().contains(lzyResult.getData().getDom())) {
            sys.setVersion_address(lzyResult.getData().getDow());
         }
      }

   }
}
