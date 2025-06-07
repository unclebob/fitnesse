// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

public class PasswordFileTest {

  private PasswordFile passwords;
  private File passwordFile;
  private PasswordCipher cipher = new HashingCipher();
  private String passwordFilename = "testDir/password.txt";

  @Before
  public void setUp() throws Exception {
    new File("testDir").mkdir();

    passwords = new PasswordFile(passwordFilename, cipher);
    passwordFile = new File(passwordFilename);
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("testDir");
  }

  @Test
  public void testSavePasswordForFirstUser() throws Exception {
    passwords.savePassword("Aladdin", "open sesame");
    assertTrue(passwordFile.exists());
    String contents = FileUtil.getFileContent(passwordFile);
    assertSubString("Aladdin:" + cipher.encrypt("open sesame"), contents);
  }

  @Test
  public void testDeleteUser() throws Exception {
    passwords.savePassword("WillBeDeleted", "WillBeDeleted");
    int beforeDeleting = passwords.getPasswordMap().size();
    passwords.deleteUser("WillBeDeleted");
    int afterDeleting = passwords.getPasswordMap().size();
    assertEquals(1, beforeDeleting - afterDeleting);
  }

  @Test
  public void testChangePasswordForFirstUser() throws Exception {
    passwords.savePassword("Aladdin", "open sesame");
    passwords.savePassword("Aladdin", "open please");
    String contents = FileUtil.getFileContent(passwordFile);
    assertNotSubString("Aladdin:" + cipher.encrypt("open sesame"), contents);
    assertSubString("Aladdin:" + cipher.encrypt("open please"), contents);
  }

  @Test
  public void testMultipleUsers() throws Exception {
    addTMNTUsers();
    String contents = FileUtil.getFileContent(passwordFile);
    assertSubString("Leonardo:" + cipher.encrypt("katana"), contents);
    assertSubString("Donatello:" + cipher.encrypt("bo"), contents);
    assertSubString("Michaelangelo:" + cipher.encrypt("nunchaku"), contents);
    assertSubString("Rafael:" + cipher.encrypt("sai"), contents);
  }

  @Test
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

  @Test
  public void testWritesAndReadsCipherType1() throws Exception {
    passwords.savePassword("rocksteady", "horn");
    String contents = FileUtil.getFileContent(passwordFile);
    assertSubString("!fitnesse.authentication.HashingCipher", contents);

    passwords = new PasswordFile(passwordFilename);
    assertEquals(HashingCipher.class, passwords.getCipher().getClass());
  }

  @Test
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
