package com.verify.service.Controller;

import com.google.gson.Gson;
import com.verify.service.Base.BaseController;
import com.verify.service.Result.LzyResult;
import com.verify.service.annotation.BasicCheck;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Request.Builder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/apis/lzy"})
public class LzyController extends BaseController {
   TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
      public X509Certificate[] getAcceptedIssuers() {
         X509Certificate[] x509Certificates = new X509Certificate[0];
         return x509Certificates;
      }

      public void checkClientTrusted(X509Certificate[] certs, String authType) {
      }

      public void checkServerTrusted(X509Certificate[] certs, String authType) {
      }
   }};
   HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
      public boolean verify(String hostname, SSLSession session) {
         return true;
      }
   };

   @PostMapping({"/parsing"})
   @ResponseBody
   @BasicCheck
   public LzyResult Parsing(@RequestParam("url") String url, @RequestParam("token") String token) {
      try {
         OkHttpClient client = this.okHttpsClient();
         Request request = (new Builder()).url(url).build();
         Response response = client.newCall(request).execute();
         String body = response.body().string();
         response.body().close();
         Pattern r = Pattern.compile("src=(.*)frameborder=\"0\"");
         Matcher matcher = r.matcher(body);

         while(matcher.find()) {
            if (!matcher.group(1).contains("/fn?v2")) {
               client = this.okHttpsClient();
               String fn = matcher.group(1).replace("\"", "");
               request = (new Builder()).url("https://www.lanzous.com" + fn).build();
               response = client.newCall(request).execute();
               body = response.body().string();
               response.body().close();
               r = Pattern.compile("var sg = (.*)");
               matcher = r.matcher(body);
               if (matcher.find()) {
                  String sg = matcher.group(1).replace("'", "").replace(";", "");
                  client = this.okHttpsClient();
                  FormBody post = (new okhttp3.FormBody.Builder()).add("action", "downprocess").add("sign", sg).add("ves", "1").build();
                  request = (new Builder()).url("https://www.lanzous.com/ajaxm.php").post(post).header("Referer", "https://www.lanzous.com" + fn).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36").build();
                  response = client.newCall(request).execute();
                  body = response.body().string();
                  response.body().close();
                  LzyResult.data lzy = (LzyResult.data)(new Gson()).fromJson(body, LzyResult.data.class);
                  if (lzy.getZt() == 1) {
                     String down_url = lzy.getDom() + "/file/" + lzy.getUrl();
                     client = this.okHttpsClient();
                     request = (new Builder()).url(down_url).header("accept-encoding", "gzip, deflate, br").header("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,zh-HK;q=0.7").header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9").header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36").build();
                     response = client.newCall(request).execute();
                     lzy.setDow(response.request().url().toString());
                     response.body().close();
                     return new LzyResult(200, "解析成功", lzy);
                  }

                  return new LzyResult(404, "解析失败");
               }
            }
         }
      } catch (Exception var14) {
         var14.printStackTrace();
      }

      return new LzyResult(404, "解析失败");
   }

   public LzyResult Parsing(String url) {
      try {
         OkHttpClient client = this.okHttpsClient();
         Request request = (new Builder()).url(url).build();
         Response response = client.newCall(request).execute();
         String body = response.body().string();
         response.body().close();
         Pattern r = Pattern.compile("src=(.*)frameborder=\"0\"");
         Matcher matcher = r.matcher(body);

         while(matcher.find()) {
            if (!matcher.group(1).contains("/fn?v2")) {
               client = this.okHttpsClient();
               String fn = matcher.group(1).replace("\"", "");
               request = (new Builder()).url("https://www.lanzous.com" + fn).build();
               response = client.newCall(request).execute();
               body = response.body().string();
               response.body().close();
               r = Pattern.compile("var sg = (.*)");
               matcher = r.matcher(body);
               if (matcher.find()) {
                  String sg = matcher.group(1).replace("'", "").replace(";", "");
                  client = this.okHttpsClient();
                  FormBody post = (new okhttp3.FormBody.Builder()).add("action", "downprocess").add("sign", sg).add("ves", "1").build();
                  request = (new Builder()).url("https://www.lanzous.com/ajaxm.php").post(post).header("Referer", "https://www.lanzous.com" + fn).header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36").build();
                  response = client.newCall(request).execute();
                  body = response.body().string();
                  response.body().close();
                  LzyResult.data lzy = (LzyResult.data)(new Gson()).fromJson(body, LzyResult.data.class);
                  if (lzy.getZt() == 1) {
                     String down_url = lzy.getDom() + "/file/" + lzy.getUrl();
                     client = this.okHttpsClient();
                     request = (new Builder()).url(down_url).header("accept-encoding", "gzip, deflate, br").header("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,zh-HK;q=0.7").header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9").header("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.88 Safari/537.36").build();
                     response = client.newCall(request).execute();
                     lzy.setDow(response.request().url().toString());
                     response.body().close();
                     return new LzyResult(200, "解析成功", lzy);
                  }

                  return new LzyResult(404, "解析失败");
               }
            }
         }
      } catch (Exception var13) {
         var13.printStackTrace();
      }

      return new LzyResult(404, "解析失败");
   }

   public OkHttpClient okHttpClient() {
      okhttp3.OkHttpClient.Builder builder = new okhttp3.OkHttpClient.Builder();
      builder.connectTimeout(30L, TimeUnit.SECONDS).readTimeout(30L, TimeUnit.SECONDS).writeTimeout(30L, TimeUnit.SECONDS).retryOnConnectionFailure(true);
      return builder.build();
   }

   public OkHttpClient okHttpsClient() {
      okhttp3.OkHttpClient.Builder builder = new okhttp3.OkHttpClient.Builder();
      builder.connectTimeout(30L, TimeUnit.SECONDS).readTimeout(30L, TimeUnit.SECONDS).writeTimeout(30L, TimeUnit.SECONDS).retryOnConnectionFailure(true).sslSocketFactory(this.getTrustedSSLSocketFactory()).hostnameVerifier(this.DO_NOT_VERIFY);
      return builder.build();
   }

   private SSLSocketFactory getTrustedSSLSocketFactory() {
      try {
         SSLContext sc = SSLContext.getInstance("SSL");
         sc.init((KeyManager[])null, this.trustAllCerts, new SecureRandom());
         return sc.getSocketFactory();
      } catch (NoSuchAlgorithmException | KeyManagementException var2) {
         var2.printStackTrace();
         return null;
      }
   }
}
