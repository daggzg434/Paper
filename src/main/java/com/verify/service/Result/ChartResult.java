package com.verify.service.Result;

import java.util.List;

public class ChartResult {
   public int code;
   public String msg;
   public ChartResult.data data;

   public ChartResult(int code, String msg) {
      this.code = code;
      this.msg = msg;
   }

   public int getCode() {
      return this.code;
   }

   public void setCode(int code) {
      this.code = code;
   }

   public String getMsg() {
      return this.msg;
   }

   public void setMsg(String msg) {
      this.msg = msg;
   }

   public ChartResult.data getData() {
      return this.data;
   }

   public void setData(ChartResult.data data) {
      this.data = data;
   }

   public static class data {
      public List<ChartResult.data.ChartInfo> chartInfos;

      public List<ChartResult.data.ChartInfo> getChartInfos() {
         return this.chartInfos;
      }

      public void setChartInfos(List<ChartResult.data.ChartInfo> chartInfos) {
         this.chartInfos = chartInfos;
      }

      public data(List<ChartResult.data.ChartInfo> chartInfos) {
         this.chartInfos = chartInfos;
      }

      public static class ChartInfo {
         public String time;
         public int usr_count;
         public int start_count;

         public ChartInfo(String time, int usr_count, int start_count) {
            this.time = time;
            this.usr_count = usr_count;
            this.start_count = start_count;
         }

         public String getTime() {
            return this.time;
         }

         public void setTime(String time) {
            this.time = time;
         }

         public int getUsr_count() {
            return this.usr_count;
         }

         public void setUsr_count(int usr_count) {
            this.usr_count = usr_count;
         }

         public int getStart_count() {
            return this.start_count;
         }

         public void setStart_count(int start_count) {
            this.start_count = start_count;
         }
      }
   }
}
