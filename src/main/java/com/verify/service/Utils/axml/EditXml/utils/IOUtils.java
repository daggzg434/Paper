package com.verify.service.Utils.axml.EditXml.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class IOUtils {
   public static final int EOF = -1;
   public static final char DIR_SEPARATOR_UNIX = '/';
   public static final char DIR_SEPARATOR_WINDOWS = '\\';
   public static final char DIR_SEPARATOR;
   public static final String LINE_SEPARATOR_UNIX = "\n";
   public static final String LINE_SEPARATOR_WINDOWS = "\r\n";
   public static final String LINE_SEPARATOR;
   private static final int DEFAULT_BUFFER_SIZE = 4096;
   private static final int SKIP_BUFFER_SIZE = 2048;
   private static char[] SKIP_CHAR_BUFFER;
   private static byte[] SKIP_BYTE_BUFFER;

   public static void close(final URLConnection conn) {
      if (conn instanceof HttpURLConnection) {
         ((HttpURLConnection)conn).disconnect();
      }

   }

   public static void closeQuietly(final Reader input) {
      closeQuietly((Closeable)input);
   }

   public static void closeQuietly(final Writer output) {
      closeQuietly((Closeable)output);
   }

   public static void closeQuietly(final InputStream input) {
      closeQuietly((Closeable)input);
   }

   public static void closeQuietly(final OutputStream output) {
      closeQuietly((Closeable)output);
   }

   public static void closeQuietly(final Closeable closeable) {
      try {
         if (closeable != null) {
            closeable.close();
         }
      } catch (IOException var2) {
      }

   }

   public static void closeQuietly(final Closeable... closeables) {
      if (closeables != null) {
         Closeable[] var1 = closeables;
         int var2 = closeables.length;

         for(int var3 = 0; var3 < var2; ++var3) {
            Closeable closeable = var1[var3];
            closeQuietly(closeable);
         }

      }
   }

   public static void closeQuietly(final Socket sock) {
      if (sock != null) {
         try {
            sock.close();
         } catch (IOException var2) {
         }
      }

   }

   public static void closeQuietly(final Selector selector) {
      if (selector != null) {
         try {
            selector.close();
         } catch (IOException var2) {
         }
      }

   }

   public static void closeQuietly(final ServerSocket sock) {
      if (sock != null) {
         try {
            sock.close();
         } catch (IOException var2) {
         }
      }

   }

   public static BufferedReader toBufferedReader(final Reader reader) {
      return reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
   }

   public static BufferedReader toBufferedReader(final Reader reader, int size) {
      return reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader, size);
   }

   public static BufferedReader buffer(final Reader reader) {
      return reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
   }

   public static BufferedReader buffer(final Reader reader, int size) {
      return reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader, size);
   }

   public static BufferedWriter buffer(final Writer writer) {
      return writer instanceof BufferedWriter ? (BufferedWriter)writer : new BufferedWriter(writer);
   }

   public static BufferedWriter buffer(final Writer writer, int size) {
      return writer instanceof BufferedWriter ? (BufferedWriter)writer : new BufferedWriter(writer, size);
   }

   public static BufferedOutputStream buffer(final OutputStream outputStream) {
      if (outputStream == null) {
         throw new NullPointerException();
      } else {
         return outputStream instanceof BufferedOutputStream ? (BufferedOutputStream)outputStream : new BufferedOutputStream(outputStream);
      }
   }

   public static BufferedOutputStream buffer(final OutputStream outputStream, int size) {
      if (outputStream == null) {
         throw new NullPointerException();
      } else {
         return outputStream instanceof BufferedOutputStream ? (BufferedOutputStream)outputStream : new BufferedOutputStream(outputStream, size);
      }
   }

   public static BufferedInputStream buffer(final InputStream inputStream) {
      if (inputStream == null) {
         throw new NullPointerException();
      } else {
         return inputStream instanceof BufferedInputStream ? (BufferedInputStream)inputStream : new BufferedInputStream(inputStream);
      }
   }

   public static BufferedInputStream buffer(final InputStream inputStream, int size) {
      if (inputStream == null) {
         throw new NullPointerException();
      } else {
         return inputStream instanceof BufferedInputStream ? (BufferedInputStream)inputStream : new BufferedInputStream(inputStream, size);
      }
   }

   public static byte[] toByteArray(final InputStream input) throws IOException {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      copy((InputStream)input, (OutputStream)output);
      return output.toByteArray();
   }

   public static byte[] toByteArray(final InputStream input, final long size) throws IOException {
      if (size > 2147483647L) {
         throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size);
      } else {
         return toByteArray(input, (int)size);
      }
   }

   public static byte[] toByteArray(final InputStream input, final int size) throws IOException {
      if (size < 0) {
         throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);
      } else if (size == 0) {
         return new byte[0];
      } else {
         byte[] data = new byte[size];

         int offset;
         int readed;
         for(offset = 0; offset < size && (readed = input.read(data, offset, size - offset)) != -1; offset += readed) {
         }

         if (offset != size) {
            throw new IOException("Unexpected readed size. current: " + offset + ", excepted: " + size);
         } else {
            return data;
         }
      }
   }

   /** @deprecated */
   @Deprecated
   public static byte[] toByteArray(final Reader input) throws IOException {
      return toByteArray(input, Charset.defaultCharset());
   }

   public static byte[] toByteArray(final Reader input, final Charset encoding) throws IOException {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      copy((Reader)input, (OutputStream)output, (Charset)encoding);
      return output.toByteArray();
   }

   public static byte[] toByteArray(final Reader input, final String encoding) throws IOException {
      return toByteArray(input, Charsets.toCharset(encoding));
   }

   /** @deprecated */
   @Deprecated
   public static byte[] toByteArray(final String input) throws IOException {
      return input.getBytes(Charset.defaultCharset());
   }

   public static byte[] toByteArray(final URI uri) throws IOException {
      return toByteArray(uri.toURL());
   }

   public static byte[] toByteArray(final URL url) throws IOException {
      URLConnection conn = url.openConnection();

      byte[] var2;
      try {
         var2 = toByteArray(conn);
      } finally {
         close(conn);
      }

      return var2;
   }

   public static byte[] toByteArray(final URLConnection urlConn) throws IOException {
      InputStream inputStream = urlConn.getInputStream();

      byte[] var2;
      try {
         var2 = toByteArray(inputStream);
      } finally {
         inputStream.close();
      }

      return var2;
   }

   /** @deprecated */
   @Deprecated
   public static char[] toCharArray(final InputStream is) throws IOException {
      return toCharArray(is, Charset.defaultCharset());
   }

   public static char[] toCharArray(final InputStream is, final Charset encoding) throws IOException {
      CharArrayWriter output = new CharArrayWriter();
      copy((InputStream)is, (Writer)output, (Charset)encoding);
      return output.toCharArray();
   }

   public static char[] toCharArray(final InputStream is, final String encoding) throws IOException {
      return toCharArray(is, Charsets.toCharset(encoding));
   }

   public static char[] toCharArray(final Reader input) throws IOException {
      CharArrayWriter sw = new CharArrayWriter();
      copy((Reader)input, (Writer)sw);
      return sw.toCharArray();
   }

   /** @deprecated */
   @Deprecated
   public static List<String> readLines(final InputStream input) throws IOException {
      return readLines(input, Charset.defaultCharset());
   }

   public static List<String> readLines(final InputStream input, final Charset encoding) throws IOException {
      InputStreamReader reader = new InputStreamReader(input, Charsets.toCharset(encoding));
      return readLines((Reader)reader);
   }

   public static List<String> readLines(final InputStream input, final String encoding) throws IOException {
      return readLines(input, Charsets.toCharset(encoding));
   }

   public static List<String> readLines(final Reader input) throws IOException {
      BufferedReader reader = toBufferedReader(input);
      List<String> list = new ArrayList();

      for(String line = reader.readLine(); line != null; line = reader.readLine()) {
         list.add(line);
      }

      return list;
   }

   /** @deprecated */
   @Deprecated
   public static InputStream toInputStream(final CharSequence input) {
      return toInputStream(input, Charset.defaultCharset());
   }

   public static InputStream toInputStream(final CharSequence input, final Charset encoding) {
      return toInputStream(input.toString(), encoding);
   }

   public static InputStream toInputStream(final CharSequence input, final String encoding) throws IOException {
      return toInputStream(input, Charsets.toCharset(encoding));
   }

   /** @deprecated */
   @Deprecated
   public static InputStream toInputStream(final String input) {
      return toInputStream(input, Charset.defaultCharset());
   }

   public static InputStream toInputStream(final String input, final Charset encoding) {
      return new ByteArrayInputStream(input.getBytes(Charsets.toCharset(encoding)));
   }

   public static InputStream toInputStream(final String input, final String encoding) throws IOException {
      byte[] bytes = input.getBytes(Charsets.toCharset(encoding));
      return new ByteArrayInputStream(bytes);
   }

   public static void write(final byte[] data, final OutputStream output) throws IOException {
      if (data != null) {
         output.write(data);
      }

   }

   public static void writeChunked(final byte[] data, final OutputStream output) throws IOException {
      if (data != null) {
         int bytes = data.length;

         int chunk;
         for(int offset = 0; bytes > 0; offset += chunk) {
            chunk = Math.min(bytes, 4096);
            output.write(data, offset, chunk);
            bytes -= chunk;
         }
      }

   }

   /** @deprecated */
   @Deprecated
   public static void write(final byte[] data, final Writer output) throws IOException {
      write(data, output, Charset.defaultCharset());
   }

   public static void write(final byte[] data, final Writer output, final Charset encoding) throws IOException {
      if (data != null) {
         output.write(new String(data, Charsets.toCharset(encoding)));
      }

   }

   public static void write(final byte[] data, final Writer output, final String encoding) throws IOException {
      write(data, output, Charsets.toCharset(encoding));
   }

   public static void write(final char[] data, final Writer output) throws IOException {
      if (data != null) {
         output.write(data);
      }

   }

   public static void writeChunked(final char[] data, final Writer output) throws IOException {
      if (data != null) {
         int bytes = data.length;

         int chunk;
         for(int offset = 0; bytes > 0; offset += chunk) {
            chunk = Math.min(bytes, 4096);
            output.write(data, offset, chunk);
            bytes -= chunk;
         }
      }

   }

   /** @deprecated */
   @Deprecated
   public static void write(final char[] data, final OutputStream output) throws IOException {
      write(data, output, Charset.defaultCharset());
   }

   public static void write(final char[] data, final OutputStream output, final Charset encoding) throws IOException {
      if (data != null) {
         output.write((new String(data)).getBytes(Charsets.toCharset(encoding)));
      }

   }

   public static void write(final char[] data, final OutputStream output, final String encoding) throws IOException {
      write(data, output, Charsets.toCharset(encoding));
   }

   public static void write(final CharSequence data, final Writer output) throws IOException {
      if (data != null) {
         write(data.toString(), output);
      }

   }

   /** @deprecated */
   @Deprecated
   public static void write(final CharSequence data, final OutputStream output) throws IOException {
      write(data, output, Charset.defaultCharset());
   }

   public static void write(final CharSequence data, final OutputStream output, final Charset encoding) throws IOException {
      if (data != null) {
         write(data.toString(), output, encoding);
      }

   }

   public static void write(final CharSequence data, final OutputStream output, final String encoding) throws IOException {
      write(data, output, Charsets.toCharset(encoding));
   }

   public static void write(final String data, final Writer output) throws IOException {
      if (data != null) {
         output.write(data);
      }

   }

   /** @deprecated */
   @Deprecated
   public static void write(final String data, final OutputStream output) throws IOException {
      write(data, output, Charset.defaultCharset());
   }

   public static void write(final String data, final OutputStream output, final Charset encoding) throws IOException {
      if (data != null) {
         output.write(data.getBytes(Charsets.toCharset(encoding)));
      }

   }

   public static void write(final String data, final OutputStream output, final String encoding) throws IOException {
      write(data, output, Charsets.toCharset(encoding));
   }

   /** @deprecated */
   @Deprecated
   public static void write(final StringBuffer data, final Writer output) throws IOException {
      if (data != null) {
         output.write(data.toString());
      }

   }

   /** @deprecated */
   @Deprecated
   public static void write(final StringBuffer data, final OutputStream output) throws IOException {
      write(data, output, (String)null);
   }

   /** @deprecated */
   @Deprecated
   public static void write(final StringBuffer data, final OutputStream output, final String encoding) throws IOException {
      if (data != null) {
         output.write(data.toString().getBytes(Charsets.toCharset(encoding)));
      }

   }

   /** @deprecated */
   @Deprecated
   public static void writeLines(final Collection<?> lines, final String lineEnding, final OutputStream output) throws IOException {
      writeLines(lines, lineEnding, output, Charset.defaultCharset());
   }

   public static void writeLines(final Collection<?> lines, String lineEnding, final OutputStream output, final Charset encoding) throws IOException {
      if (lines != null) {
         if (lineEnding == null) {
            lineEnding = LINE_SEPARATOR;
         }

         Charset cs = Charsets.toCharset(encoding);

         for(Iterator var5 = lines.iterator(); var5.hasNext(); output.write(lineEnding.getBytes(cs))) {
            Object line = var5.next();
            if (line != null) {
               output.write(line.toString().getBytes(cs));
            }
         }

      }
   }

   public static void writeLines(final Collection<?> lines, final String lineEnding, final OutputStream output, final String encoding) throws IOException {
      writeLines(lines, lineEnding, output, Charsets.toCharset(encoding));
   }

   public static void writeLines(final Collection<?> lines, String lineEnding, final Writer writer) throws IOException {
      if (lines != null) {
         if (lineEnding == null) {
            lineEnding = LINE_SEPARATOR;
         }

         for(Iterator var3 = lines.iterator(); var3.hasNext(); writer.write(lineEnding)) {
            Object line = var3.next();
            if (line != null) {
               writer.write(line.toString());
            }
         }

      }
   }

   public static int copy(final InputStream input, final OutputStream output) throws IOException {
      long count = copyLarge(input, output);
      return count > 2147483647L ? -1 : (int)count;
   }

   public static long copy(final InputStream input, final OutputStream output, final int bufferSize) throws IOException {
      return copyLarge(input, output, new byte[bufferSize]);
   }

   public static long copyLarge(final InputStream input, final OutputStream output) throws IOException {
      return copy(input, output, 4096);
   }

   public static long copyLarge(final InputStream input, final OutputStream output, final byte[] buffer) throws IOException {
      long count;
      int n;
      for(count = 0L; -1 != (n = input.read(buffer)); count += (long)n) {
         output.write(buffer, 0, n);
      }

      return count;
   }

   public static long copyLarge(final InputStream input, final OutputStream output, final long inputOffset, final long length) throws IOException {
      return copyLarge(input, output, inputOffset, length, new byte[4096]);
   }

   public static long copyLarge(final InputStream input, final OutputStream output, final long inputOffset, final long length, final byte[] buffer) throws IOException {
      if (inputOffset > 0L) {
         skipFully(input, inputOffset);
      }

      if (length == 0L) {
         return 0L;
      } else {
         int bufferLength = buffer.length;
         int bytesToRead = bufferLength;
         if (length > 0L && length < (long)bufferLength) {
            bytesToRead = (int)length;
         }

         long totalRead = 0L;

         int read;
         while(bytesToRead > 0 && -1 != (read = input.read(buffer, 0, bytesToRead))) {
            output.write(buffer, 0, read);
            totalRead += (long)read;
            if (length > 0L) {
               bytesToRead = (int)Math.min(length - totalRead, (long)bufferLength);
            }
         }

         return totalRead;
      }
   }

   /** @deprecated */
   @Deprecated
   public static void copy(final InputStream input, final Writer output) throws IOException {
      copy(input, output, Charset.defaultCharset());
   }

   public static void copy(final InputStream input, final Writer output, final Charset inputEncoding) throws IOException {
      InputStreamReader in = new InputStreamReader(input, Charsets.toCharset(inputEncoding));
      copy((Reader)in, (Writer)output);
   }

   public static void copy(final InputStream input, final Writer output, final String inputEncoding) throws IOException {
      copy(input, output, Charsets.toCharset(inputEncoding));
   }

   public static int copy(final Reader input, final Writer output) throws IOException {
      long count = copyLarge(input, output);
      return count > 2147483647L ? -1 : (int)count;
   }

   public static long copyLarge(final Reader input, final Writer output) throws IOException {
      return copyLarge(input, output, new char[4096]);
   }

   public static long copyLarge(final Reader input, final Writer output, final char[] buffer) throws IOException {
      long count;
      int n;
      for(count = 0L; -1 != (n = input.read(buffer)); count += (long)n) {
         output.write(buffer, 0, n);
      }

      return count;
   }

   public static long copyLarge(final Reader input, final Writer output, final long inputOffset, final long length) throws IOException {
      return copyLarge(input, output, inputOffset, length, new char[4096]);
   }

   public static long copyLarge(final Reader input, final Writer output, final long inputOffset, final long length, final char[] buffer) throws IOException {
      if (inputOffset > 0L) {
         skipFully(input, inputOffset);
      }

      if (length == 0L) {
         return 0L;
      } else {
         int bytesToRead = buffer.length;
         if (length > 0L && length < (long)buffer.length) {
            bytesToRead = (int)length;
         }

         long totalRead = 0L;

         int read;
         while(bytesToRead > 0 && -1 != (read = input.read(buffer, 0, bytesToRead))) {
            output.write(buffer, 0, read);
            totalRead += (long)read;
            if (length > 0L) {
               bytesToRead = (int)Math.min(length - totalRead, (long)buffer.length);
            }
         }

         return totalRead;
      }
   }

   /** @deprecated */
   @Deprecated
   public static void copy(final Reader input, final OutputStream output) throws IOException {
      copy(input, output, Charset.defaultCharset());
   }

   public static void copy(final Reader input, final OutputStream output, final Charset outputEncoding) throws IOException {
      OutputStreamWriter out = new OutputStreamWriter(output, Charsets.toCharset(outputEncoding));
      copy((Reader)input, (Writer)out);
      out.flush();
   }

   public static void copy(final Reader input, final OutputStream output, final String outputEncoding) throws IOException {
      copy(input, output, Charsets.toCharset(outputEncoding));
   }

   public static boolean contentEquals(InputStream input1, InputStream input2) throws IOException {
      if (input1 == input2) {
         return true;
      } else {
         if (!(input1 instanceof BufferedInputStream)) {
            input1 = new BufferedInputStream((InputStream)input1);
         }

         if (!(input2 instanceof BufferedInputStream)) {
            input2 = new BufferedInputStream((InputStream)input2);
         }

         int ch2;
         for(int ch = ((InputStream)input1).read(); -1 != ch; ch = ((InputStream)input1).read()) {
            ch2 = ((InputStream)input2).read();
            if (ch != ch2) {
               return false;
            }
         }

         ch2 = ((InputStream)input2).read();
         return ch2 == -1;
      }
   }

   public static boolean contentEquals(Reader input1, Reader input2) throws IOException {
      if (input1 == input2) {
         return true;
      } else {
         Reader input1 = toBufferedReader(input1);
         Reader input2 = toBufferedReader(input2);

         int ch2;
         for(int ch = input1.read(); -1 != ch; ch = input1.read()) {
            ch2 = input2.read();
            if (ch != ch2) {
               return false;
            }
         }

         ch2 = input2.read();
         return ch2 == -1;
      }
   }

   public static boolean contentEqualsIgnoreEOL(final Reader input1, final Reader input2) throws IOException {
      if (input1 == input2) {
         return true;
      } else {
         BufferedReader br1 = toBufferedReader(input1);
         BufferedReader br2 = toBufferedReader(input2);
         String line1 = br1.readLine();

         String line2;
         for(line2 = br2.readLine(); line1 != null && line2 != null && line1.equals(line2); line2 = br2.readLine()) {
            line1 = br1.readLine();
         }

         return line1 == null ? line2 == null : line1.equals(line2);
      }
   }

   public static long skip(final InputStream input, final long toSkip) throws IOException {
      if (toSkip < 0L) {
         throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
      } else {
         if (SKIP_BYTE_BUFFER == null) {
            SKIP_BYTE_BUFFER = new byte[2048];
         }

         long remain;
         long n;
         for(remain = toSkip; remain > 0L; remain -= n) {
            n = (long)input.read(SKIP_BYTE_BUFFER, 0, (int)Math.min(remain, 2048L));
            if (n < 0L) {
               break;
            }
         }

         return toSkip - remain;
      }
   }

   public static long skip(final ReadableByteChannel input, final long toSkip) throws IOException {
      if (toSkip < 0L) {
         throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
      } else {
         ByteBuffer skipByteBuffer = ByteBuffer.allocate((int)Math.min(toSkip, 2048L));

         long remain;
         int n;
         for(remain = toSkip; remain > 0L; remain -= (long)n) {
            skipByteBuffer.position(0);
            skipByteBuffer.limit((int)Math.min(remain, 2048L));
            n = input.read(skipByteBuffer);
            if (n == -1) {
               break;
            }
         }

         return toSkip - remain;
      }
   }

   public static long skip(final Reader input, final long toSkip) throws IOException {
      if (toSkip < 0L) {
         throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
      } else {
         if (SKIP_CHAR_BUFFER == null) {
            SKIP_CHAR_BUFFER = new char[2048];
         }

         long remain;
         long n;
         for(remain = toSkip; remain > 0L; remain -= n) {
            n = (long)input.read(SKIP_CHAR_BUFFER, 0, (int)Math.min(remain, 2048L));
            if (n < 0L) {
               break;
            }
         }

         return toSkip - remain;
      }
   }

   public static void skipFully(final InputStream input, final long toSkip) throws IOException {
      if (toSkip < 0L) {
         throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
      } else {
         long skipped = skip(input, toSkip);
         if (skipped != toSkip) {
            throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
         }
      }
   }

   public static void skipFully(final ReadableByteChannel input, final long toSkip) throws IOException {
      if (toSkip < 0L) {
         throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
      } else {
         long skipped = skip(input, toSkip);
         if (skipped != toSkip) {
            throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
         }
      }
   }

   public static void skipFully(final Reader input, final long toSkip) throws IOException {
      long skipped = skip(input, toSkip);
      if (skipped != toSkip) {
         throw new EOFException("Chars to skip: " + toSkip + " actual: " + skipped);
      }
   }

   public static int read(final Reader input, final char[] buffer, final int offset, final int length) throws IOException {
      if (length < 0) {
         throw new IllegalArgumentException("Length must not be negative: " + length);
      } else {
         int remaining;
         int count;
         for(remaining = length; remaining > 0; remaining -= count) {
            int location = length - remaining;
            count = input.read(buffer, offset + location, remaining);
            if (-1 == count) {
               break;
            }
         }

         return length - remaining;
      }
   }

   public static int read(final Reader input, final char[] buffer) throws IOException {
      return read((Reader)input, (char[])buffer, 0, buffer.length);
   }

   public static int read(final InputStream input, final byte[] buffer, final int offset, final int length) throws IOException {
      if (length < 0) {
         throw new IllegalArgumentException("Length must not be negative: " + length);
      } else {
         int remaining;
         int count;
         for(remaining = length; remaining > 0; remaining -= count) {
            int location = length - remaining;
            count = input.read(buffer, offset + location, remaining);
            if (-1 == count) {
               break;
            }
         }

         return length - remaining;
      }
   }

   public static int read(final InputStream input, final byte[] buffer) throws IOException {
      return read((InputStream)input, (byte[])buffer, 0, buffer.length);
   }

   public static int read(final ReadableByteChannel input, final ByteBuffer buffer) throws IOException {
      int length = buffer.remaining();

      while(buffer.remaining() > 0) {
         int count = input.read(buffer);
         if (-1 == count) {
            break;
         }
      }

      return length - buffer.remaining();
   }

   public static void readFully(final Reader input, final char[] buffer, final int offset, final int length) throws IOException {
      int actual = read(input, buffer, offset, length);
      if (actual != length) {
         throw new EOFException("Length to read: " + length + " actual: " + actual);
      }
   }

   public static void readFully(final Reader input, final char[] buffer) throws IOException {
      readFully((Reader)input, (char[])buffer, 0, buffer.length);
   }

   public static void readFully(final InputStream input, final byte[] buffer, final int offset, final int length) throws IOException {
      int actual = read(input, buffer, offset, length);
      if (actual != length) {
         throw new EOFException("Length to read: " + length + " actual: " + actual);
      }
   }

   public static void readFully(final InputStream input, final byte[] buffer) throws IOException {
      readFully((InputStream)input, (byte[])buffer, 0, buffer.length);
   }

   public static byte[] readFully(final InputStream input, final int length) throws IOException {
      byte[] buffer = new byte[length];
      readFully((InputStream)input, (byte[])buffer, 0, buffer.length);
      return buffer;
   }

   public static void readFully(final ReadableByteChannel input, final ByteBuffer buffer) throws IOException {
      int expected = buffer.remaining();
      int actual = read(input, buffer);
      if (actual != expected) {
         throw new EOFException("Length to read: " + expected + " actual: " + actual);
      }
   }

   static {
      DIR_SEPARATOR = File.separatorChar;
      LINE_SEPARATOR = "\n";
   }
}
