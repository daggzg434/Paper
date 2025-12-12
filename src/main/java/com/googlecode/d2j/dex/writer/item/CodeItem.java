package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.CodeWriter;
import com.googlecode.d2j.dex.writer.insn.Insn;
import com.googlecode.d2j.dex.writer.insn.JumpOp;
import com.googlecode.d2j.dex.writer.insn.Label;
import com.googlecode.d2j.dex.writer.insn.PreBuildInsn;
import com.googlecode.d2j.dex.writer.io.DataOut;
import com.googlecode.d2j.reader.Op;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CodeItem extends BaseItem {
   public int registersSize;
   public int insSize;
   public int outsSize;
   public int insn_size;
   public List<CodeItem.TryItem> tries;
   public DebugInfoItem debugInfo;
   public List<Insn> insns;
   public List<CodeItem.EncodedCatchHandler> handlers;
   List<CodeItem.TryItem> _tryItems;
   List<Insn> _ops;
   List<Insn> _tailOps;

   public int place(int offset) {
      this.prepareInsns();
      this.prepareTries();
      offset += 16 + this.insn_size * 2;
      if (this.tries != null && this.tries.size() > 0) {
         if ((this.insn_size & 1) != 0) {
            offset += 2;
         }

         offset += 8 * this.tries.size();
         if (this.handlers.size() > 0) {
            int base = offset;
            offset += lengthOfUleb128(this.handlers.size());
            Iterator var3 = this.handlers.iterator();

            while(var3.hasNext()) {
               CodeItem.EncodedCatchHandler h = (CodeItem.EncodedCatchHandler)var3.next();
               h.handler_off = offset - base;
               int size = h.addPairs.size();
               offset += lengthOfSleb128(h.catchAll != null ? -size : size);

               CodeItem.EncodedCatchHandler.AddrPair ap;
               for(Iterator var6 = h.addPairs.iterator(); var6.hasNext(); offset += lengthOfUleb128(ap.type.index) + lengthOfUleb128(ap.addr.offset)) {
                  ap = (CodeItem.EncodedCatchHandler.AddrPair)var6.next();
               }

               if (h.catchAll != null) {
                  offset += lengthOfUleb128(h.catchAll.offset);
               }
            }
         }
      }

      return offset;
   }

   public void write(DataOut out) {
      out.ushort("registers_size", this.registersSize);
      out.ushort("ins_size", this.insSize);
      out.ushort("outs_size", this.outsSize);
      out.ushort("tries_size", this.tries == null ? 0 : this.tries.size());
      out.uint("debug_info_off", this.debugInfo == null ? 0 : this.debugInfo.offset);
      out.uint("insn_size", this.insn_size);
      ByteBuffer b = ByteBuffer.allocate(this.insn_size * 2).order(ByteOrder.LITTLE_ENDIAN);
      Iterator var3 = this.insns.iterator();

      while(var3.hasNext()) {
         Insn insn = (Insn)var3.next();
         insn.write(b);
      }

      out.bytes("insn", b.array());
      if (this.tries != null && this.tries.size() > 0) {
         if ((this.insn_size & 1) != 0) {
            out.skip("padding", 2);
         }

         int lastEnd = 0;
         Iterator var10 = this.tries.iterator();

         while(var10.hasNext()) {
            CodeItem.TryItem ti = (CodeItem.TryItem)var10.next();
            if (ti.start.offset < lastEnd) {
               System.err.println("'Out-of-order try' may throwed by libdex");
            }

            out.uint("start_addr", ti.start.offset);
            out.ushort("insn_count", ti.end.offset - ti.start.offset);
            lastEnd = ti.end.offset;
            out.ushort("handler_off", ti.handler.handler_off);
         }

         if (this.handlers.size() > 0) {
            out.uleb128("size", this.handlers.size());
            var10 = this.handlers.iterator();

            while(var10.hasNext()) {
               CodeItem.EncodedCatchHandler h = (CodeItem.EncodedCatchHandler)var10.next();
               int size = h.addPairs.size();
               out.sleb128("size", h.catchAll != null ? -size : size);
               Iterator var7 = h.addPairs.iterator();

               while(var7.hasNext()) {
                  CodeItem.EncodedCatchHandler.AddrPair ap = (CodeItem.EncodedCatchHandler.AddrPair)var7.next();
                  out.uleb128("type_idx", ap.type.index);
                  out.uleb128("addr", ap.addr.offset);
               }

               if (h.catchAll != null) {
                  out.uleb128("catch_all_addr", h.catchAll.offset);
               }
            }
         }
      }

   }

   public void init(List<Insn> ops, List<Insn> tailOps, List<CodeItem.TryItem> tryItems) {
      this._ops = ops;
      this._tailOps = tailOps;
      this._tryItems = tryItems;
   }

   private void prepareTries() {
      if (this._tryItems.size() > 0) {
         List<CodeItem.TryItem> uniqTrys = new ArrayList();
         Set<CodeItem.TryItem> set = new HashSet();
         Iterator var3 = this._tryItems.iterator();

         while(true) {
            while(var3.hasNext()) {
               CodeItem.TryItem tryItem = (CodeItem.TryItem)var3.next();
               if (!set.contains(tryItem)) {
                  uniqTrys.add(tryItem);
                  set.add(tryItem);
               } else {
                  Iterator var5 = uniqTrys.iterator();

                  while(var5.hasNext()) {
                     CodeItem.TryItem t = (CodeItem.TryItem)var5.next();
                     if (t.equals(tryItem)) {
                        this.mergeExceptionHandler(t.handler, tryItem.handler);
                     }
                  }
               }
            }

            set.clear();
            this.tries = uniqTrys;
            if (uniqTrys.size() > 0) {
               Collections.sort(uniqTrys, new Comparator<CodeItem.TryItem>() {
                  public int compare(CodeItem.TryItem o1, CodeItem.TryItem o2) {
                     int x = o1.start.offset - o2.start.offset;
                     if (x == 0) {
                        x = o1.end.offset - o2.end.offset;
                     }

                     return x;
                  }
               });
            }

            List<CodeItem.EncodedCatchHandler> uniqHanders = new ArrayList();
            Map<CodeItem.EncodedCatchHandler, CodeItem.EncodedCatchHandler> map = new HashMap();
            Iterator var10 = uniqTrys.iterator();

            while(var10.hasNext()) {
               CodeItem.TryItem tryItem = (CodeItem.TryItem)var10.next();
               CodeItem.EncodedCatchHandler d = tryItem.handler;
               CodeItem.EncodedCatchHandler uH = (CodeItem.EncodedCatchHandler)map.get(d);
               if (uH != null) {
                  tryItem.handler = uH;
               } else {
                  uniqHanders.add(d);
                  map.put(d, d);
               }
            }

            this.handlers = uniqHanders;
            map.clear();
            break;
         }
      }

   }

   private void mergeExceptionHandler(CodeItem.EncodedCatchHandler to, CodeItem.EncodedCatchHandler from) {
      Iterator var3 = from.addPairs.iterator();

      while(var3.hasNext()) {
         CodeItem.EncodedCatchHandler.AddrPair pair = (CodeItem.EncodedCatchHandler.AddrPair)var3.next();
         if (!to.addPairs.contains(pair)) {
            to.addPairs.add(pair);
         }
      }

      if (to.catchAll == null) {
         to.catchAll = from.catchAll;
      }

   }

   private void prepareInsns() {
      List<JumpOp> jumpOps = new ArrayList();
      Iterator var2 = this._ops.iterator();

      while(var2.hasNext()) {
         Insn insn = (Insn)var2.next();
         if (insn instanceof CodeWriter.IndexedInsn) {
            ((CodeWriter.IndexedInsn)insn).fit();
         } else if (insn instanceof JumpOp) {
            jumpOps.add((JumpOp)insn);
         }
      }

      int codeSize = 0;

      while(true) {
         Insn insn;
         Iterator var7;
         for(var7 = this._ops.iterator(); var7.hasNext(); codeSize += insn.getCodeUnitSize()) {
            insn = (Insn)var7.next();
            insn.offset = codeSize;
         }

         boolean allfit = true;
         Iterator var9 = jumpOps.iterator();

         while(var9.hasNext()) {
            JumpOp jop = (JumpOp)var9.next();
            if (!jop.fit()) {
               allfit = false;
            }
         }

         if (allfit) {
            var7 = this._tailOps.iterator();

            while(var7.hasNext()) {
               insn = (Insn)var7.next();
               if ((codeSize & 1) != 0) {
                  Insn nop = new PreBuildInsn(new byte[]{(byte)Op.NOP.opcode, 0});
                  insn.offset = codeSize;
                  codeSize += nop.getCodeUnitSize();
                  this._ops.add(nop);
               }

               insn.offset = codeSize;
               codeSize += insn.getCodeUnitSize();
               this._ops.add(insn);
            }

            this._tailOps.clear();
            this.insns = this._ops;
            this.insn_size = codeSize;
            return;
         }

         codeSize = 0;
      }
   }

   public static class TryItem {
      public Label start;
      public Label end;
      public CodeItem.EncodedCatchHandler handler;

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            CodeItem.TryItem tryItem = (CodeItem.TryItem)o;
            if (this.end.offset != tryItem.end.offset) {
               return false;
            } else {
               return this.start.offset == tryItem.start.offset;
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         int result = this.start.offset;
         result = 31 * result + this.end.offset;
         return result;
      }
   }

   public static class EncodedCatchHandler {
      public int handler_off;
      public List<CodeItem.EncodedCatchHandler.AddrPair> addPairs;
      public Label catchAll;

      public boolean equals(Object o) {
         if (this == o) {
            return true;
         } else if (o != null && this.getClass() == o.getClass()) {
            CodeItem.EncodedCatchHandler that = (CodeItem.EncodedCatchHandler)o;
            if (!this.addPairs.equals(that.addPairs)) {
               return false;
            } else {
               if (this.catchAll != null) {
                  if (!this.catchAll.equals(that.catchAll)) {
                     return false;
                  }
               } else if (that.catchAll != null) {
                  return false;
               }

               return true;
            }
         } else {
            return false;
         }
      }

      public int hashCode() {
         int result = this.addPairs.hashCode();
         result = 31 * result + (this.catchAll != null ? this.catchAll.offset : 0);
         return result;
      }

      public static class AddrPair {
         public final TypeIdItem type;
         public final Label addr;

         public AddrPair(TypeIdItem type, Label addr) {
            this.type = type;
            this.addr = addr;
         }

         public boolean equals(Object o) {
            if (this == o) {
               return true;
            } else if (o != null && this.getClass() == o.getClass()) {
               CodeItem.EncodedCatchHandler.AddrPair addrPair = (CodeItem.EncodedCatchHandler.AddrPair)o;
               if (this.addr.offset != addrPair.addr.offset) {
                  return false;
               } else {
                  return this.type.equals(addrPair.type);
               }
            } else {
               return false;
            }
         }

         public int hashCode() {
            int result = this.type.hashCode();
            result = 31 * result + this.addr.offset;
            return result;
         }
      }
   }
}
