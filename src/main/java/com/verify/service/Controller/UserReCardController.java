package com.verify.service.Controller;

import com.verify.service.Base.BaseController;
import com.verify.service.Config.BasicException;
import com.verify.service.Entity.UserInfo;
import com.verify.service.Entity.UserRecard;
import com.verify.service.Entity.UserSoft;
import com.verify.service.Result.BaseResult;
import com.verify.service.Result.UserPointResult;
import com.verify.service.Result.UserReCardResult;
import com.verify.service.annotation.BasicCheck;
import java.util.ArrayList;
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
@RequestMapping({"/apis/usersoft/recard"})
public class UserReCardController extends BaseController {
   @PostMapping({"/get"})
   @ResponseBody
   @BasicCheck
   public Object getrecard(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("page") int page, @RequestParam("size") int size) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      Pageable pageable = PageRequest.of(page, size, Direction.DESC, new String[]{"id"});
      Page<UserRecard> pp = this.userRecardRepository.findBySoftappkey(appkey, pageable);
      List<UserRecard> list = pp.getContent();
      UserReCardResult result = new UserReCardResult(200, "获取成功");
      result.setData(new UserReCardResult.data(list));
      return result;
   }

   @PostMapping({"/get_mark"})
   @ResponseBody
   @BasicCheck
   public Object getrecodemark(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("mark") String mark, @RequestParam("page") int page, @RequestParam("size") int size) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      Pageable pageable = PageRequest.of(page, size, Direction.DESC, new String[]{"id"});
      Page<UserRecard> pp = this.userRecardRepository.findBySoftappkeyAndMark(appkey, mark, pageable);
      List<UserRecard> list = pp.getContent();
      UserReCardResult result = new UserReCardResult(200, "获取成功");
      result.setData(new UserReCardResult.data(list));
      return result;
   }

   @PostMapping({"/get_card"})
   @ResponseBody
   @BasicCheck
   public Object getrecodeandcard(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("card") String card) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      UserRecard recode = this.userRecardRepository.findBySoftappkeyAndRecard(appkey, card);
      if (recode == null) {
         return new BaseResult(404, "卡密不存在");
      } else {
         UserPointResult result = new UserPointResult(200, "获取成功");
         List<UserRecard> tmp = new ArrayList();
         tmp.add(recode);
         result.setData(new UserReCardResult.data(tmp));
         return result;
      }
   }

   @PostMapping({"/get_frozen"})
   @ResponseBody
   @BasicCheck
   public Object getrecodeandFrozen(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("frozen") int frozen, @RequestParam("page") int page, @RequestParam("size") int size) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      Pageable pageable = PageRequest.of(page, size, Direction.DESC, new String[]{"id"});
      Page<UserRecard> pp = this.userRecardRepository.findBySoftappkeyAndFrozen(appkey, frozen, pageable);
      List<UserRecard> list = pp.getContent();
      UserReCardResult result = new UserReCardResult(200, "获取成功");
      result.setData(new UserReCardResult.data(list));
      return result;
   }

   @PostMapping({"/create"})
   @ResponseBody
   @BasicCheck
   public BaseResult create(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("time") int time, @RequestParam("size") int size, @RequestParam("mark") String mark) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      if (size > 99) {
         return new BaseResult(404, "充值卡数量过多");
      } else if (time <= 0) {
         return new BaseResult(404, "充值卡时长无效");
      } else {
         UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
         this.SoftCheck(userSoft, userInfo);
         List<UserRecard> userRecards = new ArrayList();

         for(int i = 0; i < size; ++i) {
            UserRecard newRecard = new UserRecard();
            newRecard.setRecard(this.createCode.RandomPoint());
            newRecard.setAll_minutes(time);
            newRecard.setMark(mark);
            newRecard.setSoftappkey(appkey);
            newRecard.setUserid(userInfo.getId());
            userRecards.add(newRecard);
         }

         this.userRecardRepository.saveAll(userRecards);
         return new BaseResult(200, "生成完成");
      }
   }

   @PostMapping({"/update"})
   @ResponseBody
   @BasicCheck
   public BaseResult update(@RequestBody UserRecard recard, @RequestHeader("token") String token) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(recard.getSoftappkey());
      this.SoftCheck(userSoft, userInfo);
      UserRecard userRecard = this.userRecardRepository.findBySoftappkeyAndRecard(recard.getSoftappkey(), recard.getRecard());
      userRecard.setFrozen(recard.getFrozen());
      userRecard.setAll_minutes(recard.getAll_minutes());
      this.userRecardRepository.save(userRecard);
      return new BaseResult(200, "更新成功");
   }

   @PostMapping({"/delete"})
   @ResponseBody
   public BaseResult delete(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("recard") String recard) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      UserRecard userRecard = this.userRecardRepository.findBySoftappkeyAndRecard(appkey, recard);
      if (userRecard == null) {
         return new BaseResult(404, "充值卡不存在");
      } else {
         this.userRecardRepository.delete(userRecard);
         return new BaseResult(200, "删除完成");
      }
   }

   @PostMapping({"/delete_all"})
   @ResponseBody
   @BasicCheck
   public BaseResult deleteandall(@RequestBody List<UserRecard> recards, @RequestHeader("token") String token) {
      List<UserRecard> userRecards = new ArrayList();
      Iterator var4 = recards.iterator();

      while(var4.hasNext()) {
         UserRecard s = (UserRecard)var4.next();
         UserRecard recard = this.userRecardRepository.findBySoftappkeyAndRecard(s.getSoftappkey(), s.getRecard());
         userRecards.add(recard);
      }

      this.userRecardRepository.deleteAll(userRecards);
      return new BaseResult(200, "删除成功");
   }
}
