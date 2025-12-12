package com.verify.service.Controller;

import com.verify.service.Base.BaseController;
import com.verify.service.Entity.SysRecard;
import com.verify.service.Entity.UserInfo;
import com.verify.service.Entity.UserSoft;
import com.verify.service.Result.BaseResult;
import com.verify.service.Result.ResultUserInfo;
import com.verify.service.Result.UserInfoResult;
import com.verify.service.Utils.IpUtil;
import com.verify.service.Utils.SHAUtils;
import com.verify.service.annotation.BasicCheck;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import javax.servlet.http.HttpServletRequest;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/apis/user"})
public class UserInfoController extends BaseController {
   @PostMapping({"/login"})
   @ResponseBody
   public UserInfoResult login(@RequestParam("username") String username, @RequestParam("password") String password, HttpServletRequest request) {
      if (username.length() <= 5) {
         return new UserInfoResult(404, "账号过短");
      } else if (password.length() <= 5) {
         return new UserInfoResult(404, "密码过短");
      } else {
         UserInfo info = this.userInfoRepository.findByUsername(username);
         if (info == null) {
            return new UserInfoResult(404, "账号不存在");
         } else {
            String pass = DigestUtils.md5DigestAsHex(password.getBytes());
            char[] arr = pass.toCharArray();
            pass = SHAUtils.SHA1(arr[4] + String.valueOf(arr[3]) + arr[0] + arr[1] + arr[2] + pass.substring(5));
            if (!info.getPass().equals(pass)) {
               return new UserInfoResult(404, "密码错误");
            } else {
               if (info.getInvitation().isEmpty()) {
                  info.setInvitation(DigestUtils.md5DigestAsHex(UUID.randomUUID().toString().getBytes()).substring(0, 8));
               }

               String token = DigestUtils.md5DigestAsHex((System.currentTimeMillis() + UUID.randomUUID().toString() + info.getName()).getBytes());
               info.setToken(token);
               info.setCurrentlogin_ip(IpUtil.getIpAddr(request));
               info.setLastlogin_ip(info.getCurrentlogin_ip());
               info.setLogin_count(info.getLogin_count() + 1);
               info.setLastlogin_time(info.getCurrentlogin_time());
               info.setCurrentlogin_time(new Date());
               this.userInfoRepository.save(info);
               UserInfoResult result = new UserInfoResult(200, "校验成功");
               result.setData(new UserInfoResult.data(token));
               return result;
            }
         }
      }
   }

   @PostMapping({"/sign"})
   @ResponseBody
   public UserInfoResult adminlogin(@RequestParam("username") String username, @RequestParam("password") String password, HttpServletRequest request) {
      if (username.length() <= 5) {
         return new UserInfoResult(404, "账号过短");
      } else if (password.length() <= 5) {
         return new UserInfoResult(404, "密码过短");
      } else if (!username.equals(this.admin_username)) {
         return new UserInfoResult(404, "管理员账号错误");
      } else {
         UserInfo info = this.userInfoRepository.findByUsername(username);
         if (info == null) {
            return new UserInfoResult(404, "账号不存在");
         } else {
            String pass = DigestUtils.md5DigestAsHex(password.getBytes());
            char[] arr = pass.toCharArray();
            pass = SHAUtils.SHA1(arr[4] + String.valueOf(arr[3]) + arr[0] + arr[1] + arr[2] + pass.substring(5));
            if (!info.getPass().equals(pass)) {
               return new UserInfoResult(404, "密码错误");
            } else if (info.getExpire_time().getTime() < System.currentTimeMillis()) {
               return new UserInfoResult(404, "用户已到期/请您联系管理激活账号");
            } else {
               String token = DigestUtils.md5DigestAsHex((System.currentTimeMillis() + UUID.randomUUID().toString() + info.getName()).getBytes());
               info.setToken(token);
               info.setLogin_count(info.getLogin_count() + 1);
               info.setLastlogin_time(info.getCurrentlogin_time());
               info.setCurrentlogin_time(new Date());
               this.userInfoRepository.save(info);
               UserInfoResult result = new UserInfoResult(200, "校验成功");
               result.setData(new UserInfoResult.data(token));
               request.getSession().setAttribute("token", token);
               return result;
            }
         }
      }
   }

   @PostMapping({"/reg"})
   @ResponseBody
   public UserInfoResult reg(@RequestParam("username") String username, @RequestParam("password") String password, @RequestParam("email") String email, @RequestParam(value = "invitation",required = false) String invitation, HttpServletRequest request) {
      if (!email.contains("@")) {
         return new UserInfoResult(404, "邮箱格式错误");
      } else if (username.length() <= 5) {
         return new UserInfoResult(404, "账号过短");
      } else if (password.length() <= 5) {
         return new UserInfoResult(404, "密码过短");
      } else {
         UserInfo info = new UserInfo();
         if (this.userInfoRepository.findByUsername(username) != null) {
            return new UserInfoResult(404, "账号已存在");
         } else {
            UserInfo code_info = null;
            if (!invitation.isEmpty()) {
               code_info = this.userInfoRepository.findByInvitation(invitation);
               if (code_info == null) {
                  return new UserInfoResult(404, "邀请码不存在");
               }
            }

            if (code_info != null && !IpUtil.getIpAddr(request).equals(code_info.getCurrentlogin_ip())) {
               int all = code_info.getValid_invitation() + 1;
               long time = 0L;
               if (all == 10) {
                  time = 2592000000L;
               } else if (all == 50) {
                  time = 15811200000L;
               } else if (all == 90) {
                  time = 31536000000L;
               } else if (all > 90) {
                  time = 259200000L;
               }

               if (time > 0L) {
                  if (code_info.getExpire_time().getTime() < System.currentTimeMillis()) {
                     code_info.setExpire_time(new Date(System.currentTimeMillis() + time));
                  } else {
                     code_info.setExpire_time(new Date(code_info.getExpire_time().getTime() + time));
                  }
               }

               code_info.setValid_invitation(all);
               this.userInfoRepository.save(code_info);
               info.setExpire_time(new Date(System.currentTimeMillis() + 259200000L));
            }

            String pass = DigestUtils.md5DigestAsHex(password.getBytes());
            char[] arr = pass.toCharArray();
            pass = SHAUtils.SHA1(arr[4] + String.valueOf(arr[3]) + arr[0] + arr[1] + arr[2] + pass.substring(5));
            info.setName(username);
            info.setPass(pass);
            info.setEmail(email);
            if (username.equals(this.admin_username) || username.equals(this.test_username)) {
               try {
                  info.setExpire_time((new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).parse("2030-05-20 20:52:00"));
               } catch (ParseException var11) {
                  var11.printStackTrace();
               }
            }

            this.userInfoRepository.save(info);
            return new UserInfoResult(200, "注册成功");
         }
      }
   }

   @PostMapping({"/token_check"})
   @ResponseBody
   public UserInfoResult CheckToekn(@RequestParam("token") String token) {
      UserInfo info = this.userInfoRepository.findByToken(token);
      if (info == null) {
         return new UserInfoResult(404, "token不存在");
      } else {
         if (info.getInvitation().isEmpty()) {
            info.setInvitation(DigestUtils.md5DigestAsHex(UUID.randomUUID().toString().getBytes()).substring(0, 8));
         }

         String new_token = DigestUtils.md5DigestAsHex((System.currentTimeMillis() + UUID.randomUUID().toString() + info.getName()).getBytes());
         info.setToken(new_token);
         this.userInfoRepository.save(info);
         UserInfoResult result = new UserInfoResult(200, "校验成功");
         result.setData(new UserInfoResult.data(new_token));
         return result;
      }
   }

   @PostMapping({"/info"})
   @ResponseBody
   @BasicCheck
   public ResultUserInfo getinfo(@RequestParam("token") String token) {
      UserInfo info = this.userInfoRepository.findByToken(token);
      if (info == null) {
         return new ResultUserInfo(404, "token不存在");
      } else {
         List<UserSoft> softs = this.userSoftRepository.findAllByUserid(info.getId());
         return new ResultUserInfo(200, "获取成功", info.getName(), (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(info.getExpire_time()), softs.size(), info.getInject_count(), info.getValid_invitation(), info.getInvitation());
      }
   }

   @PostMapping({"/pay"})
   @ResponseBody
   public BaseResult pay(@RequestParam("token") String token, @RequestParam("card") String card) {
      UserInfo info = this.userInfoRepository.findByToken(token);
      if (info == null) {
         return new BaseResult(404, "token不存在");
      } else if (info.getName().equals(this.test_username)) {
         return new BaseResult(404, "测试账号无法充值");
      } else {
         SysRecard recard = this.sysRecardRespository.findByRecard(card);
         if (recard == null) {
            return new BaseResult(404, "卡密不存在");
         } else if (recard.getFrozen() != 0) {
            return new BaseResult(404, "卡密已被使用");
         } else {
            long time = (long)recard.getAll_minutes() * 1000L * 60L;
            if (info.getExpire_time().getTime() < System.currentTimeMillis()) {
               info.setExpire_time(new Date(System.currentTimeMillis() + time));
            } else {
               info.setExpire_time(new Date(info.getExpire_time().getTime() + time));
            }

            recard.setFrozen(1);
            recard.setUse_time(new Date(System.currentTimeMillis()));
            recard.setUsername(info.getName());
            this.sysRecardRespository.save(recard);
            this.userInfoRepository.save(info);
            return new BaseResult(200, "充值成功\nVip到期时间:" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(info.getExpire_time()));
         }
      }
   }

   @PostMapping({"/modify"})
   @ResponseBody
   public BaseResult modify(@RequestParam("token") String token, @RequestParam("password") String password, @RequestParam("newpassword") String newpassword) {
      UserInfo info = this.userInfoRepository.findByToken(token);
      if (info == null) {
         return new BaseResult(404, "token不存在");
      } else if (info.getName().equals(this.test_username)) {
         return new BaseResult(404, "测试账号无法修改密码");
      } else {
         String pass = DigestUtils.md5DigestAsHex(password.getBytes());
         char[] arr = pass.toCharArray();
         pass = SHAUtils.SHA1(arr[4] + String.valueOf(arr[3]) + arr[0] + arr[1] + arr[2] + pass.substring(5));
         if (!info.getPass().equals(pass)) {
            return new BaseResult(404, "旧密码不正确");
         } else if (newpassword.length() <= 5) {
            return new BaseResult(404, "新密码过短");
         } else {
            pass = DigestUtils.md5DigestAsHex(newpassword.getBytes());
            arr = pass.toCharArray();
            pass = SHAUtils.SHA1(arr[4] + String.valueOf(arr[3]) + arr[0] + arr[1] + arr[2] + pass.substring(5));
            info.setPass(pass);
            this.userInfoRepository.save(info);
            return new BaseResult(200, "修改成功");
         }
      }
   }

   @PostMapping({"/retrieve"})
   @ResponseBody
   public BaseResult modify(@RequestParam("email") String emali, @RequestParam("username") String username) {
      UserInfo info = this.userInfoRepository.findByUsername(username);
      if (info == null) {
         return new BaseResult(404, "用户不存在");
      } else if (!info.getEmail().equals(emali)) {
         return new BaseResult(404, "邮箱不正确");
      } else if (username.equals(this.test_username)) {
         return new BaseResult(404, "测试号无法找回密码");
      } else {
         String newpass = String.valueOf(this.createCode.RandomPoint().hashCode());
         String pass = DigestUtils.md5DigestAsHex(String.valueOf(newpass).getBytes());
         char[] arr = pass.toCharArray();
         pass = SHAUtils.SHA1(arr[4] + String.valueOf(arr[3]) + arr[0] + arr[1] + arr[2] + pass.substring(5));
         info.setPass(pass);
         StringBuffer buffer = new StringBuffer();
         buffer.append(emali + " 找回密码成功\n").append("你的账号:" + username + "\n").append("你的密码:" + newpass + "\n");
         this.userInfoRepository.save(info);
         this.emailUtil.sendTextEmail(emali, "穿山甲云科技(密码找回)", buffer.toString());
         return new BaseResult(200, "邮箱已发送成功\n如未收到请到垃圾箱进行查看");
      }
   }
}
