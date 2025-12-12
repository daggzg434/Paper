package com.googlecode.d2j.reader;

public interface CFG {
   int kInstrCanBranch = 1;
   int kInstrCanContinue = 2;
   int kInstrCanSwitch = 4;
   int kInstrCanThrow = 8;
   int kInstrCanReturn = 16;
   int kInstrInvoke = 32;
}
