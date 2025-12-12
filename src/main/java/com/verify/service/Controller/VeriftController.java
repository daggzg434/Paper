package com.verify.service.Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.verify.service.ServiceApplication;
import com.verify.service.Base.BaseController;
import com.verify.service.Entity.SoftStatistical;
import com.verify.service.Entity.SoftUser;
import com.verify.service.Entity.TrialInfo;
import com.verify.service.Entity.UserInfo;
import com.verify.service.Entity.UserPoint;
import com.verify.service.Entity.UserRecard;
import com.verify.service.Entity.UserRecode;
import com.verify.service.Entity.UserSoft;
import com.verify.service.Result.BaseResult;
import com.verify.service.Result.CodeResult;
import com.verify.service.Result.QueryResult;
import com.verify.service.Result.UserAppsCheckResult;
import com.verify.service.Result.UserAppsSignCheckResult;
import com.verify.service.Result.VerifyPointResult;
import com.verify.service.Result.VerifyRecardResult;
import com.verify.service.Result.VerifyRecodeResult;
import com.verify.service.Utils.DesUtils;
import com.verify.service.Utils.InputToByte;
import com.verify.service.Utils.IpUtil;
import java.io.File;
import java.io.FileInputStream;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/Auth"})
@Component
public class VeriftController extends BaseController {
   private String Key = null;

   @GetMapping(
      value = {"/Verify"},
      produces = {"text/html;charset=UTF-8"}
   )
   @ResponseBody
   public Object Verify(HttpServletRequest request, HttpServletResponse response) throws Exception {
      response.setBufferSize(1024);
      response.setCharacterEncoding("UTF-8");
      response.setHeader("Content-Type", "text/html; charset=UTF-8");
      this.Key = DigestUtils.md5DigestAsHex(DigestUtils.md5DigestAsHex(DigestUtils.md5DigestAsHex(request.getHeader("Key").getBytes()).getBytes()).getBytes()).substring(0, 8);
      BaseResult baseResult = new BaseResult(404, "");
      String AppKey = request.getHeader("Appkey");
      String Mac = request.getHeader("Mac");
      String Code = request.getHeader("Code");
      String Token = request.getHeader("Token");
      String Type = request.getHeader("Type");
      String UserName = request.getHeader("UserName") != null ? URLDecoder.decode(request.getHeader("UserName"), "UTF-8") : null;
      String PassWord = request.getHeader("PassWord");
      String Email = request.getHeader("Email");
      String Recard = request.getHeader("Recard");
      String var13 = request.getHeader("Api");
      byte var14 = -1;
      switch(var13.hashCode()) {
      case -2095631481:
         if (var13.equals("PanGolin_User_Check")) {
            var14 = 16;
         }
         break;
      case -2087109144:
         if (var13.equals("PanGolin_User_Login")) {
            var14 = 12;
         }
         break;
      case -2082314425:
         if (var13.equals("PanGolin_User_Query")) {
            var14 = 15;
         }
         break;
      case -1827029492:
         if (var13.equals("PanGolin_Verify")) {
            var14 = 6;
         }
         break;
      case -1700490521:
         if (var13.equals("PanGolin_User_Pay")) {
            var14 = 13;
         }
         break;
      case -1539843132:
         if (var13.equals("PanGolin_User_Register")) {
            var14 = 11;
         }
         break;
      case -1230269648:
         if (var13.equals("PanGolin_GetCode")) {
            var14 = 4;
         }
         break;
      case -733545579:
         if (var13.equals("PanGolin_patch_res_md5")) {
            var14 = 3;
         }
         break;
      case -447323314:
         if (var13.equals("PanGolin_User_Forget")) {
            var14 = 17;
         }
         break;
      case -19284041:
         if (var13.equals("PanGolin_User_Unbind")) {
            var14 = 14;
         }
         break;
      case 62140693:
         if (var13.equals("PanGolin_Check")) {
            var14 = 8;
         }
         break;
      case 75457749:
         if (var13.equals("PanGolin_Query")) {
            var14 = 10;
         }
         break;
      case 78142243:
         if (var13.equals("PanGolin_Trial")) {
            var14 = 7;
         }
         break;
      case 96547771:
         if (var13.equals("PanGolin_GetSoftInfo")) {
            var14 = 5;
         }
         break;
      case 103505013:
         if (var13.equals("PanGolin_patch")) {
            var14 = 0;
         }
         break;
      case 263909044:
         if (var13.equals("PanGolin_patch_md5")) {
            var14 = 2;
         }
         break;
      case 263913942:
         if (var13.equals("PanGolin_patch_res")) {
            var14 = 1;
         }
         break;
      case 1549258091:
         if (var13.equals("PanGolin_PointCheck")) {
            var14 = 9;
         }
      }

      UserSoft userSoft;
      SoftUser softUser;
      long expire_time;
      VerifyRecodeResult result;
      SoftUser softUser;
      String token;
      UserPoint userPoint_verify;
      BaseResult patch_res_result;
      switch(var14) {
      case 0:
         response.getOutputStream().write(InputToByte.toByte(new FileInputStream(ServiceApplication.temp_static + File.separator + "patch.dll")));
         response.getOutputStream().flush();
         response.getOutputStream().close();
         return "";
      case 1:
         response.getOutputStream().write(InputToByte.toByte(new FileInputStream(ServiceApplication.temp_static + File.separator + "patch.res")));
         response.getOutputStream().flush();
         response.getOutputStream().close();
         return "";
      case 2:
         patch_res_result = new BaseResult(200, DigestUtils.md5DigestAsHex(InputToByte.toByte(new FileInputStream(ServiceApplication.temp_static + File.separator + "patch.dll"))));
         response.addHeader("Result", DesUtils.encrypt((new Gson()).toJson(patch_res_result), this.Key));
         response.flushBuffer();
         break;
      case 3:
         patch_res_result = new BaseResult(200, DigestUtils.md5DigestAsHex(InputToByte.toByte(new FileInputStream(ServiceApplication.temp_static + File.separator + "patch.res"))));
         response.addHeader("Result", DesUtils.encrypt((new Gson()).toJson(patch_res_result), this.Key));
         response.flushBuffer();
         break;
      case 4:
         userSoft = this.VerifyUserSoft(AppKey, baseResult, response);
         if (userSoft != null) {
            if (Mac != null && !Mac.isEmpty() && !Mac.equals("unknown")) {
               List<UserRecode> userRecodeList = this.userRecodeRepository.findAllByMacAndSoftappkey(Mac, userSoft.getAppkey());
               if (userRecodeList != null && userRecodeList.size() != 0) {
                  boolean flag = false;

                  for(int i = 0; i < userRecodeList.size(); ++i) {
                     UserRecode recode = (UserRecode)userRecodeList.get(i);
                     if (recode.getExpire_time().getTime() > System.currentTimeMillis() && recode.getFrozen() == 0) {
                        baseResult.setCode(200);
                        baseResult.setData(new CodeResult(recode.getCard()));
                        baseResult.setMsg("获取成功");
                        this.Result(baseResult, response);
                        flag = true;
                        break;
                     }
                  }

                  if (!flag) {
                     this.Result(baseResult, response);
                  }
               } else {
                  baseResult.setMsg("未找到可用单码");
                  this.Result(baseResult, response);
               }
            } else {
               baseResult.setMsg("设备码NULL??");
               this.Result(baseResult, response);
            }
         }
         break;
      case 5:
         userSoft = this.VerifyUserSoft(AppKey, baseResult, response);
         if (userSoft != null) {
            UserAppsCheckResult.data appscheck = new UserAppsCheckResult.data();
            appscheck.setAppsList(this.userAppsCheckRepository.findBySoftappkey(AppKey));
            response.addHeader("Apps", DesUtils.encrypt((new Gson()).toJson(appscheck), this.Key));
            UserAppsSignCheckResult.data appssigncheck = new UserAppsSignCheckResult.data();
            appssigncheck.setAppsList(this.userAppsCheckSignRepository.findBySoftappkey(AppKey));
            response.addHeader("AppsSign", DesUtils.encrypt((new Gson()).toJson(appssigncheck), this.Key));
            baseResult.setCode(200);
            baseResult.setMsg("获取成功");
            baseResult.setData(userSoft);
            this.Result(baseResult, response);
            SoftStatistical statistical = new SoftStatistical();
            statistical.setAppkey(AppKey);
            statistical.setTime(new Date(System.currentTimeMillis()));
            statistical.setIp(IpUtil.getIpAddr(request));
            this.softStatisticalRepository.save(statistical);
         }
         break;
      case 6:
         userSoft = this.VerifyUserSoft(AppKey, baseResult, response);
         if (userSoft != null) {
            if (Mac != null && !Mac.isEmpty() && !Mac.equals("unknown")) {
               if (Code == null) {
                  baseResult.setMsg("卡密为空");
                  this.Result(baseResult, response);
               } else {
                  userPoint_verify = this.userPointRepository.findBySoftappkeyAndCard(AppKey, Code);
                  if (userPoint_verify != null) {
                     if (userPoint_verify.getFrozen() == 1) {
                        baseResult.setMsg("点卡已被冻结无法使用");
                        this.Result(baseResult, response);
                     } else if (userPoint_verify.getPoint() <= 0) {
                        baseResult.setMsg("点卡点数已使用完");
                        this.Result(baseResult, response);
                     } else if (userPoint_verify.getMac() != null && !userPoint_verify.getMac().isEmpty() && (userSoft.getBindmode() == 0 || userSoft.getBindmode() == 1) && !userPoint_verify.getMac().equals(Mac)) {
                        baseResult.setMsg("该卡密已绑定其他设备");
                        this.Result(baseResult, response);
                     } else {
                        token = null;
                        switch(userSoft.getBindmode()) {
                        case 0:
                           token = DigestUtils.md5DigestAsHex((Mac + Code + System.currentTimeMillis()).getBytes());
                           break;
                        case 1:
                           token = DigestUtils.md5DigestAsHex((Mac + Code).getBytes());
                           break;
                        case 2:
                           token = DigestUtils.md5DigestAsHex(Code.getBytes());
                        }

                        userPoint_verify.setPoint(userPoint_verify.getPoint() - 1);
                        userPoint_verify.setUse_time(new Date());
                        userPoint_verify.setMac(Mac);
                        userPoint_verify.setToken(token);
                        this.userPointRepository.save(userPoint_verify);
                        VerifyPointResult result = new VerifyPointResult(200, "校验成功");
                        result.setCard_type(1);
                        result.setToken(token);
                        result.setPoint(userPoint_verify.getPoint());
                        this.Result(result, response);
                     }
                  } else {
                     UserRecode userRecode_verify = this.userRecodeRepository.findBySoftappkeyAndCard(AppKey, Code);
                     if (userRecode_verify == null) {
                        baseResult.setMsg("单码/点卡不存在");
                        this.Result(baseResult, response);
                     } else if (userRecode_verify.getFrozen() == 1) {
                        baseResult.setMsg("单码已被冻结无法使用");
                        this.Result(baseResult, response);
                     } else if (userRecode_verify.getExpire_time() != null && userRecode_verify.getExpire_time().getTime() < System.currentTimeMillis()) {
                        baseResult.setMsg("已到期");
                        this.Result(baseResult, response);
                     } else if (userRecode_verify.getMac() != null && !userRecode_verify.getMac().isEmpty() && (userSoft.getBindmode() == 0 || userSoft.getBindmode() == 1) && !userRecode_verify.getMac().equals(Mac)) {
                        baseResult.setMsg("该卡密已绑定其他设备");
                        this.Result(baseResult, response);
                     } else {
                        String token = null;
                        switch(userSoft.getBindmode()) {
                        case 0:
                           token = DigestUtils.md5DigestAsHex((Mac + Code + System.currentTimeMillis()).getBytes());
                           break;
                        case 1:
                           token = DigestUtils.md5DigestAsHex((Mac + Code).getBytes());
                           break;
                        case 2:
                           token = DigestUtils.md5DigestAsHex(Code.getBytes());
                        }

                        long all_time = (long)userRecode_verify.getAll_minutes() * 1000L * 60L;
                        userRecode_verify.setUse_count(userRecode_verify.getUse_count() + 1);
                        if (userRecode_verify.getUse_time() == null) {
                           userRecode_verify.setExpire_time(new Date(System.currentTimeMillis() + all_time));
                           userRecode_verify.setUse_time(new Date());
                           userRecode_verify.setToken(token);
                           userRecode_verify.setMac(Mac);
                        } else {
                           userRecode_verify.setToken(token);
                           userRecode_verify.setMac(Mac);
                        }

                        this.userRecodeRepository.save(userRecode_verify);
                        result = new VerifyRecodeResult(200, "校验成功");
                        result.setCard_type(0);
                        result.setToken(token);
                        result.setTime(userRecode_verify.getExpire_time());
                        this.Result(result, response);
                     }
                  }
               }
            } else {
               this.Result(baseResult, response);
            }
         }
         break;
      case 7:
         userSoft = this.VerifyUserSoft(AppKey, baseResult, response);
         if (userSoft != null) {
            if (Mac != null && !Mac.isEmpty() && !Mac.equals("unknown")) {
               if (userSoft.getTry_count() > 0 && userSoft.getTry_minutes() > 0) {
                  String token = DigestUtils.md5DigestAsHex((Mac + System.currentTimeMillis()).getBytes());
                  TrialInfo trialInfo = this.trialRepository.findByMacAndAppkey(Mac, AppKey);
                  expire_time = (long)userSoft.getTry_minutes() * 1000L * 60L;
                  if (trialInfo == null) {
                     TrialInfo info = new TrialInfo();
                     info.setAppkey(AppKey);
                     info.setMac(Mac);
                     info.setToken(token);
                     info.setHas_try_count(1);
                     info.setLast_time(new Date());
                     this.trialRepository.save(info);
                     result = new VerifyRecodeResult(200, "试用成功 你还可试用" + (userSoft.getTry_count() - 1) + "次");
                     result.setToken(token);
                     result.setTime(new Date(System.currentTimeMillis() + expire_time));
                     this.Result(result, response);
                  } else {
                     int remainder_count = userSoft.getTry_count() - trialInfo.getHas_try_count();
                     if (remainder_count > 0) {
                        trialInfo.setHas_try_count(trialInfo.getHas_try_count() + 1);
                        trialInfo.setToken(token);
                        trialInfo.setLast_time(new Date());
                        this.trialRepository.save(trialInfo);
                        result = new VerifyRecodeResult(200, "试用成功 你还可试用" + (remainder_count - 1) + "次");
                        result.setToken(token);
                        result.setTime(new Date(System.currentTimeMillis() + expire_time));
                        this.Result(result, response);
                     } else {
                        baseResult.setMsg("试用次数已用完");
                        this.Result(baseResult, response);
                     }
                  }
               } else {
                  baseResult.setMsg("应用不支持试用");
                  this.Result(baseResult, response);
               }
            } else {
               this.Result(baseResult, response);
            }
         }
         break;
      case 8:
         userSoft = this.VerifyUserSoft(AppKey, baseResult, response);
         if (userSoft != null) {
            if (Mac != null && !Mac.isEmpty() && !Mac.equals("unknown")) {
               if (Type != null && Token != null) {
                  byte var45 = -1;
                  switch(Type.hashCode()) {
                  case -1268779025:
                     if (Type.equals("formal")) {
                        var45 = 0;
                     }
                     break;
                  case 110628630:
                     if (Type.equals("trial")) {
                        var45 = 1;
                     }
                  }

                  switch(var45) {
                  case 0:
                     UserRecode recode = this.userRecodeRepository.findBySoftappkeyAndCard(AppKey, Code);
                     if (recode == null) {
                        this.Result(baseResult, response);
                     } else if (recode.getMac().equals(Mac) && recode.getFrozen() != 1 && recode.getExpire_time().getTime() >= System.currentTimeMillis() && recode.getToken().equals(Token)) {
                        baseResult.setCode(200);
                        this.Result(baseResult, response);
                     } else {
                        this.Result(baseResult, response);
                     }
                     break;
                  case 1:
                     TrialInfo trialInfo = this.trialRepository.findByMacAndAppkey(Mac, AppKey);
                     if (trialInfo == null) {
                        this.Result(baseResult, response);
                     } else if (!trialInfo.getMac().equals(Mac)) {
                        this.Result(baseResult, response);
                     } else if ((System.currentTimeMillis() - trialInfo.getLast_time().getTime()) / 1000L / 60L > (long)userSoft.getTry_minutes()) {
                        this.Result(baseResult, response);
                     } else {
                        baseResult.setCode(200);
                        this.Result(baseResult, response);
                     }
                  }
               } else {
                  baseResult.setMsg("类型错误");
                  this.Result(baseResult, response);
               }
            } else {
               this.Result(baseResult, response);
            }
         }
         break;
      case 9:
         userSoft = this.VerifyUserSoft(AppKey, baseResult, response);
         if (userSoft != null) {
            if (Mac != null && !Mac.isEmpty() && !Mac.equals("unknown")) {
               userPoint_verify = this.userPointRepository.findBySoftappkeyAndCard(AppKey, Code);
               if (userPoint_verify == null) {
                  this.Result(baseResult, response);
               } else if (userPoint_verify.getPoint() >= 0 && userPoint_verify.getFrozen() != 1 && userPoint_verify.getToken().equals(Token)) {
                  baseResult.setCode(200);
                  this.Result(baseResult, response);
               } else {
                  this.Result(baseResult, response);
               }
            } else {
               this.Result(baseResult, response);
            }
         }
         break;
      case 10:
         UserPoint point = this.userPointRepository.findByCard(Code);
         if (point != null) {
            QueryResult result = new QueryResult(200, "查询成功");
            result.setCard_type(1);
            result.setData(point);
            this.Result(result, response);
         } else {
            UserRecode recode = this.userRecodeRepository.findByCard(Code);
            if (recode == null) {
               baseResult.setMsg("单码/点卡不存在");
               this.Result(baseResult, response);
            } else {
               QueryResult result = new QueryResult(200, "查询成功");
               result.setCard_type(0);
               result.setData(recode);
               this.Result(result, response);
            }
         }
         break;
      case 11:
         userSoft = this.VerifyUserSoft(AppKey, baseResult, response);
         if (userSoft != null) {
            if (UserName != null && PassWord != null && Email != null) {
               if (UserName.length() >= 6 && PassWord.length() >= 6 && UserName.length() <= 20 && PassWord.length() <= 20) {
                  if (!Email.contains("@")) {
                     baseResult.setMsg("邮箱格式错误");
                     this.Result(baseResult, response);
                  } else {
                     softUser = this.softUserRepository.findByUsernameAndSoftappkey(UserName, AppKey);
                     if (softUser != null) {
                        baseResult.setMsg("用户已存在");
                        this.Result(baseResult, response);
                     } else {
                        Date expire_time = new Date(System.currentTimeMillis() + (long)(userSoft.getReggive_time() * 1000 * 60));
                        SoftUser newuser = new SoftUser();
                        newuser.setExpire_time(expire_time);
                        newuser.setEmail(Email);
                        newuser.setUsername(UserName);
                        newuser.setPassword(PassWord);
                        newuser.setReg_time(new Date());
                        newuser.setSoftappkey(AppKey);
                        if (this.softUserRepository.save(newuser) != null) {
                           baseResult.setCode(200);
                           baseResult.setMsg("注册成功");
                           this.Result(baseResult, response);
                        } else {
                           baseResult.setMsg("注册失败");
                           this.Result(baseResult, response);
                        }
                     }
                  }
               } else {
                  baseResult.setMsg("账号/密码长度错误 请输入6-20位之间");
                  this.Result(baseResult, response);
               }
            } else {
               baseResult.setMsg("参数错误");
               this.Result(baseResult, response);
            }
         }
         break;
      case 12:
         userSoft = this.VerifyUserSoft(AppKey, baseResult, response);
         if (userSoft != null) {
            if (UserName != null && PassWord != null && Mac != null) {
               if (UserName.length() >= 6 && PassWord.length() >= 6 && UserName.length() <= 20 && PassWord.length() <= 20) {
                  softUser = this.softUserRepository.findByUsernameAndSoftappkey(UserName, AppKey);
                  if (softUser == null) {
                     baseResult.setMsg("用户不存在");
                     this.Result(baseResult, response);
                  } else if (!softUser.getPassword().equals(PassWord)) {
                     baseResult.setMsg("密码不正确");
                     this.Result(baseResult, response);
                  } else if (softUser.getFrozen() == 1) {
                     baseResult.setMsg("账号已被冻结无法登录");
                     this.Result(baseResult, response);
                  } else if (softUser.getMac() != null && !softUser.getMac().isEmpty() && (userSoft.getBindmode() == 0 || userSoft.getBindmode() == 1) && !softUser.getMac().equals(Mac)) {
                     baseResult.setMsg("登录失败,该用户已绑其他设备");
                     this.Result(baseResult, response);
                  } else if (softUser.getExpire_time().getTime() < System.currentTimeMillis()) {
                     baseResult.setMsg("账号已到期");
                     this.Result(baseResult, response);
                  } else {
                     token = null;
                     switch(userSoft.getBindmode()) {
                     case 0:
                        token = DigestUtils.md5DigestAsHex((Mac + UserName + System.currentTimeMillis()).getBytes());
                        break;
                     case 1:
                        token = DigestUtils.md5DigestAsHex((Mac + UserName).getBytes());
                        break;
                     case 2:
                        token = DigestUtils.md5DigestAsHex(UserName.getBytes());
                     }

                     softUser.setLastlogin_time(softUser.getCurrentlogin_time() == null ? new Date() : softUser.getCurrentlogin_time());
                     softUser.setCurrentlogin_time(new Date());
                     softUser.setLogin_count(softUser.getLogin_count() + 1);
                     softUser.setMac(Mac);
                     softUser.setToken(token);
                     if (this.softUserRepository.save(softUser) != null) {
                        VerifyRecodeResult result = new VerifyRecodeResult(200, "登录成功");
                        result.setTime(softUser.getExpire_time());
                        result.setToken(token);
                        this.Result(result, response);
                     } else {
                        baseResult.setMsg("登录失败");
                        this.Result(baseResult, response);
                     }
                  }
               } else {
                  baseResult.setMsg("账号/密码长度错误 请输入6-20位之间");
                  this.Result(baseResult, response);
               }
            } else {
               baseResult.setMsg("参数错误");
               this.Result(baseResult, response);
            }
         }
         break;
      case 13:
         userSoft = this.VerifyUserSoft(AppKey, baseResult, response);
         if (userSoft != null) {
            if (UserName != null && Recard != null) {
               UserRecard recard = this.userRecardRepository.findBySoftappkeyAndRecard(AppKey, Recard);
               if (recard == null) {
                  baseResult.setMsg("充值卡不存在");
                  this.Result(baseResult, response);
               } else if (recard.getFrozen() == 1) {
                  baseResult.setMsg("充值卡已使用");
                  this.Result(baseResult, response);
               } else {
                  softUser = this.softUserRepository.findByUsernameAndSoftappkey(UserName, AppKey);
                  if (softUser == null) {
                     baseResult.setMsg("用户不存在");
                     this.Result(baseResult, response);
                  } else if (softUser.getFrozen() == 1) {
                     baseResult.setMsg("用户已被冻结,无法充值");
                     this.Result(baseResult, response);
                  } else {
                     expire_time = 0L;
                     if (softUser.getExpire_time() == null) {
                        expire_time = System.currentTimeMillis() + (long)recard.getAll_minutes() * 1000L * 60L;
                     } else if (softUser.getExpire_time().getTime() > System.currentTimeMillis()) {
                        expire_time = softUser.getExpire_time().getTime() + (long)recard.getAll_minutes() * 1000L * 60L;
                     } else {
                        expire_time = System.currentTimeMillis() + (long)(recard.getAll_minutes() * 1000 * 60);
                     }

                     softUser.setExpire_time(new Date(expire_time));
                     this.softUserRepository.save(softUser);
                     recard.setFrozen(1);
                     recard.setUsername(UserName);
                     recard.setUse_time(new Date());
                     if (this.userRecardRepository.save(recard) != null) {
                        VerifyRecardResult result = new VerifyRecardResult(200, "充值成功");
                        result.setTime((new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).format(expire_time));
                        this.Result(result, response);
                     } else {
                        baseResult.setMsg("充值失败");
                        this.Result(baseResult, response);
                     }
                  }
               }
            } else {
               baseResult.setMsg("参数错误");
               this.Result(baseResult, response);
            }
         }
         break;
      case 14:
         userSoft = this.VerifyUserSoft(AppKey, baseResult, response);
         if (userSoft != null) {
            if (userSoft.getRebindmode() == 1) {
               baseResult.setMsg("当前应用不支持解绑");
               this.Result(baseResult, response);
            } else {
               softUser = this.softUserRepository.findByUsernameAndSoftappkey(UserName, AppKey);
               if (softUser == null) {
                  baseResult.setMsg("用户不存在");
                  this.Result(baseResult, response);
               } else if (!softUser.getPassword().equals(PassWord)) {
                  baseResult.setMsg("密码不正确");
                  this.Result(baseResult, response);
               } else if (softUser.getFrozen() == 1) {
                  baseResult.setMsg("用户已被冻结无法解绑");
                  this.Result(baseResult, response);
               } else if (softUser.getMac() == null) {
                  baseResult.setMsg("用户已解绑，请在新设备进行登录");
                  this.Result(baseResult, response);
               } else if (softUser.getMac().isEmpty()) {
                  baseResult.setMsg("用户已解绑，请在新设备进行登录");
                  this.Result(baseResult, response);
               } else if (softUser.getExpire_time().getTime() < System.currentTimeMillis()) {
                  baseResult.setMsg("用户已到期,无法解绑");
                  this.Result(baseResult, response);
               } else {
                  long expire_time = userSoft.getUnbind_time() > 0 ? softUser.getExpire_time().getTime() - (long)(userSoft.getUnbind_time() * 1000 * 60) : softUser.getExpire_time().getTime();
                  softUser.setExpire_time(new Date(expire_time));
                  softUser.setMac("");
                  if (this.softUserRepository.save(softUser) != null) {
                     baseResult.setCode(200);
                     baseResult.setMsg("用户已解绑，请在新设备进行登录");
                     this.Result(baseResult, response);
                  } else {
                     baseResult.setMsg("解绑失败.....");
                     this.Result(baseResult, response);
                  }
               }
            }
         }
         break;
      case 15:
         userSoft = this.VerifyUserSoft(AppKey, baseResult, response);
         if (userSoft != null) {
            StringBuffer buffer = new StringBuffer();
            if (UserName != null && !UserName.isEmpty()) {
               softUser = this.softUserRepository.findByUsernameAndSoftappkey(UserName, AppKey);
               if (softUser == null) {
                  buffer.append(UserName + " 用户不存在\n");
               } else {
                  buffer.append(UserName + " 到期时间:" + (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).format(softUser.getExpire_time()) + "\n").append(UserName + " 状态:" + (softUser.getFrozen() == 0 ? "正常\n" : "冻结\n")).append(UserName + " 设备码:" + softUser.getMac() + "\n");
               }
            }

            if (Recard != null && !Recard.isEmpty()) {
               UserRecard recard = this.userRecardRepository.findByRecard(Recard);
               if (recard == null) {
                  buffer.append(Recard + " 充值卡不存在\n");
               } else {
                  buffer.append(Recard + " 卡值:" + recard.getAll_minutes() + "分钟\n").append(Recard + " 状态:" + (recard.getFrozen() == 0 ? "未充值\n" : "已充值\n")).append(Recard + " 使用者:" + recard.getUsername());
               }
            }

            baseResult.setCode(200);
            baseResult.setMsg(buffer.toString());
            this.Result(baseResult, response);
         }
         break;
      case 16:
         userSoft = this.VerifyUserSoft(AppKey, baseResult, response);
         if (userSoft != null) {
            if (Mac != null && !Mac.isEmpty() && !Mac.equals("unknown")) {
               softUser = this.softUserRepository.findByUsernameAndSoftappkey(UserName, AppKey);
               if (softUser == null) {
                  this.Result(baseResult, response);
               } else if (softUser.getFrozen() == 1) {
                  this.Result(baseResult, response);
               } else if (softUser.getExpire_time().getTime() < System.currentTimeMillis()) {
                  this.Result(baseResult, response);
               } else if (!softUser.getToken().equals(Token)) {
                  this.Result(baseResult, response);
               } else {
                  baseResult.setCode(200);
                  this.Result(baseResult, response);
               }
            } else {
               this.Result(baseResult, response);
            }
         }
         break;
      case 17:
         userSoft = this.VerifyUserSoft(AppKey, baseResult, response);
         if (userSoft != null) {
            if (Email == null) {
               baseResult.setMsg("参数错误");
               this.Result(baseResult, response);
            } else {
               softUser = (SoftUser)this.softUserRepository.findBySoftappkeyAndEmail(AppKey, Email).get(0);
               if (softUser == null) {
                  baseResult.setMsg("邮箱不存在");
                  this.Result(baseResult, response);
               } else {
                  StringBuffer buffer = new StringBuffer();
                  buffer.append(Email + " 找回密码成功\n").append("你的账号:" + softUser.getUsername() + "\n").append("你的密码:" + softUser.getPassword() + "\n");
                  this.emailUtil.sendTextEmail(Email, "穿山甲云科技(密码找回)", buffer.toString());
                  baseResult.setCode(200);
                  baseResult.setMsg("邮箱已发送成功\n如为收到请到垃圾箱进行查看");
                  this.Result(baseResult, response);
               }
            }
         }
      }

      return "穿山甲云科技";
   }

   private UserSoft VerifyUserSoft(String AppKey, BaseResult baseResult, HttpServletResponse response) throws Exception {
      UserSoft userSoft = this.softRepository.findByAppkey(AppKey);
      if (userSoft == null) {
         baseResult.setMsg("Appkey不正确");
         this.Result(baseResult, response);
         return null;
      } else if (userSoft.frozen == 1) {
         baseResult.setMsg("应用被禁用");
         this.Result(baseResult, response);
         return null;
      } else {
         int User_Id = userSoft.getUserid();
         UserInfo userInfo = this.userInfoRepository.findById(User_Id);
         if (userInfo == null) {
            baseResult.setMsg("商户不存在");
            this.Result(baseResult, response);
            return null;
         } else if (userInfo.getExpire_time().getTime() < System.currentTimeMillis()) {
            baseResult.setMsg("商户已到期");
            this.Result(baseResult, response);
            return null;
         } else {
            return userSoft;
         }
      }
   }

   private void Result(Object data, HttpServletResponse response) throws Exception {
      Gson gson = (new GsonBuilder()).setDateFormat("yyyy-MM-dd HH:mm:ss").create();
      response.addHeader("Result", DesUtils.encrypt(gson.toJson(data), this.Key));
      response.flushBuffer();
   }
}
