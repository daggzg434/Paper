package com.googlecode.d2j.dex.writer.item;

import com.googlecode.d2j.dex.writer.insn.Label;
import com.googlecode.d2j.dex.writer.io.DataOut;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DebugInfoItem extends BaseItem {
   public List<DebugInfoItem.DNode> debugNodes = new ArrayList();
   public StringIdItem[] parameterNames;
   public int firstLine;
   public StringIdItem fileName;
   static final int DBG_END_SEQUENCE = 0;
   static final int DBG_ADVANCE_PC = 1;
   static final int DBG_ADVANCE_LINE = 2;
   static final int DBG_START_LOCAL = 3;
   static final int DBG_START_LOCAL_EXTENDED = 4;
   static final int DBG_END_LOCAL = 5;
   static final int DBG_RESTART_LOCAL = 6;
   static final int DBG_SET_PROLOGUE_END = 7;
   static final int DBG_SET_EPILOGUE_BEGIN = 8;
   static final int DBG_SET_FILE = 9;
   static final int DBG_FIRST_SPECIAL = 10;
   static final int DBG_LINE_BASE = -4;
   static final int DBG_LINE_RANGE = 15;

   public int place(int offset) {
      offset += lengthOfUleb128(this.firstLine);
      int addr;
      if (this.parameterNames == null) {
         offset += lengthOfUleb128(0);
      } else {
         offset += lengthOfUleb128(this.parameterNames.length);
         StringIdItem[] var2 = this.parameterNames;
         addr = var2.length;

         for(int var4 = 0; var4 < addr; ++var4) {
            StringIdItem s = var2[var4];
            offset += lengthOfUleb128(1 + (s == null ? -1 : s.index));
         }
      }

      int line = this.firstLine;
      addr = 0;
      if (this.fileName != null) {
         ++offset;
         offset += lengthOfUleb128(this.fileName.index + 1);
      }

      Iterator var9 = this.debugNodes.iterator();

      while(var9.hasNext()) {
         DebugInfoItem.DNode opNode = (DebugInfoItem.DNode)var9.next();
         int lineDelta;
         switch(opNode.op) {
         case 4:
            offset += lengthOfUleb128(opNode.sig.index + 1);
         case 3:
            lineDelta = opNode.label.offset - addr;
            if (lineDelta < 0) {
               throw new RuntimeException();
            }

            if (lineDelta > 0) {
               ++offset;
               offset += lengthOfUleb128(lineDelta);
            }

            addr = opNode.label.offset;
            ++offset;
            offset += lengthOfUleb128(opNode.reg);
            offset += lengthOfUleb128(opNode.name.index + 1);
            offset += lengthOfUleb128(opNode.type.index + 1);
            break;
         case 5:
         case 6:
            lineDelta = opNode.label.offset - addr;
            if (lineDelta < 0) {
               throw new RuntimeException();
            }

            if (lineDelta > 0) {
               ++offset;
               offset += lengthOfUleb128(lineDelta);
            }

            addr = opNode.label.offset;
            ++offset;
            offset += lengthOfUleb128(opNode.reg);
            break;
         case 7:
         case 8:
            ++offset;
            break;
         case 9:
            throw new RuntimeException();
         default:
            lineDelta = opNode.line - line;
            int addrDelta = opNode.label.offset - addr;
            if (addrDelta < 0) {
               throw new RuntimeException();
            }

            if (opNode.label.offset != 0 || lineDelta != 0 || addrDelta != 0) {
               if (lineDelta < -4 || lineDelta > 10 || addrDelta > 15) {
                  if (addrDelta > 15) {
                     ++offset;
                     offset += lengthOfUleb128(addrDelta);
                     boolean var12 = false;
                  }

                  if (lineDelta < -4 || lineDelta > 10) {
                     ++offset;
                     offset += lengthOfSleb128(lineDelta);
                     boolean var11 = false;
                  }
               }

               ++offset;
               line = opNode.line;
               addr = opNode.label.offset;
            }
         }
      }

      ++offset;
      return offset;
   }

   public void write(DataOut out) {
      out.uleb128("startline", this.firstLine);
      int addr;
      if (this.parameterNames == null) {
         out.uleb128("szParams", 0);
      } else {
         out.uleb128("szParams", this.parameterNames.length);
         StringIdItem[] var2 = this.parameterNames;
         addr = var2.length;

         for(int var4 = 0; var4 < addr; ++var4) {
            StringIdItem s = var2[var4];
            out.uleb128p1("param_name_index", s == null ? -1 : s.index);
         }
      }

      int line = this.firstLine;
      addr = 0;
      if (this.fileName != null) {
         out.sbyte("DBG_SET_FILE", 9);
         out.uleb128p1("filename", this.fileName.index);
      }

      Iterator var10 = this.debugNodes.iterator();

      while(var10.hasNext()) {
         DebugInfoItem.DNode opNode = (DebugInfoItem.DNode)var10.next();
         int lineDelta;
         switch(opNode.op) {
         case 3:
            lineDelta = opNode.label.offset - addr;
            if (lineDelta < 0) {
               throw new RuntimeException();
            }

            if (lineDelta > 0) {
               this.addAdvancePC(out, lineDelta);
            }

            addr = opNode.label.offset;
            out.sbyte("DBG_START_LOCAL", 3);
            out.uleb128("reg", opNode.reg);
            out.uleb128p1("name", opNode.name.index);
            out.uleb128p1("type", opNode.type.index);
            break;
         case 4:
            lineDelta = opNode.label.offset - addr;
            if (lineDelta < 0) {
               throw new RuntimeException();
            }

            if (lineDelta > 0) {
               this.addAdvancePC(out, lineDelta);
            }

            addr = opNode.label.offset;
            out.sbyte("DBG_START_LOCAL_EXTENDED", 4);
            out.uleb128("reg", opNode.reg);
            out.uleb128p1("name", opNode.name.index);
            out.uleb128p1("type", opNode.type.index);
            out.uleb128p1("sig", opNode.sig.index);
            break;
         case 5:
            lineDelta = opNode.label.offset - addr;
            if (lineDelta < 0) {
               throw new RuntimeException();
            }

            if (lineDelta > 0) {
               this.addAdvancePC(out, lineDelta);
            }

            addr = opNode.label.offset;
            out.sbyte("DBG_END_LOCAL", 5);
            out.uleb128("reg", opNode.reg);
            break;
         case 6:
            lineDelta = opNode.label.offset - addr;
            if (lineDelta < 0) {
               throw new RuntimeException();
            }

            if (lineDelta > 0) {
               this.addAdvancePC(out, lineDelta);
            }

            addr = opNode.label.offset;
            out.sbyte("DBG_RESTART_LOCAL", 6);
            out.uleb128("reg", opNode.reg);
            break;
         case 7:
            out.sbyte("DBG_SET_PROLOGUE_END", 7);
            break;
         case 8:
            out.sbyte("DBG_SET_EPILOGUE_BEGIN", 8);
            break;
         case 9:
            throw new RuntimeException();
         default:
            lineDelta = opNode.line - line;
            int addrDelta = opNode.label.offset - addr;
            if (addrDelta < 0) {
               throw new RuntimeException();
            }

            if (opNode.label.offset != 0 || lineDelta != 0 || addrDelta != 0) {
               if (lineDelta < -4 || lineDelta > 10 || addrDelta > 15) {
                  if (addrDelta > 15) {
                     this.addAdvancePC(out, addrDelta);
                     addrDelta = 0;
                  }

                  if (lineDelta < -4 || lineDelta > 10) {
                     this.addAdvanceLine(out, lineDelta);
                     lineDelta = 0;
                  }
               }

               int op = lineDelta + 4 + addrDelta * 15 + 10;
               out.sbyte("DEBUG_OP_X", op);
               line = opNode.line;
               addr = opNode.label.offset;
            }
         }
      }

      out.sbyte("DBG_END_SEQUENCE", 0);
   }

   private void addAdvanceLine(DataOut out, int lineDelta) {
      out.sbyte("DBG_ADVANCE_LINE", 2);
      out.sleb128("offset", lineDelta);
   }

   private void addAdvancePC(DataOut out, int delta) {
      out.sbyte("DBG_ADVANCE_PC", 1);
      out.uleb128("offset", delta);
   }

   public static class DNode {
      public int op;
      public int reg;
      public int line;
      public Label label;
      StringIdItem name;
      TypeIdItem type;
      StringIdItem sig;

      public static DebugInfoItem.DNode startLocal(int reg, Label label, StringIdItem name, TypeIdItem type) {
         DebugInfoItem.DNode node = new DebugInfoItem.DNode();
         node.reg = reg;
         node.label = label;
         node.name = name;
         node.type = type;
         node.op = 3;
         return node;
      }

      public static DebugInfoItem.DNode line(int line, Label label) {
         DebugInfoItem.DNode node = new DebugInfoItem.DNode();
         node.line = line;
         node.label = label;
         node.op = 99999;
         return node;
      }

      public static DebugInfoItem.DNode startLocalEx(int reg, Label label, StringIdItem name, TypeIdItem type, StringIdItem sig) {
         DebugInfoItem.DNode node = new DebugInfoItem.DNode();
         node.reg = reg;
         node.label = label;
         node.name = name;
         node.type = type;
         node.sig = sig;
         node.op = 4;
         return node;
      }

      public static DebugInfoItem.DNode endLocal(int reg, Label label) {
         DebugInfoItem.DNode node = new DebugInfoItem.DNode();
         node.reg = reg;
         node.label = label;
         node.op = 5;
         return node;
      }

      public static DebugInfoItem.DNode restartLocal(int reg, Label label) {
         DebugInfoItem.DNode node = new DebugInfoItem.DNode();
         node.reg = reg;
         node.label = label;
         node.op = 6;
         return node;
      }

      public static DebugInfoItem.DNode epiogue(Label label) {
         DebugInfoItem.DNode node = new DebugInfoItem.DNode();
         node.label = label;
         node.op = 8;
         return node;
      }

      public static DebugInfoItem.DNode prologue(Label label) {
         DebugInfoItem.DNode node = new DebugInfoItem.DNode();
         node.label = label;
         node.op = 7;
         return node;
      }
   }
}
