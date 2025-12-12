package com.verify.service.Utils;

import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.jf.dexlib2.AccessFlags;
import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.Method;
import org.jf.dexlib2.iface.MethodImplementation;
import org.jf.dexlib2.iface.MethodParameter;
import org.jf.dexlib2.iface.instruction.Instruction;
import org.jf.dexlib2.immutable.ImmutableMethod;
import org.jf.dexlib2.immutable.ImmutableMethodImplementation;
import org.jf.dexlib2.immutable.ImmutableMethodParameter;
import org.jf.dexlib2.immutable.instruction.ImmutableInstruction35c;

public class SingleRun {
   public static Method Auto(Method Method) {
      List<Instruction> newInsts = new ArrayList();
      int regIndex = Method.getImplementation().getRegisterCount();
      List<MethodParameter> params = new ArrayList();
      params.add(new ImmutableMethodParameter("Landroid/app/Activity;", (ImmutableSet)null, (String)null));
      newInsts.add(new ImmutableInstruction35c(Opcode.INVOKE_STATIC, 1, regIndex - Method.getParameters().size() - 1, 0, 0, 0, 0, new ImmutableMethod("Lcrc642c631ec79b49b81e/App;", "Init", params, "V", AccessFlags.PUBLIC.getValue(), (Set)null, (MethodImplementation)null)));
      Method.getImplementation().getInstructions().forEach((Instruction) -> {
         newInsts.add(Instruction);
      });
      ImmutableMethodImplementation newmi = new ImmutableMethodImplementation(regIndex, newInsts, Method.getImplementation().getTryBlocks(), Method.getImplementation().getDebugItems());
      return new ImmutableMethod(Method.getDefiningClass(), Method.getName(), Method.getParameters(), Method.getReturnType(), Method.getAccessFlags(), Method.getAnnotations(), newmi);
   }
}
