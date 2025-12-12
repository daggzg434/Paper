package com.googlecode.dex2jar.ir.ts.an;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.AssignStmt;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.ts.Cfg;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class BaseAnalyze<T extends AnalyzeValue> implements Cfg.FrameVisitor<T[]>, Cfg.TravelCallBack {
   protected static final boolean DEBUG = false;
   public List<T> aValues;
   private boolean reindexLocal;
   private T[] currentFrame;
   protected int localSize;
   protected IrMethod method;
   private T[] tmpFrame;

   public BaseAnalyze(IrMethod method) {
      this(method, true);
   }

   public BaseAnalyze(IrMethod method, boolean reindexLocal) {
      this.aValues = new ArrayList();
      this.method = method;
      if (!reindexLocal) {
         int maxReg = -1;
         Iterator var4 = method.locals.iterator();

         while(var4.hasNext()) {
            Local local = (Local)var4.next();
            if (local._ls_index > maxReg) {
               maxReg = local._ls_index;
            }
         }

         this.localSize = maxReg + 1;
      } else {
         this.localSize = method.locals.size();
      }

      this.reindexLocal = reindexLocal;
   }

   public void analyze() {
      this.init();
      this.analyze0();
      this.analyzeValue();
   }

   protected void analyze0() {
      this.tmpFrame = this.newFrame(this.localSize);
      Cfg.dfs(this.method.stmts, this);
      this.tmpFrame = null;
   }

   protected void analyzeValue() {
   }

   protected void afterExec(T[] frame, Stmt stmt) {
   }

   public T[] exec(T[] frame, Stmt stmt) {
      this.currentFrame = frame;

      try {
         Cfg.travel((Stmt)stmt, this, false);
      } catch (Exception var4) {
         throw new RuntimeException("fail exe " + stmt, var4);
      }

      frame = this.currentFrame;
      this.currentFrame = null;
      this.afterExec(frame, stmt);
      return frame;
   }

   protected T getFromFrame(int idx) {
      return this.currentFrame[idx];
   }

   protected T[] getFrame(Stmt stmt) {
      return (AnalyzeValue[])((AnalyzeValue[])stmt.frame);
   }

   protected void setFrame(Stmt stmt, T[] frame) {
      stmt.frame = frame;
   }

   protected void init() {
      if (this.reindexLocal) {
         int index = 0;

         Local local;
         for(Iterator var2 = this.method.locals.iterator(); var2.hasNext(); local._ls_index = index++) {
            local = (Local)var2.next();
         }
      }

      this.initCFG();
   }

   protected void initCFG() {
      Cfg.createCFG(this.method);
   }

   protected T[] newFrame() {
      return this.newFrame(this.localSize);
   }

   public T[] initFirstFrame(Stmt first) {
      return this.newFrame(this.localSize);
   }

   protected abstract T[] newFrame(int size);

   protected abstract T newValue();

   public Local onAssign(Local local, AssignStmt as) {
      System.arraycopy(this.currentFrame, 0, this.tmpFrame, 0, this.localSize);
      this.currentFrame = this.tmpFrame;
      T aValue = this.onAssignLocal(local, as.op2);
      this.aValues.add(aValue);
      this.currentFrame[local._ls_index] = aValue;
      return local;
   }

   protected T onAssignLocal(Local local, Value value) {
      return this.newValue();
   }

   public Local onUse(Local local) {
      T aValue = this.currentFrame[local._ls_index];
      this.onUseLocal(aValue, local);
      return local;
   }

   protected void onUseLocal(T aValue, Local local) {
   }

   public String toString() {
      StringBuilder sb = new StringBuilder();

      for(Stmt stmt = this.method.stmts.getFirst(); stmt != null; stmt = stmt.getNext()) {
         T[] frame = (AnalyzeValue[])((AnalyzeValue[])stmt.frame);
         if (frame != null) {
            AnalyzeValue[] var4 = frame;
            int var5 = frame.length;

            for(int var6 = 0; var6 < var5; ++var6) {
               T p = var4[var6];
               if (p == null) {
                  sb.append('.');
               } else {
                  sb.append(p.toRsp());
               }
            }

            sb.append(" | ");
         }

         sb.append(stmt.toString()).append('\n');
      }

      return sb.toString();
   }
}
