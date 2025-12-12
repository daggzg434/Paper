package com.googlecode.dex2jar.ir.ts;

import com.googlecode.d2j.DexType;
import com.googlecode.dex2jar.ir.IrMethod;
import com.googlecode.dex2jar.ir.StmtTraveler;
import com.googlecode.dex2jar.ir.expr.Constant;
import com.googlecode.dex2jar.ir.expr.Exprs;
import com.googlecode.dex2jar.ir.expr.FilledArrayExpr;
import com.googlecode.dex2jar.ir.expr.InvokeExpr;
import com.googlecode.dex2jar.ir.expr.StaticFieldExpr;
import com.googlecode.dex2jar.ir.expr.TypeExpr;
import com.googlecode.dex2jar.ir.expr.Value;

public class MultiArrayTransformer extends StatedTransformer {
   public boolean transformReportChanged(IrMethod method) {
      final boolean[] changed = new boolean[]{false};
      (new StmtTraveler() {
         public Value travel(Value op) {
            TypeExpr te;
            if (((Value)opx).vt == Value.VT.CHECK_CAST) {
               te = (TypeExpr)opx;
               if (te.op.vt == Value.VT.CHECK_CAST) {
                  TypeExpr te2 = (TypeExpr)te.op;
                  if (te.type.equals(te2.type)) {
                     opx = te2;
                  }
               }
            }

            Value op = super.travel((Value)opx);
            if (op.vt == Value.VT.CHECK_CAST) {
               te = (TypeExpr)op;
               if (te.type.charAt(0) == '[') {
                  Value from = te.getOp();
                  if (from.vt == Value.VT.INVOKE_STATIC) {
                     InvokeExpr invokeExpr = (InvokeExpr)from;
                     if (invokeExpr.getName().equals("newInstance") && invokeExpr.getOwner().equals("Ljava/lang/reflect/Array;") && invokeExpr.getArgs().length == 2 && invokeExpr.getArgs()[0].equals("Ljava/lang/Class;")) {
                        Value arg0 = invokeExpr.getOps()[0];
                        String elementType = null;
                        if (arg0.vt == Value.VT.CONSTANT) {
                           elementType = ((DexType)((Constant)invokeExpr.getOps()[0]).value).desc;
                        } else if (arg0.vt == Value.VT.STATIC_FIELD) {
                           StaticFieldExpr sfe = (StaticFieldExpr)arg0;
                           if (sfe.owner.startsWith("Ljava/lang/") && sfe.name.equals("TYPE")) {
                              String var8 = sfe.owner;
                              byte var9 = -1;
                              switch(var8.hashCode()) {
                              case -1405192707:
                                 if (var8.equals("Ljava/lang/Integer;")) {
                                    var9 = 4;
                                 }
                                 break;
                              case 30795859:
                                 if (var8.equals("Ljava/lang/Boolean;")) {
                                    var9 = 0;
                                 }
                                 break;
                              case 811419466:
                                 if (var8.equals("Ljava/lang/Double;")) {
                                    var9 = 7;
                                 }
                                 break;
                              case 1388882290:
                                 if (var8.equals("Ljava/lang/Character;")) {
                                    var9 = 3;
                                 }
                                 break;
                              case 1604503711:
                                 if (var8.equals("Ljava/lang/Float;")) {
                                    var9 = 6;
                                 }
                                 break;
                              case 1849571571:
                                 if (var8.equals("Ljava/lang/Byte;")) {
                                    var9 = 1;
                                 }
                                 break;
                              case 1858503167:
                                 if (var8.equals("Ljava/lang/Long;")) {
                                    var9 = 5;
                                 }
                                 break;
                              case 1867733479:
                                 if (var8.equals("Ljava/lang/Void;")) {
                                    var9 = 8;
                                 }
                                 break;
                              case 1973004927:
                                 if (var8.equals("Ljava/lang/Short;")) {
                                    var9 = 2;
                                 }
                              }

                              switch(var9) {
                              case 0:
                                 elementType = "Z";
                                 break;
                              case 1:
                                 elementType = "B";
                                 break;
                              case 2:
                                 elementType = "S";
                                 break;
                              case 3:
                                 elementType = "C";
                                 break;
                              case 4:
                                 elementType = "I";
                                 break;
                              case 5:
                                 elementType = "J";
                                 break;
                              case 6:
                                 elementType = "F";
                                 break;
                              case 7:
                                 elementType = "D";
                                 break;
                              case 8:
                                 elementType = "V";
                              }
                           }
                        }

                        if (elementType != null) {
                           Value dt = invokeExpr.getOps()[1];
                           if (invokeExpr.getArgs()[1].equals("I")) {
                              if (te.type.equals("[" + elementType)) {
                                 int d;
                                 for(d = 0; elementType.charAt(d) == '['; ++d) {
                                 }

                                 changed[0] = true;
                                 if (d > 0) {
                                    return Exprs.nNewMutiArray(elementType.substring(d), d + 1, new Value[]{dt});
                                 }

                                 return Exprs.nNewArray(elementType, dt);
                              }
                           } else if (dt.vt == Value.VT.FILLED_ARRAY) {
                              FilledArrayExpr filledArrayExpr = (FilledArrayExpr)dt;
                              int dx = filledArrayExpr.getOps().length;
                              if (te.type.length() > dx && te.type.substring(dx).equals(elementType)) {
                                 int d1;
                                 for(d1 = 0; elementType.charAt(d1) == '['; ++d1) {
                                 }

                                 changed[0] = true;
                                 return Exprs.nNewMutiArray(elementType.substring(d1), d1 + dx, filledArrayExpr.getOps());
                              }
                           }
                        }
                     }
                  }
               }
            }

            return op;
         }
      }).travel(method);
      return changed[0];
   }
}
