package com.googlecode.d2j.util.zip;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

public class ZipFile implements AutoCloseable, ZipConstants {
   static final int GPBF_ENCRYPTED_FLAG = 1;
   static final int GPBF_DATA_DESCRIPTOR_FLAG = 8;
   static final int GPBF_UTF8_FLAG = 2048;
   static final int GPBF_UNSUPPORTED_MASK = 1;
   private List<ZipEntry> entries;
   private String comment;
   final ByteBuffer raf;
   RandomAccessFile file;

   public ZipFile(ByteBuffer in) throws IOException {
      this.raf = in.asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN);
      this.readCentralDir();
   }

   public ZipFile(File fd) throws IOException {
      RandomAccessFile randomAccessFile = new RandomAccessFile(fd, "r");
      this.file = randomAccessFile;
      this.raf = randomAccessFile.getChannel().map(MapMode.READ_ONLY, 0L, fd.length());
      this.readCentralDir();
   }

   public ZipFile(byte[] data) throws IOException {
      this(ByteBuffer.wrap(data));
   }

   public List<? extends ZipEntry> entries() {
      return this.entries;
   }

   public String getComment() {
      return this.comment;
   }

   public ZipEntry findFirstEntry(String entryName) {
      if (entryName == null) {
         throw new NullPointerException("entryName == null");
      } else {
         ZipEntry ze = this.findFirstEntry0(entryName);
         if (ze == null) {
            ze = this.findFirstEntry0(entryName + "/");
         }

         return ze;
      }
   }

   private ZipEntry findFirstEntry0(String entryName) {
      Iterator var2 = this.entries.iterator();

      ZipEntry e;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         e = (ZipEntry)var2.next();
      } while(!e.getName().equals(entryName));

      return e;
   }

   public long getEntryDataStart(ZipEntry entry) {
      int fileNameLength = this.raf.getShort((int)(entry.localHeaderRelOffset + 26L)) & '\uffff';
      int extraFieldLength = this.raf.getShort((int)(entry.localHeaderRelOffset + 28L)) & '\uffff';
      return entry.localHeaderRelOffset + 30L + (long)fileNameLength + (long)extraFieldLength;
   }

   public InputStream getInputStream(ZipEntry entry) throws IOException {
      long entryDataStart = this.getEntryDataStart(entry);
      ByteBuffer is = (ByteBuffer)this.raf.duplicate().position((int)entryDataStart);
      ByteBuffer buf;
      if (entry.compressionMethod == 0) {
         buf = (ByteBuffer)is.slice().order(ByteOrder.LITTLE_ENDIAN).limit((int)entry.size);
         return new ZipFile.ByteBufferBackedInputStream(buf);
      } else {
         buf = (ByteBuffer)is.slice().order(ByteOrder.LITTLE_ENDIAN).limit((int)entry.compressedSize);
         int bufSize = Math.max(1024, (int)Math.min(entry.getSize(), 65535L));
         return new ZipFile.ZipInflaterInputStream(new ZipFile.ByteBufferBackedInputStream(buf), new Inflater(true), bufSize, entry);
      }
   }

   static void skip(ByteBuffer is, int i) {
      is.position(is.position() + i);
   }

   public int size() {
      return this.entries.size();
   }

   private void readCentralDir() throws IOException {
      ByteBuffer raf = this.raf;
      long scanOffset = (long)(raf.limit() - 22);
      if (scanOffset < 0L) {
         throw new ZipException("File too short to be a zip file: " + raf.limit());
      } else {
         long stopOffset = scanOffset - 65536L;
         if (stopOffset < 0L) {
            stopOffset = 0L;
         }

         do {
            raf.position((int)scanOffset);
            if ((long)raf.getInt() == 101010256L) {
               int diskNumber = raf.getShort() & '\uffff';
               int diskWithCentralDir = raf.getShort() & '\uffff';
               int numEntries = raf.getShort() & '\uffff';
               int totalNumEntries = raf.getShort() & '\uffff';
               skip(raf, 4);
               long centralDirOffset = (long)raf.getInt() & 4294967295L;
               int commentLength = raf.getShort() & '\uffff';
               if (numEntries == totalNumEntries && diskNumber == 0 && diskWithCentralDir == 0) {
                  boolean skipCommentsAndExtra = true;
                  if (commentLength > 0) {
                     if (commentLength > raf.remaining()) {
                        System.err.println("WARN: the zip comment exceed the zip content");
                     } else if (skipCommentsAndExtra) {
                        skip(raf, commentLength);
                     } else {
                        byte[] commentBytes = new byte[commentLength];
                        raf.get(commentBytes);
                        this.comment = new String(commentBytes, 0, commentBytes.length, StandardCharsets.UTF_8);
                     }
                  }

                  ByteBuffer buf = (ByteBuffer)raf.duplicate().order(ByteOrder.LITTLE_ENDIAN).position((int)centralDirOffset);
                  this.entries = new ArrayList(numEntries);

                  for(int i = 0; i < numEntries; ++i) {
                     ZipEntry newEntry = new ZipEntry(buf, skipCommentsAndExtra);
                     if (newEntry.localHeaderRelOffset < centralDirOffset) {
                        this.entries.add(newEntry);
                     }
                  }

                  return;
               } else {
                  throw new ZipException("Spanned archives not supported");
               }
            }

            --scanOffset;
         } while(scanOffset >= stopOffset);

         throw new ZipException("End Of Central Directory signature not found");
      }
   }

   static void throwZipException(String msg, int magic) throws ZipException {
      String hexString = String.format("0x%08x", magic);
      throw new ZipException(msg + " signature not found; was " + hexString);
   }

   public void close() throws IOException {
      if (this.file != null) {
         this.file.close();
      }

   }

   private static class ByteBufferBackedInputStream extends InputStream {
      private final ByteBuffer buf;

      public ByteBufferBackedInputStream(ByteBuffer buf) {
         this.buf = buf;
      }

      public int read() throws IOException {
         return !this.buf.hasRemaining() ? -1 : this.buf.get() & 255;
      }

      public int read(byte[] b, int off, int len) throws IOException {
         if (!this.buf.hasRemaining()) {
            return -1;
         } else {
            len = Math.min(len, this.buf.remaining());
            this.buf.get(b, off, len);
            return len;
         }
      }
   }

   static class ZipInflaterInputStream extends InflaterInputStream {
      private final ZipEntry entry;
      private long bytesRead = 0L;

      public ZipInflaterInputStream(InputStream is, Inflater inf, int bsize, ZipEntry entry) {
         super(is, inf, bsize);
         this.entry = entry;
      }

      public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
         int i;
         try {
            i = super.read(buffer, byteOffset, byteCount);
         } catch (IOException var6) {
            throw new IOException("Error reading data for " + this.entry.getName() + " near offset " + this.bytesRead, var6);
         }

         if (i == -1) {
            if (this.entry.size != this.bytesRead) {
               throw new IOException("Size mismatch on inflated file: " + this.bytesRead + " vs " + this.entry.size);
            }
         } else {
            this.bytesRead += (long)i;
         }

         return i;
      }

      public int available() throws IOException {
         return super.available() == 0 ? 0 : (int)(this.entry.getSize() - this.bytesRead);
      }
   }
}
