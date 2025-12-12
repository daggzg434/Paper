package com.googlecode.dex2jar.ir.ts.an;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.ts.UniqueQueue;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;

public class SimpleLiveAnalyze extends BaseAnalyze<SimpleLiveValue> {
   protected Set<SimpleLiveValue> markUsed() {
      Set<SimpleLiveValue> used = new HashSet(this.aValues.size() / 2);
      Queue<SimpleLiveValue> q = new UniqueQueue();
      Iterator var3 = this.aValues.iterator();

      label58:
      while(true) {
         SimpleLiveValue sv;
         do {
            if (!var3.hasNext()) {
               return used;
            }

            sv = (SimpleLiveValue)var3.next();
         } while(!sv.used);

         q.add(sv);

         while(true) {
            SimpleLiveValue v;
            do {
               do {
                  do {
                     if (q.isEmpty()) {
                        continue label58;
                     }

                     v = (SimpleLiveValue)q.poll();
                  } while(!v.used);
               } while(used.contains(v));

               used.add(v);
               SimpleLiveValue p = v.parent;
               if (p != null && !p.used) {
                  p.used = true;
                  q.add(p);
               }
            } while(v.otherParents == null);

            Iterator var8 = v.otherParents.iterator();

            while(var8.hasNext()) {
               SimpleLiveValue p = (SimpleLiveValue)var8.next();
               if (!p.used) {
                  p.used = true;
                  q.add(p);
               }
            }

            v.otherParents = null;
         }
      }
   }

   protected void analyzeValue() {
      this.markUsed();
   }

   public int getLocalSize() {
      return this.localSize;
   }

   public SimpleLiveAnalyze(IrMethod method, boolean reindexLocal) {
      super(method, reindexLocal);
   }

   protected SimpleLiveValue onAssignLocal(Local local, Value value) {
      SimpleLiveValue v = (SimpleLiveValue)super.onAssignLocal(local, value);
      v.used = true;
      return v;
   }

   protected void onUseLocal(SimpleLiveValue aValue, Local local) {
      aValue.used = true;
      super.onUseLocal(aValue, local);
   }

   public SimpleLiveValue[] merge(SimpleLiveValue[] srcFrame, SimpleLiveValue[] distFrame, Stmt src, Stmt dist) {
      int i;
      SimpleLiveValue sV;
      SimpleLiveValue dV;
      if (distFrame == null) {
         distFrame = new SimpleLiveValue[this.localSize];

         for(i = 0; i < srcFrame.length; ++i) {
            sV = srcFrame[i];
            if (sV != null) {
               dV = new SimpleLiveValue();
               this.aValues.add(dV);
               dV.parent = sV;
               distFrame[i] = dV;
            }
         }
      } else {
         for(i = 0; i < srcFrame.length; ++i) {
            sV = srcFrame[i];
            dV = distFrame[i];
            if (sV != null && dV != null) {
               List<SimpleLiveValue> ps = dV.otherParents;
               if (ps == null) {
                  dV.otherParents = (List)(ps = new ArrayList(3));
               }

               ((List)ps).add(sV);
            }
         }
      }

      return distFrame;
   }

   protected SimpleLiveValue[] newFrame(int size) {
      return new SimpleLiveValue[size];
   }

   protected SimpleLiveValue newValue() {
      return new SimpleLiveValue();
   }
}
