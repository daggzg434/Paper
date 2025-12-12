package com.verify.service.Controller;

import com.verify.service.Base.BaseController;
import com.verify.service.Config.BasicException;
import com.verify.service.Entity.UserAppsCheck;
import com.verify.service.Entity.UserInfo;
import com.verify.service.Entity.UserSoft;
import com.verify.service.Result.BaseResult;
import com.verify.service.Result.UserAppsCheckResult;
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
@RequestMapping({"/apis/soft/apps"})
public class UserAppsCheckController extends BaseController {
   @PostMapping({"/get"})
   @ResponseBody
   @BasicCheck
   public UserAppsCheckResult get(@RequestParam("token") String token, @RequestParam("appkey") String appkey) throws BasicException {
      UserInfo info = this.userInfoRepository.findByToken(token);
      UserSoft soft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(soft, info);
      List<UserAppsCheck> list = this.userAppsCheckRepository.findBySoftappkey(appkey);
      UserAppsCheckResult.data data = new UserAppsCheckResult.data();
      data.setAppsList(list);
      return new UserAppsCheckResult(200, "获取成功", data);
   }

   @PostMapping({"/delete"})
   @BasicCheck
   @ResponseBody
   public BaseResult delete(@RequestBody UserAppsCheck userAppsCheck, @RequestHeader("token") String token) throws BasicException {
      UserInfo info = this.userInfoRepository.findByToken(token);
      UserSoft soft = this.userSoftRepository.findByAppkey(userAppsCheck.getSoftappkey());
      this.SoftCheck(soft, info);
      this.userAppsCheckRepository.delete(userAppsCheck);
      return new BaseResult(200, "删除成功");
   }

   @PostMapping({"/save"})
   @ResponseBody
   @BasicCheck
   public BaseResult save(@RequestBody List<UserAppsCheck> userAppsChecks, @RequestHeader("token") String token) {
      this.userAppsCheckRepository.saveAll(userAppsChecks);
      return new BaseResult(200, "保存成功");
   }
}
