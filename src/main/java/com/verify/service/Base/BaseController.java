package com.verify.service.Base;

import com.verify.service.Config.BasicException;
import com.verify.service.Entity.UserInfo;
import com.verify.service.Entity.UserSoft;
import com.verify.service.Repository.SoftStatisticalRepository;
import com.verify.service.Repository.SoftUserRepository;
import com.verify.service.Repository.SysConfigRepository;
import com.verify.service.Repository.SysRecardRespository;
import com.verify.service.Repository.SysUpdateRepository;
import com.verify.service.Repository.TrialRepository;
import com.verify.service.Repository.UserAppsCheckRepository;
import com.verify.service.Repository.UserAppsCheckSignRepository;
import com.verify.service.Repository.UserInfoRepository;
import com.verify.service.Repository.UserPointRepository;
import com.verify.service.Repository.UserRecardRepository;
import com.verify.service.Repository.UserRecodeRepository;
import com.verify.service.Repository.UserSoftRepository;
import com.verify.service.Utils.CreateCode;
import com.verify.service.Utils.EmailUtil;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

public class BaseController {
   @Value("${test.username}")
   public String test_username;
   @Value("${admin.username}")
   public String admin_username;
   @Value("${index.out}")
   public String hello;
   @Value("${index.apkpath}")
   public String dow;
   @Value("${global.name}")
   public String ApplicationName;
   @Autowired
   public CreateCode createCode;
   @Autowired
   public UserSoftRepository userSoftRepository;
   @Autowired
   public UserInfoRepository userInfoRepository;
   @Autowired
   public UserSoftRepository softRepository;
   @Autowired
   public UserPointRepository userPointRepository;
   @Autowired
   public UserRecodeRepository userRecodeRepository;
   @Autowired
   public TrialRepository trialRepository;
   @Autowired
   public SoftUserRepository softUserRepository;
   @Autowired
   public UserRecardRepository userRecardRepository;
   @Autowired
   public UserAppsCheckRepository userAppsCheckRepository;
   @Autowired
   public UserAppsCheckSignRepository userAppsCheckSignRepository;
   @Autowired
   public EmailUtil emailUtil;
   @Autowired
   public SysRecardRespository sysRecardRespository;
   @Autowired
   public SysConfigRepository sysConfigRepository;
   @Autowired
   public SysUpdateRepository sysUpdateRepository;
   @Autowired
   public SoftStatisticalRepository softStatisticalRepository;
   public static Map<String, Map<Integer, ByteArrayOutputStream>> statelist = new HashMap();
   public static Map<String, Future> runnableMap = new HashMap();

   public void SoftCheck(UserSoft userSoft, UserInfo userInfo) throws BasicException {
      if (userSoft == null) {
         throw new BasicException("应用不存在");
      } else if (userSoft.getUserid() != userInfo.getId()) {
         throw new BasicException("应用不属于该用户");
      }
   }
}
