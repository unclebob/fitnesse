// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.util;

import java.io.UnsupportedEncodingException;

import util.FileUtil;

public class Base64 {
  private static final byte[] base64Alphabet =
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes();
  private static final byte pad = '=';
  private static final int[] base64Value;
  static {{
    base64Value = new int[0xff+1];
    for (int i = 0; i <= 0xff; i++)
      base64Value[i] = -1;
    for (int v = 0; v <= 0x3f; v++)
      base64Value[base64Alphabet[v]] = v;
    base64Value[pad] = 0;
  }}

  public static String decode(String base64) throws UnsupportedEncodingException {
    // The INPUT's charset is irrelevant because Base64 is US-ASCII which is compatible to any charset
    // The OUTPUT of decode() will most likely be UTF-8 as most browsers encode basic auth in UTF-8 now
    return new String(decode(base64.getBytes()), FileUtil.CHARENCODING);
  }

  public static byte[] decode(byte[] bytes) {
    int lengthOfDecoding = getLengthOfDecoding(bytes);
    byte[] decoding = new byte[lengthOfDecoding];
    int decodingIndex = 0;

    for (int index = 0; index < bytes.length; index += 4) {
      int v1 = getValueFor(bytes[index]);
      int v2 = getValueFor(bytes[index + 1]);
      int v3 = getValueFor(bytes[index + 2]);
      int v4 = getValueFor(bytes[index + 3]);

      int c1 = (v1 << 2 | v2 >> 4) & 0xff;
      int c2 = (v2 << 4 | v3 >> 2) & 0xff;
      int c3 = (v3 << 6 | v4) & 0xff;

      decoding[decodingIndex++] = (byte)c1;
      if (bytes[index + 2] != pad)
        decoding[decodingIndex++] = (byte)c2;
      if (bytes[index + 3] != pad)
        decoding[decodingIndex++] = (byte)c3;
    }
    return decoding;
  }

  public static String encode(String value) {
    return new String(encode(value.getBytes()));
  }

  public static byte[] encode(byte[] bytes) {
    int inputLength = bytes.length;

    int lengthOfEncoding = getLengthOfEncoding(bytes);
    byte[] encoding = new byte[lengthOfEncoding];
    int encodingIndex = 0;

    int index = 0;
    while (index < inputLength) {
      byte c1 = bytes[index];
      byte c2 = index + 1 >= inputLength ? 0 : bytes[index + 1];
      byte c3 = index + 2 >= inputLength ? 0 : bytes[index + 2];

      int v1 = ((c1 & 0xfc) >> 2) & 0x3f;
      int v2 = (c1 << 4 | (c2 & 0xf0) >> 4) & 0x3f;
      int v3 = (c2 << 2 | (c3 & 0xc0) >> 6) & 0x3f;
      int v4 = c3 & 0x3f;

      encoding[encodingIndex++] = base64Alphabet[v1];
      encoding[encodingIndex++] = base64Alphabet[v2];
      encoding[encodingIndex++] = (index + 1 >= inputLength) ? pad : base64Alphabet[v3];
      encoding[encodingIndex++] = (index + 2 >= inputLength) ? pad : base64Alphabet[v4];
      index += 3;
    }
    return encoding;
  }

  private static int getLengthOfDecoding(byte[] bytes) {
    if ((bytes.length & 3) != 0)
      throw new IllegalArgumentException("Truncated BASE64 data? length=" + bytes.length);

    int lengthOfOutput = (bytes.length >> 2) * 3;
    if (bytes.length > 0) {
      if (bytes[bytes.length - 1] == pad)
        lengthOfOutput--;
      if (bytes[bytes.length - 2] == pad)
        lengthOfOutput--;
    }

    return lengthOfOutput;
  }

  private static int getLengthOfEncoding(byte[] bytes) {
    return ((bytes.length + 2) / 3) << 2;
  }

  public static int getValueFor(byte b) {
    int value = base64Value[b & 0xff];
    if (value == -1)
      throw new IllegalArgumentException("Invalid BASE64 symbol: " + (char)(b & 0xff));
    return value;
  }

}
