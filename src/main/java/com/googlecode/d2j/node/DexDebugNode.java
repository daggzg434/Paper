package com.googlecode.d2j.node;

import com.googlecode.d2j.DexLabel;
import com.googlecode.d2j.visitors.DexDebugVisitor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DexDebugNode extends DexDebugVisitor {
   public List<DexDebugNode.DexDebugOpNode> debugNodes = new ArrayList();
   public List<String> parameterNames;
   public String fineName;

   protected void addDebug(DexDebugNode.DexDebugOpNode dexDebugNode) {
      this.debugNodes.add(dexDebugNode);
   }

   public void visitSetFile(String file) {
      this.fineName = file;
   }

   public void visitRestartLocal(int reg, DexLabel label) {
      this.addDebug(new DexDebugNode.DexDebugOpNode.RestartLocal(label, reg));
   }

   public void visitParameterName(final int parameterIndex, final String name) {
      if (this.parameterNames == null) {
         this.parameterNames = new ArrayList();
      }

      while(this.parameterNames.size() <= parameterIndex) {
         this.parameterNames.add((Object)null);
      }

      this.parameterNames.set(parameterIndex, name);
   }

   public void visitLineNumber(final int line, final DexLabel label) {
      this.addDebug(new DexDebugNode.DexDebugOpNode.LineNumber(label, line));
   }

   public void visitStartLocal(int reg, DexLabel label, String name, String type, String signature) {
      this.addDebug(new DexDebugNode.DexDebugOpNode.StartLocalNode(label, reg, name, type, signature));
   }

   public void visitEndLocal(int reg, DexLabel label) {
      this.addDebug(new DexDebugNode.DexDebugOpNode.EndLocal(label, reg));
   }

   public void accept(DexDebugVisitor v) {
      if (this.parameterNames != null) {
         for(int i = 0; i < this.parameterNames.size(); ++i) {
            String name = (String)this.parameterNames.get(i);
            if (name != null) {
               v.visitParameterName(i, name);
            }
         }
      }

      if (this.debugNodes != null) {
         Iterator var4 = this.debugNodes.iterator();

         while(var4.hasNext()) {
            DexDebugNode.DexDebugOpNode n = (DexDebugNode.DexDebugOpNode)var4.next();
            n.accept(v);
         }
      }

      if (this.fineName != null) {
         v.visitSetFile(this.fineName);
      }

   }

   public void visitPrologue(DexLabel dexLabel) {
      this.addDebug(new DexDebugNode.DexDebugOpNode.Prologue(dexLabel));
   }

   public void visitEpiogue(DexLabel dexLabel) {
      this.addDebug(new DexDebugNode.DexDebugOpNode.Epiogue(dexLabel));
   }

   public abstract static class DexDebugOpNode {
      public DexLabel label;

      protected DexDebugOpNode(DexLabel label) {
         this.label = label;
      }

      public abstract void accept(DexDebugVisitor cv);

      public static class LineNumber extends DexDebugNode.DexDebugOpNode {
         public int line;

         public LineNumber(DexLabel label, int line) {
            super(label);
            this.line = line;
         }

         public void accept(DexDebugVisitor cv) {
            cv.visitLineNumber(this.line, this.label);
         }
      }

      public static class RestartLocal extends DexDebugNode.DexDebugOpNode {
         public int reg;

         public RestartLocal(DexLabel label, int reg) {
            super(label);
            this.reg = reg;
         }

         public void accept(DexDebugVisitor cv) {
            cv.visitRestartLocal(this.reg, this.label);
         }
      }

      public static class Prologue extends DexDebugNode.DexDebugOpNode {
         public Prologue(DexLabel label) {
            super(label);
         }

         public void accept(DexDebugVisitor cv) {
            cv.visitPrologue(this.label);
         }
      }

      public static class Epiogue extends DexDebugNode.DexDebugOpNode {
         public Epiogue(DexLabel label) {
            super(label);
         }

         public void accept(DexDebugVisitor cv) {
            cv.visitEpiogue(this.label);
         }
      }

      public static class EndLocal extends DexDebugNode.DexDebugOpNode {
         public int reg;

         public EndLocal(DexLabel label, int reg) {
            super(label);
            this.reg = reg;
         }

         public void accept(DexDebugVisitor cv) {
            cv.visitEndLocal(this.reg, this.label);
         }
      }

      public static class StartLocalNode extends DexDebugNode.DexDebugOpNode {
         public int reg;
         public String name;
         public String type;
         public String signature;

         public StartLocalNode(DexLabel label, int reg, String name, String type, String signature) {
            super(label);
            this.reg = reg;
            this.name = name;
            this.type = type;
            this.signature = signature;
         }

         public void accept(DexDebugVisitor cv) {
            cv.visitStartLocal(this.reg, this.label, this.name, this.type, this.signature);
         }
      }
   }
}
