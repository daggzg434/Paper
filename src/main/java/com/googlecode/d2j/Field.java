package com.googlecode.d2j;

public class Field {
   private String name;
   private String owner;
   private String type;

   public Field(String owner, String name, String type) {
      this.owner = owner;
      this.type = type;
      this.name = name;
   }

   public String getName() {
      return this.name;
   }

   public String getOwner() {
      return this.owner;
   }

   public String getType() {
      return this.type;
   }

   public String toString() {
      return this.getOwner() + "." + this.getName() + " " + this.getType();
   }
}
