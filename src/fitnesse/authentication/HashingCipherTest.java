// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import java.util.Random;

import junit.framework.TestCase;

public class HashingCipherTest extends TestCase {
  private String[] inputs = new String[]{"123", "abc", "12345678901234567890", "this is a test", "!@#$%^&*()"};
  private HashingCipher crypter = new HashingCipher();

  public void setUp() throws Exception {
  }

  public void tearDown() throws Exception {
  }

  public void testHashReturnsDifferentValueThanPassed() throws Exception {
    String testString = "This is a test string";
    String hash = crypter.encrypt(testString);
    assertNotNull(hash);
    assertFalse(hash.equals(testString));
  }

  public void testDifferentStringHashDifferently() throws Exception {
    String hash1 = crypter.encrypt("123456");
    String hash2 = crypter.encrypt("abcdef");
    assertFalse(hash1.equals(hash2));
  }

  public void testLengthOfHash() throws Exception {
    for (int i = 0; i < inputs.length; i++) {
      String input = inputs[i];
      String encryption = crypter.encrypt(input);
      assertEquals(input, 20, encryption.length());
    }
  }

  public void testSameInputGivesSameOutput() throws Exception {
    for (int i = 0; i < inputs.length; i++) {
      String input = inputs[i];
      String encryption1 = crypter.encrypt(input);
      String encryption2 = crypter.encrypt(input);
      assertEquals(input, encryption1, encryption2);
    }
  }

  public void testAlgorithmSpeed() throws Exception {
    Random generator = new Random();
    int sampleSize = 1000;
    String[] inputs = new String[sampleSize];

    for (int i = 0; i < inputs.length; i++) {
      int passwordSize = generator.nextInt(20) + 1;
      byte[] passwd = new byte[passwordSize];
      generator.nextBytes(passwd);
      inputs[i] = new String(passwd);
    }

    long startTime = System.currentTimeMillis();
    for (int i = 0; i < inputs.length; i++) {
      crypter.encrypt(inputs[i]);
    }
    long duration = System.currentTimeMillis() - startTime;

    assertTrue(duration < 1000);
  }
}
