package com.verify.service.Controller;

import com.verify.service.Base.BaseController;
import com.verify.service.Config.BasicException;
import com.verify.service.Entity.UserInfo;
import com.verify.service.Entity.UserPoint;
import com.verify.service.Entity.UserSoft;
import com.verify.service.Result.BaseResult;
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
@RequestMapping({"/apis/usersoft/point"})
public class UserPointController extends BaseController {
   @PostMapping({"/get"})
   @ResponseBody
   @BasicCheck
   public Object getpoint(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("page") int page, @RequestParam("size") int size) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      Pageable pageable = PageRequest.of(page, size, Direction.DESC, new String[]{"id"});
      Page<UserPoint> pp = this.userPointRepository.findBySoftappkey(appkey, pageable);
      List<UserPoint> list = pp.getContent();
      UserPointResult result = new UserPointResult(200, "获取成功");
      result.setData(new UserPointResult.data(list));
      return result;
   }

   @PostMapping({"/get_mark"})
   @ResponseBody
   @BasicCheck
   public Object getpointandmark(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("mark") String mark, @RequestParam("page") int page, @RequestParam("size") int size) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      Pageable pageable = PageRequest.of(page, size, Direction.DESC, new String[]{"id"});
      Page<UserPoint> pp = this.userPointRepository.findBySoftappkeyAndMark(appkey, mark, pageable);
      List<UserPoint> list = pp.getContent();
      UserPointResult result = new UserPointResult(200, "获取成功");
      result.setData(new UserPointResult.data(list));
      return result;
   }

   @PostMapping({"/get_point"})
   @ResponseBody
   @BasicCheck
   public Object getpointandpoint(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("point") int point, @RequestParam("page") int page, @RequestParam("size") int size) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      Pageable pageable = PageRequest.of(page, size, Direction.DESC, new String[]{"id"});
      Page<UserPoint> pp = this.userPointRepository.findBySoftappkeyAndPoint(appkey, point, pageable);
      List<UserPoint> list = pp.getContent();
      UserPointResult result = new UserPointResult(200, "获取成功");
      result.setData(new UserPointResult.data(list));
      return result;
   }

   @PostMapping({"/get_card"})
   @ResponseBody
   @BasicCheck
   public Object getpointandcard(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("card") String card) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      UserPoint userPoint = this.userPointRepository.findBySoftappkeyAndCard(appkey, card);
      if (userPoint == null) {
         return new BaseResult(404, "卡密不存在");
      } else {
         UserPointResult result = new UserPointResult(200, "获取成功");
         List<UserPoint> tmp = new ArrayList();
         tmp.add(userPoint);
         result.setData(new UserPointResult.data(tmp));
         return result;
      }
   }

   @PostMapping({"/get_frozen"})
   @ResponseBody
   @BasicCheck
   public Object getpointandFrozen(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("frozen") int frozen, @RequestParam("page") int page, @RequestParam("size") int size) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      Pageable pageable = PageRequest.of(page, size, Direction.DESC, new String[]{"id"});
      Page<UserPoint> pp = this.userPointRepository.findBySoftappkeyAndFrozen(appkey, frozen, pageable);
      List<UserPoint> list = pp.getContent();
      UserPointResult result = new UserPointResult(200, "获取成功");
      result.setData(new UserPointResult.data(list));
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
         pp = this.userPointRepository.findBySoftappkeyAndMacIsNotNull(appkey, pageable);
      } else {
         pp = this.userPointRepository.findBySoftappkeyAndMacIsNull(appkey, pageable);
      }

      List<UserPoint> list = pp.getContent();
      UserPointResult result = new UserPointResult(200, "获取成功");
      result.setData(new UserPointResult.data(list));
      return result;
   }

   @PostMapping({"/create"})
   @ResponseBody
   @BasicCheck
   public BaseResult create(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("point") int point, @RequestParam("size") int size, @RequestParam("mark") String mark) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      if (size > 99) {
         return new BaseResult(404, "点卡数量过多");
      } else if (point <= 0) {
         return new BaseResult(404, "点数无效");
      } else {
         UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
         this.SoftCheck(userSoft, userInfo);
         List<UserPoint> all = this.userPointRepository.findAllByUseridAndSoftappkey(userInfo.getId(), appkey);
         if (all != null && all.size() > 2000) {
            return new BaseResult(404, "点卡总数量过多");
         } else {
            List<UserPoint> userPoints = new ArrayList();

            for(int i = 0; i < size; ++i) {
               UserPoint newPoint = new UserPoint();
               newPoint.setCard(this.createCode.RandomPoint());
               newPoint.setPoint(point);
               newPoint.setMark(mark);
               newPoint.setSoftappkey(appkey);
               newPoint.setUserid(userInfo.getId());
               userPoints.add(newPoint);
            }

            this.userPointRepository.saveAll(userPoints);
            return new BaseResult(200, "生成完成");
         }
      }
   }

   @PostMapping({"/update"})
   @ResponseBody
   @BasicCheck
   public BaseResult update(@RequestBody UserPoint point, @RequestHeader("token") String token) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(point.getSoftappkey());
      this.SoftCheck(userSoft, userInfo);
      UserPoint point1 = this.userPointRepository.findByCard(point.getCard());
      point1.setFrozen(point.getFrozen());
      point1.setPoint(point.getPoint());
      point1.setMac(point.getMac());
      this.userPointRepository.save(point1);
      return new BaseResult(200, "更新成功");
   }

   @PostMapping({"/delete"})
   @ResponseBody
   @BasicCheck
   public BaseResult delete(@RequestParam("token") String token, @RequestParam("appkey") String appkey, @RequestParam("card") String card) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      UserPoint point = this.userPointRepository.findByCard(card);
      if (point == null) {
         return new BaseResult(404, "点卡不存在");
      } else {
         this.userPointRepository.delete(point);
         return new BaseResult(200, "删除完成");
      }
   }

   @PostMapping({"/delete_all"})
   @ResponseBody
   @BasicCheck
   public BaseResult deleteandall(@RequestBody List<UserPoint> point, @RequestHeader("token") String token) {
      List<UserPoint> userPoints = new ArrayList();
      Iterator var4 = point.iterator();

      while(var4.hasNext()) {
         UserPoint s = (UserPoint)var4.next();
         UserPoint point1 = this.userPointRepository.findBySoftappkeyAndCard(s.getSoftappkey(), s.getCard());
         userPoints.add(point1);
      }

      this.userPointRepository.deleteAll(userPoints);
      userPoints.clear();
      return new BaseResult(200, "删除成功");
   }

   @PostMapping({"/update_all"})
   @ResponseBody
   @BasicCheck
   public BaseResult updateandall(@RequestBody List<UserPoint> point, @RequestHeader("token") String token) {
      List<UserPoint> userPoints = new ArrayList();
      Iterator var4 = point.iterator();

      while(var4.hasNext()) {
         UserPoint s = (UserPoint)var4.next();
         UserPoint point1 = this.userPointRepository.findBySoftappkeyAndCard(s.getSoftappkey(), s.getCard());
         point1.setFrozen(s.getFrozen());
         userPoints.add(point1);
      }

      this.userPointRepository.saveAll(userPoints);
      userPoints.clear();
      return new BaseResult(200, "更新成功");
   }
}
