package com.googlecode.d2j;

public class DexException extends RuntimeException {
   private static final long serialVersionUID = 1L;

   public DexException() {
   }

   public DexException(String message) {
      super(message);
   }

   public DexException(Throwable cause) {
      super(cause);
   }

   public DexException(String message, Throwable cause) {
      super(message, cause);
   }

   public DexException(Throwable cause, String messageFormat, Object... args) {
      this(String.format(messageFormat, args), cause);
   }

   public DexException(String messageFormat, Object... args) {
      this(String.format(messageFormat, args));
   }
}
