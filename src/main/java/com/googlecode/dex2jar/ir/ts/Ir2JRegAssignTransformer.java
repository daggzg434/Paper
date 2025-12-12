package com.googlecode.dex2jar.ir.ts;

import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.expr.Local;
import com.googlecode.dex2jar.ir.expr.RefExpr;
import com.googlecode.dex2jar.ir.expr.Value;
import com.googlecode.dex2jar.ir.stmt.Stmt;
import com.googlecode.dex2jar.ir.ts.an.SimpleLiveAnalyze;
import com.googlecode.dex2jar.ir.ts.an.SimpleLiveValue;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class Ir2JRegAssignTransformer implements Transformer {
   private static final Comparator<Ir2JRegAssignTransformer.Reg> OrderRegAssignByPreferredSizeDesc = new Comparator<Ir2JRegAssignTransformer.Reg>() {
      public int compare(Ir2JRegAssignTransformer.Reg o1, Ir2JRegAssignTransformer.Reg o2) {
         int x = o2.prefers.size() - o1.prefers.size();
         if (x == 0) {
            x = o2.excludes.size() - o1.excludes.size();
         }

         return x;
      }
   };

   private Ir2JRegAssignTransformer.Reg[] genGraph(IrMethod method, final Ir2JRegAssignTransformer.Reg[] regs) {
      Ir2JRegAssignTransformer.Reg[] args;
      if (method.isStatic) {
         args = new Ir2JRegAssignTransformer.Reg[method.args.length];
      } else {
         args = new Ir2JRegAssignTransformer.Reg[method.args.length + 1];
      }

      Set<Stmt> tos = new HashSet();
      Iterator var5 = method.stmts.iterator();

      label82:
      while(true) {
         Stmt stmt;
         do {
            do {
               if (!var5.hasNext()) {
                  Ir2JRegAssignTransformer.Reg[] var17 = regs;
                  int var18 = regs.length;

                  for(int var19 = 0; var19 < var18; ++var19) {
                     Ir2JRegAssignTransformer.Reg reg = var17[var19];
                     reg.excludes.remove(reg);
                     reg.prefers.remove(reg);
                  }

                  return args;
               }

               stmt = (Stmt)var5.next();
            } while(stmt.st != Stmt.ST.ASSIGN && stmt.st != Stmt.ST.IDENTITY);
         } while(stmt.getOp1().vt != Value.VT.LOCAL);

         Local left = (Local)stmt.getOp1();
         Value op2 = stmt.getOp2();
         int idx = left._ls_index;
         Ir2JRegAssignTransformer.Reg leftReg = regs[idx];
         Cfg.collectTos(stmt, tos);
         Iterator var11 = tos.iterator();

         while(true) {
            SimpleLiveValue[] frame;
            do {
               if (!var11.hasNext()) {
                  tos.clear();
                  if (op2.vt == Value.VT.LOCAL) {
                     Ir2JRegAssignTransformer.Reg rightReg = regs[((Local)op2)._ls_index];
                     leftReg.prefers.add(rightReg);
                     rightReg.prefers.add(leftReg);
                  }

                  if (op2.vt == Value.VT.THIS_REF) {
                     args[0] = leftReg;
                  } else if (op2.vt == Value.VT.PARAMETER_REF) {
                     RefExpr refExpr = (RefExpr)op2;
                     if (method.isStatic) {
                        args[refExpr.parameterIndex] = leftReg;
                     } else {
                        args[refExpr.parameterIndex + 1] = leftReg;
                     }
                  }
                  continue label82;
               }

               Stmt next = (Stmt)var11.next();
               frame = (SimpleLiveValue[])((SimpleLiveValue[])next.frame);
            } while(frame == null);

            for(int i = 0; i < frame.length; ++i) {
               if (i != idx) {
                  SimpleLiveValue v = frame[i];
                  if (v != null && v.used) {
                     Ir2JRegAssignTransformer.Reg rightReg = regs[i];
                     leftReg.excludes.add(rightReg);
                     rightReg.excludes.add(leftReg);
                  }
               }
            }
         }
      }
   }

   Map<Character, List<Ir2JRegAssignTransformer.Reg>> groupAndCleanUpByType(Ir2JRegAssignTransformer.Reg[] regs) {
      Map<Character, List<Ir2JRegAssignTransformer.Reg>> groups = new HashMap();
      Ir2JRegAssignTransformer.Reg[] var3 = regs;
      int var4 = regs.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Ir2JRegAssignTransformer.Reg reg = var3[var5];
         char simpleType = reg.type;
         List<Ir2JRegAssignTransformer.Reg> group = (List)groups.get(simpleType);
         if (group == null) {
            group = new ArrayList();
            groups.put(simpleType, group);
         }

         ((List)group).add(reg);
         Iterator it = reg.excludes.iterator();

         Ir2JRegAssignTransformer.Reg ex;
         while(it.hasNext()) {
            ex = (Ir2JRegAssignTransformer.Reg)it.next();
            if (ex.type != reg.type) {
               it.remove();
            }
         }

         it = reg.prefers.iterator();

         while(it.hasNext()) {
            ex = (Ir2JRegAssignTransformer.Reg)it.next();
            if (ex.type != reg.type) {
               it.remove();
            }
         }
      }

      return groups;
   }

   private void initExcludeColor(BitSet excludeColor, Ir2JRegAssignTransformer.Reg as) {
      excludeColor.clear();
      Iterator var3 = as.excludes.iterator();

      while(true) {
         Ir2JRegAssignTransformer.Reg ex;
         do {
            do {
               if (!var3.hasNext()) {
                  return;
               }

               ex = (Ir2JRegAssignTransformer.Reg)var3.next();
            } while(ex.reg < 0);

            excludeColor.set(ex.reg);
         } while(ex.type != 'J' && ex.type != 'D');

         excludeColor.set(ex.reg + 1);
      }
   }

   private void initSuggestColor(BitSet suggestColor, Ir2JRegAssignTransformer.Reg as) {
      suggestColor.clear();
      Iterator var3 = as.prefers.iterator();

      while(var3.hasNext()) {
         Ir2JRegAssignTransformer.Reg ex = (Ir2JRegAssignTransformer.Reg)var3.next();
         if (ex.reg >= 0) {
            suggestColor.set(ex.reg);
         }
      }

   }

   public void transform(IrMethod method) {
      if (method.locals.size() != 0) {
         SimpleLiveAnalyze sa = new SimpleLiveAnalyze(method, true);
         sa.analyze();
         int maxLocalSize = sa.getLocalSize();
         Ir2JRegAssignTransformer.Reg[] regs = new Ir2JRegAssignTransformer.Reg[maxLocalSize];

         Local local;
         Ir2JRegAssignTransformer.Reg reg;
         for(Iterator var5 = method.locals.iterator(); var5.hasNext(); regs[local._ls_index] = reg) {
            local = (Local)var5.next();
            reg = new Ir2JRegAssignTransformer.Reg();
            char type = local.valueType.charAt(0);
            if (type == '[') {
               type = 'L';
            }

            reg.type = type;
            local.tag = reg;
         }

         Ir2JRegAssignTransformer.Reg[] args = this.genGraph(method, regs);
         int j;
         if (!method.isStatic) {
            Ir2JRegAssignTransformer.Reg atThis = args[0];
            Ir2JRegAssignTransformer.Reg[] var22 = regs;
            j = regs.length;

            for(int var9 = 0; var9 < j; ++var9) {
               Ir2JRegAssignTransformer.Reg reg = var22[var9];
               if (reg != atThis) {
                  reg.excludes.add(atThis);
                  atThis.excludes.add(reg);
               }
            }
         }

         int i = 0;
         int index = 0;
         if (!method.isStatic) {
            args[i++].reg = index++;
         }

         for(j = 0; j < method.args.length; ++j) {
            Ir2JRegAssignTransformer.Reg reg = args[i++];
            String type = method.args[j];
            if (reg == null) {
               ++index;
            } else {
               reg.reg = index++;
            }

            if ("J".equals(type) || "D".equals(type)) {
               ++index;
            }
         }

         Map<Character, List<Ir2JRegAssignTransformer.Reg>> groups = this.groupAndCleanUpByType(regs);
         BitSet excludeColor = new BitSet();
         BitSet suggestColor = new BitSet();
         BitSet globalExcludes = new BitSet();
         BitSet usedInOneType = new BitSet();
         Iterator var11 = groups.entrySet().iterator();

         while(var11.hasNext()) {
            Entry<Character, List<Ir2JRegAssignTransformer.Reg>> e = (Entry)var11.next();
            List<Ir2JRegAssignTransformer.Reg> assigns = (List)e.getValue();
            Collections.sort(assigns, OrderRegAssignByPreferredSizeDesc);
            char type = (Character)e.getKey();
            boolean doubleOrLong = type == 'J' || type == 'D';
            Iterator var16 = assigns.iterator();

            while(var16.hasNext()) {
               Ir2JRegAssignTransformer.Reg as = (Ir2JRegAssignTransformer.Reg)var16.next();
               if (as.reg < 0) {
                  this.initExcludeColor(excludeColor, as);
                  this.excludeParameters(excludeColor, args, type);
                  excludeColor.or(globalExcludes);
                  this.initSuggestColor(suggestColor, as);

                  int reg;
                  for(reg = suggestColor.nextSetBit(0); reg >= 0; reg = suggestColor.nextSetBit(reg + 1)) {
                     if (doubleOrLong) {
                        if (!excludeColor.get(reg) && !excludeColor.get(reg + 1)) {
                           as.reg = reg;
                           break;
                        }
                     } else if (!excludeColor.get(reg)) {
                        as.reg = reg;
                        break;
                     }
                  }

                  if (as.reg < 0) {
                     if (!doubleOrLong) {
                        reg = excludeColor.nextClearBit(0);
                        as.reg = reg;
                     } else {
                        reg = -1;

                        do {
                           ++reg;
                           reg = excludeColor.nextClearBit(reg);
                        } while(excludeColor.get(reg + 1));

                        as.reg = reg;
                     }
                  }
               }

               usedInOneType.set(as.reg);
               if (doubleOrLong) {
                  usedInOneType.set(as.reg + 1);
               }
            }

            globalExcludes.or(usedInOneType);
            usedInOneType.clear();
         }

         Local local;
         for(var11 = method.locals.iterator(); var11.hasNext(); local.tag = null) {
            local = (Local)var11.next();
            Ir2JRegAssignTransformer.Reg as = (Ir2JRegAssignTransformer.Reg)local.tag;
            local._ls_index = as.reg;
         }

         Stmt stmt;
         for(var11 = method.stmts.iterator(); var11.hasNext(); stmt.frame = null) {
            stmt = (Stmt)var11.next();
         }

      }
   }

   private void excludeParameters(BitSet excludeColor, Ir2JRegAssignTransformer.Reg[] args, char type) {
      Ir2JRegAssignTransformer.Reg[] var4 = args;
      int var5 = args.length;

      for(int var6 = 0; var6 < var5; ++var6) {
         Ir2JRegAssignTransformer.Reg arg = var4[var6];
         if (arg.type != type) {
            excludeColor.set(arg.reg);
            if (arg.type == 'J' || arg.type == 'D') {
               excludeColor.set(arg.reg + 1);
            }
         }
      }

   }

   public static class Reg {
      public Set<Ir2JRegAssignTransformer.Reg> excludes = new HashSet(4);
      public Set<Ir2JRegAssignTransformer.Reg> prefers = new HashSet(3);
      int reg = -1;
      public char type;
   }
}
