package com.verify.service.Utils.axml.EditXml.decode;

import java.io.IOException;

public interface IAXMLSerialize {
   int getSize();

   int getType();

   void setSize(int size);

   void setType(int type);

   void read(IntReader reader) throws IOException;

   void write(IntWriter writer) throws IOException;
}
