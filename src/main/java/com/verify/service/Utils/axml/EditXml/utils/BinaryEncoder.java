package com.verify.service.Utils.axml.EditXml.utils;

public interface BinaryEncoder extends Encoder {
   byte[] encode(byte[] source) throws EncoderException;
}
