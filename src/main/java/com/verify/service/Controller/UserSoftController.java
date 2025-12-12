package com.verify.service.Controller;

import com.verify.service.ServiceApplication;
import com.verify.service.Base.BaseController;
import com.verify.service.Config.BasicException;
import com.verify.service.Entity.SoftStatistical;
import com.verify.service.Entity.SoftUser;
import com.verify.service.Entity.UserInfo;
import com.verify.service.Entity.UserPoint;
import com.verify.service.Entity.UserRecard;
import com.verify.service.Entity.UserRecode;
import com.verify.service.Entity.UserSoft;
import com.verify.service.Result.BaseResult;
import com.verify.service.Result.ChartResult;
import com.verify.service.Result.UserSoftResult;
import com.verify.service.annotation.BasicCheck;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/apis/usersoft"})
public class UserSoftController extends BaseController {
   @PostMapping({"/getusersoft"})
   @BasicCheck
   @ResponseBody
   public UserSoftResult getUserSoft(@RequestParam("token") String token) throws ParseException {
      UserInfo info = this.userInfoRepository.findByToken(token);
      UserSoftResult result = new UserSoftResult(200, "获取成功");
      List<UserSoft> userSoft = this.userSoftRepository.findAllByUserid(info.getId());
      Iterator var5 = userSoft.iterator();

      while(var5.hasNext()) {
         UserSoft soft = (UserSoft)var5.next();
         List all;
         int count;
         Iterator var9;
         List all_user;
         switch(soft.getAuthmode()) {
         case 1:
            all = this.userPointRepository.findAllByUseridAndSoftappkey(soft.getUserid(), soft.getAppkey());
            soft.setAll_point_card_count(all.size());
            count = 0;
            var9 = all.iterator();

            while(var9.hasNext()) {
               UserPoint p = (UserPoint)var9.next();
               if (p.getMac() != null) {
                  ++count;
               }
            }

            soft.setUse_point_card_count(count);
            all_user = this.userRecodeRepository.findAllByUseridAndSoftappkey(soft.getUserid(), soft.getAppkey());
            soft.setAll_recode_card_count(all_user.size());
            int recode_count = 0;
            Iterator var11 = all_user.iterator();

            while(var11.hasNext()) {
               UserRecode p = (UserRecode)var11.next();
               if (p.getMac() != null) {
                  ++recode_count;
               }
            }

            soft.setUse_recode_card_count(recode_count);
            break;
         case 3:
            all = this.userRecardRepository.findAllByUseridAndSoftappkey(soft.getUserid(), soft.getAppkey());
            soft.setAll_recard_card_count(all.size());
            count = 0;
            var9 = all.iterator();

            while(var9.hasNext()) {
               UserRecard p = (UserRecard)var9.next();
               if (p.getFrozen() == 1) {
                  ++count;
               }
            }

            soft.setUse_recard_card_count(count);
            all_user = this.softUserRepository.findBySoftappkey(soft.getAppkey());
            soft.setAll_user_count(all_user.size());
         }

         SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         LocalDateTime today_start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
         LocalDateTime today_end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
         String min = today_start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
         String max = today_end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
         List<SoftStatistical> softStatisticals = this.softStatisticalRepository.findByAppkeyAndTimeBetween(soft.getAppkey(), simpleDateFormat.parse(min), simpleDateFormat.parse(max));
         HashSet<String> iplist = new HashSet();
         Iterator var14 = softStatisticals.iterator();

         while(var14.hasNext()) {
            SoftStatistical statistical = (SoftStatistical)var14.next();
            iplist.add(statistical.getIp());
         }

         soft.setSoft_use_count(iplist.size());
      }

      result.setData(new UserSoftResult.data(userSoft));
      return result;
   }

   @GetMapping({"/getico"})
   @ResponseBody
   public void getSoftIco(@RequestParam("uuid") String uuid, HttpServletResponse response) throws IOException {
      File ico_path = new File(ServiceApplication.ico + File.separator + uuid);
      if (!ico_path.exists()) {
         throw new IOException("UUID错误");
      } else {
         FileInputStream ico = new FileInputStream(ico_path);
         byte[] bytes = new byte[ico.available()];
         ico.read(bytes);
         ico.close();
         response.getOutputStream().write(bytes);
         response.getOutputStream().flush();
      }
   }

   @PostMapping({"/save"})
   @ResponseBody
   @BasicCheck
   public BaseResult save(@RequestBody UserSoft soft, @RequestHeader("token") String token) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(soft.getAppkey());
      this.SoftCheck(userSoft, userInfo);
      userSoft.setOpen_bugly(soft.getOpen_bugly());
      userSoft.setBugly_appkey(soft.getBugly_appkey());
      userSoft.setOpen_vpn_check(soft.getOpen_vpn_check());
      userSoft.setOpen_xposed_check(soft.getOpen_xposed_check());
      userSoft.setOpen_secure_check(soft.getOpen_secure_check());
      userSoft.setOpen_vxp_check(soft.getOpen_vxp_check());
      userSoft.setOpen_splach(soft.getOpen_splach());
      userSoft.setOpen_notice(soft.getOpen_notice());
      userSoft.setSplach_url(soft.getSplach_url());
      userSoft.setSplach_time(soft.getSplach_time());
      userSoft.setOpen_notice_title(soft.getOpen_notice_title());
      userSoft.setOpen_notice_body(soft.getOpen_notice_body());
      userSoft.setOpen_notice_auto(soft.getOpen_notice_auto());
      userSoft.setOpen_notice_dialog_style(soft.getOpen_notice_dialog_style());
      userSoft.setOpen_notice_cancel(soft.getOpen_notice_cancel());
      userSoft.setOpen_notice_confirm_text(soft.getOpen_notice_confirm_text());
      userSoft.setOpen_notice_confirm_action(soft.getOpen_notice_confirm_action());
      userSoft.setOpen_notice_cancel_text(soft.getOpen_notice_cancel_text());
      userSoft.setOpen_notice_additional_text(soft.getOpen_notice_additional_text());
      userSoft.setOpen_notice_additional_action(soft.getOpen_notice_additional_action());
      userSoft.setOpen_notice_additional_action_body(soft.getOpen_notice_additional_action_body());
      userSoft.setOpen_notice_confirm_action_body(soft.getOpen_notice_confirm_action_body());
      userSoft.setOpen_update(soft.getOpen_update());
      userSoft.setUpdate_title(soft.getUpdate_title());
      userSoft.setUpdate_msg(soft.getUpdate_msg());
      userSoft.setUpdate_url(soft.getUpdate_url());
      userSoft.setUpdatemode(soft.getUpdatemode());
      userSoft.setUpdate_ver_mode(soft.getUpdate_ver_mode());
      userSoft.setUpdate_dialog_style(soft.getUpdate_dialog_style());
      userSoft.setVersion(soft.getVersion());
      userSoft.setTitle(soft.getTitle());
      userSoft.setNotice(soft.getNotice());
      userSoft.setDelay_time(soft.getDelay_time());
      userSoft.setShow_count(soft.getShow_count());
      userSoft.setShare_count(soft.getShare_count());
      userSoft.setShare_msg(soft.getShare_msg());
      userSoft.setMore_url(soft.getMore_url());
      userSoft.setQq_key(soft.getQq_key());
      userSoft.setGroup_key(soft.getGroup_key());
      userSoft.setGroup_style(soft.getGroup_style());
      userSoft.setFrozen(soft.getFrozen());
      userSoft.setBj_url(soft.getBj_url());
      userSoft.setTry_count(soft.getTry_count());
      userSoft.setTry_minutes(soft.getTry_minutes());
      userSoft.setWeburl(soft.getWeburl());
      userSoft.setBindmode(soft.getBindmode());
      userSoft.setDialog_style(soft.getDialog_style());
      userSoft.setDiy_body(soft.getDiy_body());
      userSoft.setReggive_time(soft.getReggive_time());
      userSoft.setUnbind_time(soft.getUnbind_time());
      userSoft.setRebindmode(soft.getRebindmode());
      userSoft.setOpen_top_lamp(soft.getOpen_top_lamp());
      userSoft.setOpen_top_lamp_msg(soft.open_top_lamp_msg);
      userSoft.setOpen_top_lamp_text_color(soft.open_top_lamp_text_color);
      userSoft.setOpen_top_lamp_bj_color(soft.getOpen_top_lamp_bj_color());
      userSoft.setOpen_top_lamp_action(soft.getOpen_top_lamp_action());
      userSoft.setOpen_top_lamp_action_body(soft.open_top_lamp_action_body);
      userSoft.setDialog_title_color(soft.getDialog_title_color());
      userSoft.setDialog_msg_color(soft.getDialog_msg_color());
      userSoft.setDialog_confirm_color(soft.getDialog_confirm_color());
      userSoft.setDialog_cancel_color(soft.getDialog_cancel_color());
      userSoft.setDialog_additional_color(soft.getDialog_additional_color());
      userSoft.setOpen_apps_check(soft.getOpen_apps_check());
      userSoft.setOpen_apps_sign_check(soft.getOpen_apps_sign_check());
      if (soft.getAuthmode() == 0) {
         userSoft.setAuthmode(0);
         return this.userSoftRepository.save(userSoft) != null ? new BaseResult(200, "保存成功") : new BaseResult(404, "保存失败");
      } else if (soft.getAuthmode() == 1) {
         userSoft.setAuthmode(1);
         return this.userSoftRepository.save(userSoft) != null ? new BaseResult(200, "保存成功") : new BaseResult(404, "保存失败");
      } else if (soft.getAuthmode() == 2) {
         userSoft.setAuthmode(2);
         return this.userSoftRepository.save(userSoft) != null ? new BaseResult(200, "保存成功") : new BaseResult(404, "保存失败");
      } else if (soft.getAuthmode() == 3) {
         userSoft.setAuthmode(3);
         return this.userSoftRepository.save(userSoft) != null ? new BaseResult(200, "保存成功") : new BaseResult(404, "保存失败");
      } else {
         return new BaseResult(404, "无效模式");
      }
   }

   @PostMapping({"/delete"})
   @ResponseBody
   @BasicCheck
   public BaseResult delete(@RequestBody UserSoft soft, @RequestHeader("token") String token) throws BasicException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(soft.getAppkey());
      this.SoftCheck(userSoft, userInfo);
      List<UserPoint> userPointList = this.userPointRepository.findAllByUseridAndSoftappkey(userInfo.getId(), userSoft.getAppkey());
      if (userPointList != null) {
         this.userPointRepository.deleteAll(userPointList);
      }

      List<UserRecard> userRecardList = this.userRecardRepository.findAllByUseridAndSoftappkey(userInfo.getId(), userSoft.getAppkey());
      if (userRecardList != null) {
         this.userRecardRepository.deleteAll(userRecardList);
      }

      List<UserRecode> userRecodeList = this.userRecodeRepository.findAllByUseridAndSoftappkey(userInfo.getId(), userSoft.getAppkey());
      if (userRecodeList != null) {
         this.userRecodeRepository.deleteAll(userRecodeList);
      }

      List<SoftUser> softUserList = this.softUserRepository.findBySoftappkey(userSoft.getAppkey());
      if (softUserList != null) {
         this.softUserRepository.deleteAll(softUserList);
      }

      (new File(ServiceApplication.ico + File.separator + userSoft.getIco_name())).delete();
      this.userSoftRepository.delete(userSoft);
      this.userAppsCheckRepository.deleteAll(this.userAppsCheckRepository.findBySoftappkey(userSoft.getAppkey()));
      this.userAppsCheckSignRepository.deleteAll(this.userAppsCheckSignRepository.findBySoftappkey(userSoft.getAppkey()));
      this.softStatisticalRepository.deleteAll(this.softStatisticalRepository.findByAppkey(userSoft.getAppkey()));
      return new BaseResult(200, "删除成功");
   }

   @PostMapping({"/chart"})
   @ResponseBody
   @BasicCheck
   public ChartResult chart(@RequestParam("token") String token, @RequestParam("appkey") String appkey) throws BasicException, ParseException {
      UserInfo userInfo = this.userInfoRepository.findByToken(token);
      UserSoft userSoft = this.userSoftRepository.findByAppkey(appkey);
      this.SoftCheck(userSoft, userInfo);
      SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      ChartResult chartResult = new ChartResult(200, "获取成功");
      List<ChartResult.data.ChartInfo> chartInfos = new ArrayList();
      LocalDateTime today_start = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
      LocalDateTime today_end = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

      for(int i = 5; i > -1; --i) {
         LocalDateTime start = today_start.plusDays((long)(-i));
         LocalDateTime end = today_end.plusDays((long)(-i));
         String min = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
         String max = end.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
         int start_count = (int)this.softStatisticalRepository.countByAppkeyAndTimeBetween(appkey, simpleDateFormat.parse(min), simpleDateFormat.parse(max));
         List<SoftStatistical> softStatisticals = this.softStatisticalRepository.findByAppkeyAndTimeBetween(appkey, simpleDateFormat.parse(min), simpleDateFormat.parse(max));
         HashSet<String> iplist = new HashSet();
         Iterator var18 = softStatisticals.iterator();

         while(var18.hasNext()) {
            SoftStatistical statistical = (SoftStatistical)var18.next();
            iplist.add(statistical.getIp());
         }

         int usr_count = iplist.size();
         ChartResult.data.ChartInfo chartInfo = new ChartResult.data.ChartInfo(start.format(DateTimeFormatter.ofPattern("MM-dd")), usr_count, start_count);
         chartInfos.add(chartInfo);
      }

      chartResult.setData(new ChartResult.data(chartInfos));
      return chartResult;
   }
}
