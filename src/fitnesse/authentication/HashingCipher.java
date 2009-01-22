// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import fitnesse.components.Base64;

public class HashingCipher implements PasswordCipher {
  private static final int repetitions = 3;

  private static final String theMagicLock = // A Peom by Yeats.
    "Like a long-leggedfly upon the stream\n" +
      "His mind moves upon silence.";

  public String encrypt(String value) {
    byte[] crypted = repeatEncryption(theMagicLock.getBytes(), value.getBytes());
    byte[] squeezed = fillToSize(crypted, 15);
    byte[] encoded = Base64.encode(squeezed);

    return new String(encoded);
  }

  private byte[] repeatEncryption(byte[] lock, byte[] key) {
    for (int i = 0; i < repetitions; i++)
      lock = encrypt(lock, key);

    return lock;
  }

  private byte[] encrypt(byte[] lock, byte[] key) {
    int keyIndex = 0;
    for (int i = 0; i < lock.length; i++) {
      byte lockByte = lock[i];
      byte keyByte = key[keyIndex++];
      lock[i] = (byte) (lockByte + keyByte);
      if (keyIndex == key.length)
        keyIndex = 0;
    }

    return lock;
  }

  public byte[] fillToSize(byte[] input, int size) {
    byte[] bytes = new byte[size];
    int inputLength = input.length;
    int inputIndex = 0;
    int outputIndex = 0;
    if (inputLength <= size) {
      while (outputIndex < size) {
        bytes[outputIndex++] = input[inputIndex++];
        if (inputIndex == inputLength)
          inputIndex = 0;
      }
    } else {
      while (inputIndex < inputLength) {
        byte currentByte = bytes[outputIndex];
        bytes[outputIndex++] = (byte) (currentByte + input[inputIndex++]);
        if (outputIndex == size)
          outputIndex = 0;
      }
    }

    return bytes;
  }
}
