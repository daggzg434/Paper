package com.googlecode.dex2jar.ir.stmt;

import com.googlecode.dex2jar.ir.LabelAndLocalMapper;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;

public class StmtList implements Iterable<Stmt>, Comparator<Stmt> {
   private Stmt first;
   private Stmt last;
   private int index = 1;
   private int size = 0;

   public void add(Stmt stmt) {
      this.insertLast(stmt);
   }

   public void addAll(Collection<Stmt> list) {
      Iterator var2 = list.iterator();

      while(var2.hasNext()) {
         Stmt stmt = (Stmt)var2.next();
         this.insertLast(stmt);
      }

   }

   public StmtList clone(LabelAndLocalMapper mapper) {
      StmtList nList = new StmtList();
      Iterator var3 = this.iterator();

      while(var3.hasNext()) {
         Stmt stmt = (Stmt)var3.next();
         nList.add(stmt.clone(mapper));
      }

      return nList;
   }

   public int compare(Stmt o1, Stmt o2) {
      return o1.id - o2.id;
   }

   public boolean contains(Stmt stmt) {
      return stmt.list == this;
   }

   public Stmt getFirst() {
      return this.first;
   }

   public Stmt getLast() {
      return this.last;
   }

   public int getSize() {
      return this.size;
   }

   private void indexIt(Stmt stmt) {
      if (stmt.id <= 0) {
         stmt.id = this.index++;
      }

   }

   public void insertAfter(Stmt position, Stmt stmt) {
      if (position.list == this) {
         this.indexIt(stmt);
         stmt.list = this;
         ++this.size;
         stmt.next = position.next;
         stmt.pre = position;
         if (position.next == null) {
            this.last = stmt;
         } else {
            position.next.pre = stmt;
         }

         position.next = stmt;
      }

   }

   public void insertBefore(Stmt position, Stmt stmt) {
      if (position.list == this) {
         this.indexIt(stmt);
         stmt.list = this;
         ++this.size;
         stmt.pre = position.pre;
         stmt.next = position;
         if (position.pre == null) {
            this.first = stmt;
         } else {
            position.pre.next = stmt;
         }

         position.pre = stmt;
      }

   }

   public void insertFirst(Stmt stmt) {
      this.indexIt(stmt);
      stmt.list = this;
      ++this.size;
      if (this.first == null) {
         this.first = this.last = stmt;
         stmt.pre = stmt.next = null;
      } else {
         stmt.pre = null;
         stmt.next = this.first;
         this.first.pre = stmt;
         this.first = stmt;
      }

   }

   public void insertLast(Stmt stmt) {
      this.indexIt(stmt);
      stmt.list = this;
      ++this.size;
      if (this.first == null) {
         this.first = this.last = stmt;
         stmt.pre = stmt.next = null;
      } else {
         stmt.next = null;
         stmt.pre = this.last;
         this.last.next = stmt;
         this.last = stmt;
      }

   }

   public Iterator<Stmt> iterator() {
      return new StmtList.StmtListIterator(this, this.first);
   }

   public void remove(Stmt stmt) {
      if (stmt.list == this) {
         --this.size;
         stmt.list = null;
         if (stmt.pre == null) {
            this.first = stmt.next;
         } else {
            stmt.pre.next = stmt.next;
         }

         if (stmt.next == null) {
            this.last = stmt.pre;
         } else {
            stmt.next.pre = stmt.pre;
         }

         stmt.pre = null;
         stmt.next = null;
      }

   }

   public void replace(Stmt stmt, Stmt nas) {
      if (stmt.list == this) {
         this.indexIt(nas);
         nas.list = this;
         nas.next = stmt.next;
         nas.pre = stmt.pre;
         if (stmt.next != null) {
            stmt.next.pre = nas;
         } else {
            this.last = nas;
         }

         if (stmt.pre != null) {
            stmt.pre.next = nas;
         } else {
            this.first = nas;
         }

         stmt.next = null;
         stmt.pre = null;
         stmt.list = null;
      }

   }

   public String toString() {
      if (this.size == 0) {
         return "[Empty]";
      } else {
         StringBuilder sb = new StringBuilder();

         Stmt s;
         for(Iterator var2 = this.iterator(); var2.hasNext(); sb.append(s).append("\n")) {
            s = (Stmt)var2.next();
            if (s.st == Stmt.ST.LABEL) {
               sb.append("\n");
            }
         }

         return sb.toString();
      }
   }

   public void move(Stmt start, Stmt end, Stmt dist) {
      if (start.pre == null) {
         this.first = end.next;
      } else {
         start.pre.next = end.next;
      }

      if (end.next == null) {
         this.last = start.pre;
      } else {
         end.next.pre = start.pre;
      }

      if (dist.next == null) {
         this.last = end;
         end.next = null;
      } else {
         dist.next.pre = end;
         end.next = dist.next;
      }

      dist.next = start;
      start.pre = dist;
   }

   public void clear() {
      this.size = 0;
      this.first = null;
      this.last = null;
   }

   private static class StmtListIterator implements Iterator<Stmt> {
      private Stmt current;
      private Stmt next;
      private final StmtList list;

      public StmtListIterator(StmtList list, Stmt next) {
         this.list = list;
         this.next = next;
      }

      public boolean hasNext() {
         return this.next != null;
      }

      public Stmt next() {
         Stmt x = this.current = this.next;
         if (x != null) {
            this.next = x.next;
         } else {
            this.next = null;
         }

         return x;
      }

      public void remove() {
         if (this.current != null) {
            this.list.remove(this.current);
            this.current = null;
         }

      }
   }
}
