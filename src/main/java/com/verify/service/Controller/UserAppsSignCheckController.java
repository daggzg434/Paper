package com.verify.service.Controller;

import com.verify.service.Base.BaseController;
import com.verify.service.Config.BasicException;
import com.verify.service.Entity.UserAppsCheckSign;
import com.verify.service.Entity.UserInfo;
import com.verify.service.Entity.UserSoft;
import com.verify.service.Result.BaseResult;
import com.verify.service.Result.UserAppsSignCheckResult;
import com.verify.service.annotation.BasicCheck;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/apis/soft/apps_sign"})
public class UserAppsSignCheckController extends BaseController {
   @PostMapping({"/get"})
   @ResponseBody
   @BasicCheck
   public UserAppsSignCheckResult get(@RequestParam("token") String token, @RequestParam("appkey") String appkey) throws BasicException {
      UserInfo info = this.userInfoRepository.findByToken(token);
      UserSoft soft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(soft, info);
      List<UserAppsCheckSign> list = this.userAppsCheckSignRepository.findBySoftappkey(appkey);
      UserAppsSignCheckResult.data data = new UserAppsSignCheckResult.data();
      data.setAppsList(list);
      return new UserAppsSignCheckResult(200, "获取成功", data);
   }

   @PostMapping({"/delete"})
   @ResponseBody
   @BasicCheck
   public BaseResult delete(@RequestBody UserAppsCheckSign userAppsCheck, @RequestHeader("token") String token) throws BasicException {
      UserInfo info = this.userInfoRepository.findByToken(token);
      UserSoft soft = this.userSoftRepository.findByAppkey(userAppsCheck.getSoftappkey());
      this.SoftCheck(soft, info);
      this.userAppsCheckSignRepository.delete(userAppsCheck);
      return new BaseResult(200, "删除成功");
   }

   @PostMapping({"/save"})
   @ResponseBody
   @BasicCheck
   public BaseResult save(@RequestBody List<UserAppsCheckSign> userAppsChecks, @RequestHeader("token") String token) {
      this.userAppsCheckSignRepository.saveAll(userAppsChecks);
      return new BaseResult(200, "保存成功");
   }
}
