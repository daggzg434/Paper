package com.verify.service.Controller;

import com.verify.service.Base.BaseController;
import com.verify.service.Config.BasicException;
import com.verify.service.Entity.UserInfo;
import com.verify.service.Entity.UserRecode;
import com.verify.service.Entity.UserSoft;
import com.verify.service.Result.BaseResult;
import com.verify.service.Result.UserCodeResult;
import com.verify.service.Result.UserPointResult;
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
@RequestMapping({"/apis/usersoft/code"})
public class UserReCodeController extends BaseController {
   @PostMapping({"/get"})
   @ResponseBody
   @BasicCheck
   public Object getpoint(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("page") int page, @RequestParam("size") int size) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      Pageable pageable = PageRequest.of(page, size, Direction.DESC, new String[]{"id"});
      Page<UserRecode> pp = this.userRecodeRepository.findBySoftappkey(appkey, pageable);
      List<UserRecode> list = pp.getContent();
      UserCodeResult result = new UserCodeResult(200, "获取成功");
      result.setData(new UserCodeResult.data(list));
      return result;
   }

   @PostMapping({"/get_card"})
   @ResponseBody
   @BasicCheck
   public Object getrecodeandcard(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("card") String card) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      UserRecode recode = this.userRecodeRepository.findBySoftappkeyAndCard(appkey, card);
      if (recode == null) {
         return new BaseResult(404, "卡密不存在");
      } else {
         UserPointResult result = new UserPointResult(200, "获取成功");
         List<UserRecode> tmp = new ArrayList();
         tmp.add(recode);
         result.setData(new UserCodeResult.data(tmp));
         return result;
      }
   }

   @PostMapping({"/get_mark"})
   @ResponseBody
   @BasicCheck
   public Object getrecodemark(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("mark") String mark, @RequestParam("page") int page, @RequestParam("size") int size) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      Pageable pageable = PageRequest.of(page, size, Direction.DESC, new String[]{"id"});
      Page<UserRecode> pp = this.userRecodeRepository.findBySoftappkeyAndMark(appkey, mark, pageable);
      List<UserRecode> list = pp.getContent();
      UserCodeResult result = new UserCodeResult(200, "获取成功");
      result.setData(new UserCodeResult.data(list));
      return result;
   }

   @PostMapping({"/get_usecount"})
   @ResponseBody
   @BasicCheck
   public Object getrecodeusecount(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("usecount") int usecount, @RequestParam("page") int page, @RequestParam("size") int size) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      Pageable pageable = PageRequest.of(page, size, Direction.DESC, new String[]{"id"});
      Page<UserRecode> pp = this.userRecodeRepository.findBySoftappkeyAndUsecount(appkey, usecount, pageable);
      List<UserRecode> list = pp.getContent();
      UserCodeResult result = new UserCodeResult(200, "获取成功");
      result.setData(new UserCodeResult.data(list));
      return result;
   }

   @PostMapping({"/get_time"})
   @ResponseBody
   @BasicCheck
   public Object getrecodetime(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("time") int time, @RequestParam("page") int page, @RequestParam("size") int size) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      Pageable pageable = PageRequest.of(page, size, Direction.DESC, new String[]{"id"});
      Page<UserRecode> pp = this.userRecodeRepository.findBySoftappkeyAndAllminutes(appkey, time, pageable);
      List<UserRecode> list = pp.getContent();
      UserCodeResult result = new UserCodeResult(200, "获取成功");
      result.setData(new UserCodeResult.data(list));
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
      Page<UserRecode> pp = this.userRecodeRepository.findBySoftappkeyAndFrozen(appkey, frozen, pageable);
      List<UserRecode> list = pp.getContent();
      UserCodeResult result = new UserCodeResult(200, "获取成功");
      result.setData(new UserCodeResult.data(list));
      return result;
   }

   @PostMapping({"/get_use"})
   @ResponseBody
   @BasicCheck
   public Object getpointandnull(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("use") boolean use, @RequestParam("page") int page, @RequestParam("size") int size) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      Pageable pageable = PageRequest.of(page, size, Direction.DESC, new String[]{"id"});
      Page pp;
      if (use) {
         pp = this.userRecodeRepository.findBySoftappkeyAndMacIsNotNull(appkey, pageable);
      } else {
         pp = this.userRecodeRepository.findBySoftappkeyAndMacIsNull(appkey, pageable);
      }

      List<UserRecode> list = pp.getContent();
      UserCodeResult result = new UserCodeResult(200, "获取成功");
      result.setData(new UserCodeResult.data(list));
      return result;
   }

   @PostMapping({"/create"})
   @ResponseBody
   @BasicCheck
   public BaseResult create(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("time") int time, @RequestParam("size") int size, @RequestParam("mark") String mark) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      if (size > 99) {
         return new BaseResult(404, "点卡数量过多");
      } else if (time <= 0) {
         return new BaseResult(404, "单码无效");
      } else {
         UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
         this.SoftCheck(userSoft, userInfo);
         List<UserRecode> userRecodes = new ArrayList();

         for(int i = 0; i < size; ++i) {
            UserRecode newcode = new UserRecode();
            newcode.setCard(this.createCode.RandomPoint());
            newcode.setAll_minutes(time);
            newcode.setMark(mark);
            newcode.setSoftappkey(appkey);
            newcode.setUserid(userInfo.getId());
            userRecodes.add(newcode);
         }

         this.userRecodeRepository.saveAll(userRecodes);
         return new BaseResult(200, "生成完成");
      }
   }

   @PostMapping({"/update"})
   @ResponseBody
   @BasicCheck
   public BaseResult update(@RequestBody UserRecode code, @RequestHeader("token") String token) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(code.getSoftappkey());
      this.SoftCheck(userSoft, userInfo);
      UserRecode recode = this.userRecodeRepository.findByCard(code.getCard());
      recode.setFrozen(code.getFrozen());
      recode.setExpire_time(code.getExpire_time());
      recode.setMac(code.getMac());
      this.userRecodeRepository.save(recode);
      return new BaseResult(200, "更新成功");
   }

   @PostMapping({"/delete"})
   @ResponseBody
   @BasicCheck
   public BaseResult delete(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("card") String card) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      UserRecode recode = this.userRecodeRepository.findBySoftappkeyAndCard(appkey, card);
      if (recode == null) {
         return new BaseResult(404, "单码不存在");
      } else {
         this.userRecodeRepository.delete(recode);
         return new BaseResult(200, "删除完成");
      }
   }

   @PostMapping({"/delete_all"})
   @ResponseBody
   @BasicCheck
   public BaseResult deleteandall(@RequestBody List<UserRecode> recode, @RequestHeader("token") String token) {
      List<UserRecode> userRecodes = new ArrayList();
      Iterator var4 = recode.iterator();

      while(var4.hasNext()) {
         UserRecode s = (UserRecode)var4.next();
         UserRecode recode1 = this.userRecodeRepository.findBySoftappkeyAndCard(s.getSoftappkey(), s.getCard());
         userRecodes.add(recode1);
      }

      this.userRecodeRepository.deleteAll(userRecodes);
      return new BaseResult(200, "删除成功");
   }
}
