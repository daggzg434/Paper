package com.verify.service.Utils.axml.EditXml.decode;

public interface IVisitor {
   void visit(BNSNode node);

   void visit(BTagNode node);

   void visit(BTXTNode node);
}
