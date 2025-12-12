package com.verify.service.Controller;

import com.verify.service.Base.BaseController;
import com.verify.service.Config.BasicException;
import com.verify.service.Entity.SoftUser;
import com.verify.service.Entity.UserInfo;
import com.verify.service.Entity.UserSoft;
import com.verify.service.Result.BaseResult;
import com.verify.service.Result.SoftUserResult;
import com.verify.service.annotation.BasicCheck;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/apis/usersoft/user"})
public class SoftUserController extends BaseController {
   @PostMapping({"/get"})
   @ResponseBody
   @BasicCheck
   public Object get(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("page") int page, @RequestParam("size") int size) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      Pageable pageable = PageRequest.of(page, size, Direction.DESC, new String[]{"id"});
      Page<SoftUser> pp = this.softUserRepository.findBySoftappkey(appkey, pageable);
      List<SoftUser> list = pp.getContent();
      SoftUserResult result = new SoftUserResult(200, "获取成功");
      result.setData(new SoftUserResult.data(list));
      return result;
   }

   @PostMapping({"/get_user"})
   @ResponseBody
   @BasicCheck
   public Object getuser(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("username") String username) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      SoftUser softUser = this.softUserRepository.findByUsernameAndSoftappkey(username, appkey);
      if (softUser == null) {
         return new BaseResult(404, "用户名不存在");
      } else {
         SoftUserResult result = new SoftUserResult(200, "获取成功");
         List<SoftUser> tmp = new ArrayList();
         tmp.add(softUser);
         result.setData(new SoftUserResult.data(tmp));
         return result;
      }
   }

   @PostMapping({"/get_email"})
   @ResponseBody
   @BasicCheck
   public Object getemail(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("email") String email) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      List<SoftUser> softUser = this.softUserRepository.findBySoftappkeyAndEmail(appkey, email);
      SoftUserResult result = new SoftUserResult(200, "获取成功");
      result.setData(new SoftUserResult.data(softUser));
      return result;
   }

   @PostMapping({"/get_mac"})
   @ResponseBody
   @BasicCheck
   public Object getmac(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("mac") String mac) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      List<SoftUser> softUser = this.softUserRepository.findBySoftappkeyAndMac(appkey, mac);
      SoftUserResult result = new SoftUserResult(200, "获取成功");
      result.setData(new SoftUserResult.data(softUser));
      return result;
   }

   @PostMapping({"/get_frozen"})
   @ResponseBody
   @BasicCheck
   public Object getrecodeandFrozen(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("frozen") int frozen, @RequestParam("page") int page, @RequestParam("size") int size) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      Pageable pageable = PageRequest.of(page, size, Direction.DESC, new String[]{"id"});
      Page<SoftUser> pp = this.softUserRepository.findBySoftappkeyAndFrozen(appkey, frozen, pageable);
      List<SoftUser> list = pp.getContent();
      SoftUserResult result = new SoftUserResult(200, "获取成功");
      result.setData(new SoftUserResult.data(list));
      return result;
   }

   @PostMapping({"/get_expire"})
   @ResponseBody
   @BasicCheck
   public Object getpointandnull(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("expire") boolean expire, @RequestParam("page") int page, @RequestParam("size") int size) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      Pageable pageable = PageRequest.of(page, size, Direction.DESC, new String[]{"id"});
      Page pp;
      if (expire) {
         pp = this.softUserRepository.findBySoftappkeyAndExpiretimeBefore(appkey, new Date(), pageable);
      } else {
         pp = this.softUserRepository.findBySoftappkeyAndExpiretimeAfter(appkey, new Date(), pageable);
      }

      List<SoftUser> list = pp.getContent();
      SoftUserResult result = new SoftUserResult(200, "获取成功");
      result.setData(new SoftUserResult.data(list));
      return result;
   }

   @PostMapping({"/update"})
   @ResponseBody
   @BasicCheck
   public BaseResult update(@RequestBody SoftUser softUser, @RequestHeader("token") String token) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(softUser.getSoftappkey());
      this.SoftCheck(userSoft, userInfo);
      SoftUser softUser1 = this.softUserRepository.findByUsernameAndSoftappkey(softUser.getUsername(), softUser.getSoftappkey());
      softUser1.setFrozen(softUser.getFrozen());
      softUser1.setExpire_time(softUser.getExpire_time());
      softUser1.setMac(softUser.getMac());
      this.softUserRepository.save(softUser1);
      return new BaseResult(200, "更新成功");
   }

   @PostMapping({"/delete"})
   @ResponseBody
   @BasicCheck
   public BaseResult delete(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("username") String username) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      SoftUser softUser = this.softUserRepository.findByUsernameAndSoftappkey(username, appkey);
      if (softUser == null) {
         return new BaseResult(404, "用户不存在");
      } else {
         this.softUserRepository.delete(softUser);
         return new BaseResult(200, "删除完成");
      }
   }

   @PostMapping({"/delete_all"})
   @ResponseBody
   @BasicCheck
   public BaseResult deleteandall(@RequestBody List<SoftUser> softUsers, @RequestHeader("token") String token) {
      List<SoftUser> softUserList = new ArrayList();
      Iterator var4 = softUsers.iterator();

      while(var4.hasNext()) {
         SoftUser s = (SoftUser)var4.next();
         SoftUser softUser = this.softUserRepository.findByUsernameAndSoftappkey(s.getUsername(), s.getSoftappkey());
         softUserList.add(softUser);
      }

      this.softUserRepository.deleteAll(softUserList);
      return new BaseResult(200, "删除成功");
   }
}
