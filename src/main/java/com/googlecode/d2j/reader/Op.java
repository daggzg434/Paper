package com.googlecode.d2j.reader;

public enum Op implements CFG {
   NOP(0, "nop", InstructionFormat.kFmt10x, InstructionIndexType.kIndexNone, 2, false),
   MOVE(1, "move", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   MOVE_FROM16(2, "move/from16", InstructionFormat.kFmt22x, InstructionIndexType.kIndexNone, 2, true),
   MOVE_16(3, "move/16", InstructionFormat.kFmt32x, InstructionIndexType.kIndexNone, 2, true),
   MOVE_WIDE(4, "move-wide", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   MOVE_WIDE_FROM16(5, "move-wide/from16", InstructionFormat.kFmt22x, InstructionIndexType.kIndexNone, 2, true),
   MOVE_WIDE_16(6, "move-wide/16", InstructionFormat.kFmt32x, InstructionIndexType.kIndexNone, 2, true),
   MOVE_OBJECT(7, "move-object", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   MOVE_OBJECT_FROM16(8, "move-object/from16", InstructionFormat.kFmt22x, InstructionIndexType.kIndexNone, 2, true),
   MOVE_OBJECT_16(9, "move-object/16", InstructionFormat.kFmt32x, InstructionIndexType.kIndexNone, 2, true),
   MOVE_RESULT(10, "move-result", InstructionFormat.kFmt11x, InstructionIndexType.kIndexNone, 2, true),
   MOVE_RESULT_WIDE(11, "move-result-wide", InstructionFormat.kFmt11x, InstructionIndexType.kIndexNone, 2, true),
   MOVE_RESULT_OBJECT(12, "move-result-object", InstructionFormat.kFmt11x, InstructionIndexType.kIndexNone, 2, true),
   MOVE_EXCEPTION(13, "move-exception", InstructionFormat.kFmt11x, InstructionIndexType.kIndexNone, 2, true),
   RETURN_VOID(14, "return-void", InstructionFormat.kFmt10x, InstructionIndexType.kIndexNone, 16, false),
   RETURN(15, "return", InstructionFormat.kFmt11x, InstructionIndexType.kIndexNone, 16, false),
   RETURN_WIDE(16, "return-wide", InstructionFormat.kFmt11x, InstructionIndexType.kIndexNone, 16, false),
   RETURN_OBJECT(17, "return-object", InstructionFormat.kFmt11x, InstructionIndexType.kIndexNone, 16, false),
   CONST_4(18, "const/4", InstructionFormat.kFmt11n, InstructionIndexType.kIndexNone, 2, true),
   CONST_16(19, "const/16", InstructionFormat.kFmt21s, InstructionIndexType.kIndexNone, 2, true),
   CONST(20, "const", InstructionFormat.kFmt31i, InstructionIndexType.kIndexNone, 2, true),
   CONST_HIGH16(21, "const/high16", InstructionFormat.kFmt21h, InstructionIndexType.kIndexNone, 2, true),
   CONST_WIDE_16(22, "const-wide/16", InstructionFormat.kFmt21s, InstructionIndexType.kIndexNone, 2, true),
   CONST_WIDE_32(23, "const-wide/32", InstructionFormat.kFmt31i, InstructionIndexType.kIndexNone, 2, true),
   CONST_WIDE(24, "const-wide", InstructionFormat.kFmt51l, InstructionIndexType.kIndexNone, 2, true),
   CONST_WIDE_HIGH16(25, "const-wide/high16", InstructionFormat.kFmt21h, InstructionIndexType.kIndexNone, 2, true),
   CONST_STRING(26, "const-string", InstructionFormat.kFmt21c, InstructionIndexType.kIndexStringRef, 10, true),
   CONST_STRING_JUMBO(27, "const-string/jumbo", InstructionFormat.kFmt31c, InstructionIndexType.kIndexStringRef, 10, true),
   CONST_CLASS(28, "const-class", InstructionFormat.kFmt21c, InstructionIndexType.kIndexTypeRef, 10, true),
   MONITOR_ENTER(29, "monitor-enter", InstructionFormat.kFmt11x, InstructionIndexType.kIndexNone, 10, false),
   MONITOR_EXIT(30, "monitor-exit", InstructionFormat.kFmt11x, InstructionIndexType.kIndexNone, 10, false),
   CHECK_CAST(31, "check-cast", InstructionFormat.kFmt21c, InstructionIndexType.kIndexTypeRef, 10, true),
   INSTANCE_OF(32, "instance-of", InstructionFormat.kFmt22c, InstructionIndexType.kIndexTypeRef, 10, true),
   ARRAY_LENGTH(33, "array-length", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 10, true),
   NEW_INSTANCE(34, "new-instance", InstructionFormat.kFmt21c, InstructionIndexType.kIndexTypeRef, 10, true),
   NEW_ARRAY(35, "new-array", InstructionFormat.kFmt22c, InstructionIndexType.kIndexTypeRef, 10, true),
   FILLED_NEW_ARRAY(36, "filled-new-array", InstructionFormat.kFmt35c, InstructionIndexType.kIndexTypeRef, 10, true),
   FILLED_NEW_ARRAY_RANGE(37, "filled-new-array/range", InstructionFormat.kFmt3rc, InstructionIndexType.kIndexTypeRef, 10, true),
   FILL_ARRAY_DATA(38, "fill-array-data", InstructionFormat.kFmt31t, InstructionIndexType.kIndexNone, 2, false),
   THROW(39, "throw", InstructionFormat.kFmt11x, InstructionIndexType.kIndexNone, 8, false),
   GOTO(40, "goto", InstructionFormat.kFmt10t, InstructionIndexType.kIndexNone, 1, false),
   GOTO_16(41, "goto/16", InstructionFormat.kFmt20t, InstructionIndexType.kIndexNone, 1, false),
   GOTO_32(42, "goto/32", InstructionFormat.kFmt30t, InstructionIndexType.kIndexNone, 1, false),
   PACKED_SWITCH(43, "packed-switch", InstructionFormat.kFmt31t, InstructionIndexType.kIndexNone, 6, false),
   SPARSE_SWITCH(44, "sparse-switch", InstructionFormat.kFmt31t, InstructionIndexType.kIndexNone, 6, false),
   CMPL_FLOAT(45, "cmpl-float", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, false),
   CMPG_FLOAT(46, "cmpg-float", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, false),
   CMPL_DOUBLE(47, "cmpl-double", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, false),
   CMPG_DOUBLE(48, "cmpg-double", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, false),
   CMP_LONG(49, "cmp-long", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, false),
   IF_EQ(50, "if-eq", InstructionFormat.kFmt22t, InstructionIndexType.kIndexNone, 3, false),
   IF_NE(51, "if-ne", InstructionFormat.kFmt22t, InstructionIndexType.kIndexNone, 3, false),
   IF_LT(52, "if-lt", InstructionFormat.kFmt22t, InstructionIndexType.kIndexNone, 3, false),
   IF_GE(53, "if-ge", InstructionFormat.kFmt22t, InstructionIndexType.kIndexNone, 3, false),
   IF_GT(54, "if-gt", InstructionFormat.kFmt22t, InstructionIndexType.kIndexNone, 3, false),
   IF_LE(55, "if-le", InstructionFormat.kFmt22t, InstructionIndexType.kIndexNone, 3, false),
   IF_EQZ(56, "if-eqz", InstructionFormat.kFmt21t, InstructionIndexType.kIndexNone, 3, false),
   IF_NEZ(57, "if-nez", InstructionFormat.kFmt21t, InstructionIndexType.kIndexNone, 3, false),
   IF_LTZ(58, "if-ltz", InstructionFormat.kFmt21t, InstructionIndexType.kIndexNone, 3, false),
   IF_GEZ(59, "if-gez", InstructionFormat.kFmt21t, InstructionIndexType.kIndexNone, 3, false),
   IF_GTZ(60, "if-gtz", InstructionFormat.kFmt21t, InstructionIndexType.kIndexNone, 3, false),
   IF_LEZ(61, "if-lez", InstructionFormat.kFmt21t, InstructionIndexType.kIndexNone, 3, false),
   AGET(68, "aget", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, true),
   AGET_WIDE(69, "aget-wide", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, true),
   AGET_OBJECT(70, "aget-object", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, true),
   AGET_BOOLEAN(71, "aget-boolean", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, true),
   AGET_BYTE(72, "aget-byte", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, true),
   AGET_CHAR(73, "aget-char", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, true),
   AGET_SHORT(74, "aget-short", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, true),
   APUT(75, "aput", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, false),
   APUT_WIDE(76, "aput-wide", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, false),
   APUT_OBJECT(77, "aput-object", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, false),
   APUT_BOOLEAN(78, "aput-boolean", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, false),
   APUT_BYTE(79, "aput-byte", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, false),
   APUT_CHAR(80, "aput-char", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, false),
   APUT_SHORT(81, "aput-short", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, false),
   IGET(82, "iget", InstructionFormat.kFmt22c, InstructionIndexType.kIndexFieldRef, 10, true),
   IGET_WIDE(83, "iget-wide", InstructionFormat.kFmt22c, InstructionIndexType.kIndexFieldRef, 10, true),
   IGET_OBJECT(84, "iget-object", InstructionFormat.kFmt22c, InstructionIndexType.kIndexFieldRef, 10, true),
   IGET_BOOLEAN(85, "iget-boolean", InstructionFormat.kFmt22c, InstructionIndexType.kIndexFieldRef, 10, true),
   IGET_BYTE(86, "iget-byte", InstructionFormat.kFmt22c, InstructionIndexType.kIndexFieldRef, 10, true),
   IGET_CHAR(87, "iget-char", InstructionFormat.kFmt22c, InstructionIndexType.kIndexFieldRef, 10, true),
   IGET_SHORT(88, "iget-short", InstructionFormat.kFmt22c, InstructionIndexType.kIndexFieldRef, 10, true),
   IPUT(89, "iput", InstructionFormat.kFmt22c, InstructionIndexType.kIndexFieldRef, 10, false),
   IPUT_WIDE(90, "iput-wide", InstructionFormat.kFmt22c, InstructionIndexType.kIndexFieldRef, 10, false),
   IPUT_OBJECT(91, "iput-object", InstructionFormat.kFmt22c, InstructionIndexType.kIndexFieldRef, 10, false),
   IPUT_BOOLEAN(92, "iput-boolean", InstructionFormat.kFmt22c, InstructionIndexType.kIndexFieldRef, 10, false),
   IPUT_BYTE(93, "iput-byte", InstructionFormat.kFmt22c, InstructionIndexType.kIndexFieldRef, 10, false),
   IPUT_CHAR(94, "iput-char", InstructionFormat.kFmt22c, InstructionIndexType.kIndexFieldRef, 10, false),
   IPUT_SHORT(95, "iput-short", InstructionFormat.kFmt22c, InstructionIndexType.kIndexFieldRef, 10, false),
   SGET(96, "sget", InstructionFormat.kFmt21c, InstructionIndexType.kIndexFieldRef, 10, true),
   SGET_WIDE(97, "sget-wide", InstructionFormat.kFmt21c, InstructionIndexType.kIndexFieldRef, 10, true),
   SGET_OBJECT(98, "sget-object", InstructionFormat.kFmt21c, InstructionIndexType.kIndexFieldRef, 10, true),
   SGET_BOOLEAN(99, "sget-boolean", InstructionFormat.kFmt21c, InstructionIndexType.kIndexFieldRef, 10, true),
   SGET_BYTE(100, "sget-byte", InstructionFormat.kFmt21c, InstructionIndexType.kIndexFieldRef, 10, true),
   SGET_CHAR(101, "sget-char", InstructionFormat.kFmt21c, InstructionIndexType.kIndexFieldRef, 10, true),
   SGET_SHORT(102, "sget-short", InstructionFormat.kFmt21c, InstructionIndexType.kIndexFieldRef, 10, true),
   SPUT(103, "sput", InstructionFormat.kFmt21c, InstructionIndexType.kIndexFieldRef, 10, false),
   SPUT_WIDE(104, "sput-wide", InstructionFormat.kFmt21c, InstructionIndexType.kIndexFieldRef, 10, false),
   SPUT_OBJECT(105, "sput-object", InstructionFormat.kFmt21c, InstructionIndexType.kIndexFieldRef, 10, false),
   SPUT_BOOLEAN(106, "sput-boolean", InstructionFormat.kFmt21c, InstructionIndexType.kIndexFieldRef, 10, false),
   SPUT_BYTE(107, "sput-byte", InstructionFormat.kFmt21c, InstructionIndexType.kIndexFieldRef, 10, false),
   SPUT_CHAR(108, "sput-char", InstructionFormat.kFmt21c, InstructionIndexType.kIndexFieldRef, 10, false),
   SPUT_SHORT(109, "sput-short", InstructionFormat.kFmt21c, InstructionIndexType.kIndexFieldRef, 10, false),
   INVOKE_VIRTUAL(110, "invoke-virtual", InstructionFormat.kFmt35c, InstructionIndexType.kIndexMethodRef, 42, true),
   INVOKE_SUPER(111, "invoke-super", InstructionFormat.kFmt35c, InstructionIndexType.kIndexMethodRef, 42, true),
   INVOKE_DIRECT(112, "invoke-direct", InstructionFormat.kFmt35c, InstructionIndexType.kIndexMethodRef, 42, true),
   INVOKE_STATIC(113, "invoke-static", InstructionFormat.kFmt35c, InstructionIndexType.kIndexMethodRef, 42, true),
   INVOKE_INTERFACE(114, "invoke-interface", InstructionFormat.kFmt35c, InstructionIndexType.kIndexMethodRef, 42, true),
   INVOKE_VIRTUAL_RANGE(116, "invoke-virtual/range", InstructionFormat.kFmt3rc, InstructionIndexType.kIndexMethodRef, 42, true),
   INVOKE_SUPER_RANGE(117, "invoke-super/range", InstructionFormat.kFmt3rc, InstructionIndexType.kIndexMethodRef, 42, true),
   INVOKE_DIRECT_RANGE(118, "invoke-direct/range", InstructionFormat.kFmt3rc, InstructionIndexType.kIndexMethodRef, 42, true),
   INVOKE_STATIC_RANGE(119, "invoke-static/range", InstructionFormat.kFmt3rc, InstructionIndexType.kIndexMethodRef, 42, true),
   INVOKE_INTERFACE_RANGE(120, "invoke-interface/range", InstructionFormat.kFmt3rc, InstructionIndexType.kIndexMethodRef, 42, true),
   NEG_INT(123, "neg-int", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   NOT_INT(124, "not-int", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   NEG_LONG(125, "neg-long", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   NOT_LONG(126, "not-long", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   NEG_FLOAT(127, "neg-float", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   NEG_DOUBLE(128, "neg-double", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   INT_TO_LONG(129, "int-to-long", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   INT_TO_FLOAT(130, "int-to-float", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   INT_TO_DOUBLE(131, "int-to-double", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   LONG_TO_INT(132, "long-to-int", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   LONG_TO_FLOAT(133, "long-to-float", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   LONG_TO_DOUBLE(134, "long-to-double", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   FLOAT_TO_INT(135, "float-to-int", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   FLOAT_TO_LONG(136, "float-to-long", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   FLOAT_TO_DOUBLE(137, "float-to-double", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   DOUBLE_TO_INT(138, "double-to-int", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   DOUBLE_TO_LONG(139, "double-to-long", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   DOUBLE_TO_FLOAT(140, "double-to-float", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   INT_TO_BYTE(141, "int-to-byte", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   INT_TO_CHAR(142, "int-to-char", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   INT_TO_SHORT(143, "int-to-short", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   ADD_INT(144, "add-int", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   SUB_INT(145, "sub-int", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   MUL_INT(146, "mul-int", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   DIV_INT(147, "div-int", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, true),
   REM_INT(148, "rem-int", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, true),
   AND_INT(149, "and-int", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   OR_INT(150, "or-int", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   XOR_INT(151, "xor-int", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   SHL_INT(152, "shl-int", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   SHR_INT(153, "shr-int", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   USHR_INT(154, "ushr-int", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   ADD_LONG(155, "add-long", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   SUB_LONG(156, "sub-long", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   MUL_LONG(157, "mul-long", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   DIV_LONG(158, "div-long", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, true),
   REM_LONG(159, "rem-long", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 10, true),
   AND_LONG(160, "and-long", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   OR_LONG(161, "or-long", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   XOR_LONG(162, "xor-long", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   SHL_LONG(163, "shl-long", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   SHR_LONG(164, "shr-long", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   USHR_LONG(165, "ushr-long", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   ADD_FLOAT(166, "add-float", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   SUB_FLOAT(167, "sub-float", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   MUL_FLOAT(168, "mul-float", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   DIV_FLOAT(169, "div-float", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   REM_FLOAT(170, "rem-float", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   ADD_DOUBLE(171, "add-double", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   SUB_DOUBLE(172, "sub-double", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   MUL_DOUBLE(173, "mul-double", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   DIV_DOUBLE(174, "div-double", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   REM_DOUBLE(175, "rem-double", InstructionFormat.kFmt23x, InstructionIndexType.kIndexNone, 2, true),
   ADD_INT_2ADDR(176, "add-int/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   SUB_INT_2ADDR(177, "sub-int/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   MUL_INT_2ADDR(178, "mul-int/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   DIV_INT_2ADDR(179, "div-int/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 10, true),
   REM_INT_2ADDR(180, "rem-int/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 10, true),
   AND_INT_2ADDR(181, "and-int/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   OR_INT_2ADDR(182, "or-int/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   XOR_INT_2ADDR(183, "xor-int/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   SHL_INT_2ADDR(184, "shl-int/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   SHR_INT_2ADDR(185, "shr-int/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   USHR_INT_2ADDR(186, "ushr-int/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   ADD_LONG_2ADDR(187, "add-long/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   SUB_LONG_2ADDR(188, "sub-long/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   MUL_LONG_2ADDR(189, "mul-long/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   DIV_LONG_2ADDR(190, "div-long/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 10, true),
   REM_LONG_2ADDR(191, "rem-long/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 10, true),
   AND_LONG_2ADDR(192, "and-long/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   OR_LONG_2ADDR(193, "or-long/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   XOR_LONG_2ADDR(194, "xor-long/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   SHL_LONG_2ADDR(195, "shl-long/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   SHR_LONG_2ADDR(196, "shr-long/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   USHR_LONG_2ADDR(197, "ushr-long/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   ADD_FLOAT_2ADDR(198, "add-float/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   SUB_FLOAT_2ADDR(199, "sub-float/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   MUL_FLOAT_2ADDR(200, "mul-float/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   DIV_FLOAT_2ADDR(201, "div-float/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   REM_FLOAT_2ADDR(202, "rem-float/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   ADD_DOUBLE_2ADDR(203, "add-double/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   SUB_DOUBLE_2ADDR(204, "sub-double/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   MUL_DOUBLE_2ADDR(205, "mul-double/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   DIV_DOUBLE_2ADDR(206, "div-double/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   REM_DOUBLE_2ADDR(207, "rem-double/2addr", InstructionFormat.kFmt12x, InstructionIndexType.kIndexNone, 2, true),
   ADD_INT_LIT16(208, "add-int/lit16", InstructionFormat.kFmt22s, InstructionIndexType.kIndexNone, 2, true),
   RSUB_INT(209, "rsub-int", InstructionFormat.kFmt22s, InstructionIndexType.kIndexNone, 2, true),
   MUL_INT_LIT16(210, "mul-int/lit16", InstructionFormat.kFmt22s, InstructionIndexType.kIndexNone, 2, true),
   DIV_INT_LIT16(211, "div-int/lit16", InstructionFormat.kFmt22s, InstructionIndexType.kIndexNone, 10, true),
   REM_INT_LIT16(212, "rem-int/lit16", InstructionFormat.kFmt22s, InstructionIndexType.kIndexNone, 10, true),
   AND_INT_LIT16(213, "and-int/lit16", InstructionFormat.kFmt22s, InstructionIndexType.kIndexNone, 2, true),
   OR_INT_LIT16(214, "or-int/lit16", InstructionFormat.kFmt22s, InstructionIndexType.kIndexNone, 2, true),
   XOR_INT_LIT16(215, "xor-int/lit16", InstructionFormat.kFmt22s, InstructionIndexType.kIndexNone, 2, true),
   ADD_INT_LIT8(216, "add-int/lit8", InstructionFormat.kFmt22b, InstructionIndexType.kIndexNone, 2, true),
   RSUB_INT_LIT8(217, "rsub-int/lit8", InstructionFormat.kFmt22b, InstructionIndexType.kIndexNone, 2, true),
   MUL_INT_LIT8(218, "mul-int/lit8", InstructionFormat.kFmt22b, InstructionIndexType.kIndexNone, 2, true),
   DIV_INT_LIT8(219, "div-int/lit8", InstructionFormat.kFmt22b, InstructionIndexType.kIndexNone, 10, true),
   REM_INT_LIT8(220, "rem-int/lit8", InstructionFormat.kFmt22b, InstructionIndexType.kIndexNone, 10, true),
   AND_INT_LIT8(221, "and-int/lit8", InstructionFormat.kFmt22b, InstructionIndexType.kIndexNone, 2, true),
   OR_INT_LIT8(222, "or-int/lit8", InstructionFormat.kFmt22b, InstructionIndexType.kIndexNone, 2, true),
   XOR_INT_LIT8(223, "xor-int/lit8", InstructionFormat.kFmt22b, InstructionIndexType.kIndexNone, 2, true),
   SHL_INT_LIT8(224, "shl-int/lit8", InstructionFormat.kFmt22b, InstructionIndexType.kIndexNone, 2, true),
   SHR_INT_LIT8(225, "shr-int/lit8", InstructionFormat.kFmt22b, InstructionIndexType.kIndexNone, 2, true),
   USHR_INT_LIT8(226, "ushr-int/lit8", InstructionFormat.kFmt22b, InstructionIndexType.kIndexNone, 2, true),
   INVOKE_POLYMORPHIC(250, "invoke-polymorphic", InstructionFormat.kFmt45cc, InstructionIndexType.kIndexMethodAndProtoRef, 42, true),
   INVOKE_POLYMORPHIC_RANGE(251, "invoke-polymorphic/range", InstructionFormat.kFmt4rcc, InstructionIndexType.kIndexMethodAndProtoRef, 42, true),
   INVOKE_CUSTOM(252, "invoke-custom", InstructionFormat.kFmt35c, InstructionIndexType.kIndexCallSiteRef, 42, true),
   INVOKE_CUSTOM_RANGE(253, "invoke-custom/range", InstructionFormat.kFmt3rc, InstructionIndexType.kIndexCallSiteRef, 42, true),
   BAD_OP(-1, "bad-opcode", (InstructionFormat)null, InstructionIndexType.kIndexNone, 0, false);

   public int opcode;
   public InstructionFormat format;
   InstructionIndexType indexType;
   int flags;
   public String displayName;
   public static final Op[] ops = new Op[256];
   public boolean changeFrame;

   public boolean canBranch() {
      return 0 != (this.flags & 1);
   }

   public boolean canContinue() {
      return 0 != (this.flags & 2);
   }

   public boolean canReturn() {
      return 0 != (this.flags & 16);
   }

   public boolean canSwitch() {
      return 0 != (this.flags & 4);
   }

   public boolean canThrow() {
      return 0 != (this.flags & 8);
   }

   private Op(int op, String displayName, InstructionFormat fmt, InstructionIndexType indexType, int flags, boolean changeFrame) {
      this.opcode = op;
      this.displayName = displayName;
      this.format = fmt;
      this.indexType = indexType;
      this.flags = flags;
   }

   public String toString() {
      return this.displayName;
   }

   static {
      Op[] ops = Op.ops;
      Op[] var1 = values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         Op op = var1[var3];
         if (op.opcode >= 0) {
            ops[op.opcode] = op;
         }
      }

   }
}
