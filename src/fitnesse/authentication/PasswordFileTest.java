// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import java.io.File;

import util.FileUtil;
import util.RegexTestCase;

public class PasswordFileTest extends RegexTestCase {

  private PasswordFile passwords;
  private File passwordFile;
  private PasswordCipher cipher = new HashingCipher();
  private String passwordFilename = "testDir/password.txt";

  public void setUp() throws Exception {
    new File("testDir").mkdir();

    passwords = new PasswordFile(passwordFilename, cipher);
    passwordFile = new File(passwordFilename);
  }

  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("testDir");
  }

  public void testSavePasswordForFirstUser() throws Exception {
    passwords.savePassword("Aladdin", "open sesame");
    assertTrue(passwordFile.exists());
    String contents = FileUtil.getFileContent(passwordFile);
    assertSubString("Aladdin:" + cipher.encrypt("open sesame"), contents);
  }

  public void testChangePasswordForFirstUser() throws Exception {
    passwords.savePassword("Aladdin", "open sesame");
    passwords.savePassword("Aladdin", "open please");
    String contents = FileUtil.getFileContent(passwordFile);
    assertNotSubString("Aladdin:" + cipher.encrypt("open sesame"), contents);
    assertSubString("Aladdin:" + cipher.encrypt("open please"), contents);
  }

  public void testMultipleUsers() throws Exception {
    addTMNTUsers();
    String contents = FileUtil.getFileContent(passwordFile);
    assertSubString("Leonardo:" + cipher.encrypt("katana"), contents);
    assertSubString("Donatello:" + cipher.encrypt("bo"), contents);
    assertSubString("Michaelangelo:" + cipher.encrypt("nunchaku"), contents);
    assertSubString("Rafael:" + cipher.encrypt("sai"), contents);
  }

  public void testAddChangePasswordWithMultipleUsers() throws Exception {
    addTMNTUsers();
    passwords.savePassword("Donatello", "manrikigusari");
    String contents = FileUtil.getFileContent(passwordFile);
    assertSubString("Leonardo:" + cipher.encrypt("katana"), contents);
    assertSubString("Donatello:" + cipher.encrypt("manrikigusari"), contents);
    assertSubString("Michaelangelo:" + cipher.encrypt("nunchaku"), contents);
    assertSubString("Rafael:" + cipher.encrypt("sai"), contents);
  }

  private void addTMNTUsers() throws Exception {
    passwords.savePassword("Leonardo", "katana");
    passwords.savePassword("Donatello", "bo");
    passwords.savePassword("Michaelangelo", "nunchaku");
    passwords.savePassword("Rafael", "sai");
  }

  public void testWritesAndReadsCipherType1() throws Exception {
    passwords.savePassword("rocksteady", "horn");
    String contents = FileUtil.getFileContent(passwordFile);
    assertSubString("!fitnesse.authentication.HashingCipher", contents);

    passwords = new PasswordFile(passwordFilename);
    assertEquals(HashingCipher.class, passwords.getCipher().getClass());
  }

  public void testWritesAndReadsCipherType2() throws Exception {
    passwordFilename = "testDir/passwords2.txt";
    setUp();
    passwords = new PasswordFile(passwordFilename);
    passwords.savePassword("rocksteady", "horn");
    String contents = FileUtil.getFileContent(passwordFile);
    assertSubString("!fitnesse.authentication.TransparentCipher", contents);

    passwords = new PasswordFile(passwordFilename);
    assertEquals(TransparentCipher.class, passwords.getCipher().getClass());
  }
}
