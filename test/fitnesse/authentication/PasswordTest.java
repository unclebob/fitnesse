// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.File;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertThrows;


public class PasswordTest {
  private Password password;

  @Before
  public void setUp() throws Exception {
    password = new Password("testDir/password.txt");
  }

  @Test
  public void testSavingUserAgainWithDifferentPassword() throws Exception {
    File file = new File(Password.defaultFile);
    FileUtil.createFile(file, "");
    saveFileWithUser("admin", "admin");

    List<String> fileLines = FileUtil.getFileLines(file);
    String text = fileLines.get(1);
    assertTrue(text.contains("admin"));
    saveFileWithUser("admin", "differentPassword");
    fileLines = FileUtil.getFileLines(file);
    String newText = fileLines.get(1);
    assertNotEquals(text, newText);
  }

  @Test
  public void testSavingUser() throws Exception {
    Password password = new Password(Password.defaultFile);
    File file = new File(Password.defaultFile);
    FileUtil.createFile(file, "");
    List<String> beforeSaving = FileUtil.getFileLines(file);
    assertEquals(0, beforeSaving.size());
    password.savePassword("admin", "admin");
    List<String> afterSaving = FileUtil.getFileLines(file);
    assertEquals(2, FileUtil.getFileLines(file).size());
    assertEquals("!fitnesse.authentication.HashingCipher", afterSaving.get(0));
    assertEquals("admin:UqKBNj590CeI3kOLiZXL", afterSaving.get(1));
  }

  @Test
  public void testDeletingNonExistingUser() throws Exception {
    File file = new File(Password.defaultFile);
    FileUtil.createFile(file, "");
    saveFileWithUser("admin", "admin");
    Exception exception = assertThrows(Exception.class, () -> {
      Password passwordToDelete = new Password(Password.defaultFile);
      passwordToDelete.deletePassword("DoesNotExist");
    });
    assertEquals("User does not exist.", exception.getMessage());
  }

  @Test
  public void testDeletingUser() throws Exception {
    File file = new File(Password.defaultFile);
    FileUtil.createFile(file, "");
    saveFileWithUser("admin", "admin");
    saveFileWithUser("WillBeDeleted", "WillBeDeleted");
    assertEquals(3, FileUtil.getFileLines(file).size());

    Password passwordToDelete = new Password(Password.defaultFile);
    passwordToDelete.deletePassword("WillBeDeleted");
    assertEquals(2, FileUtil.getFileLines(file).size());
    List<String> afterSaving = FileUtil.getFileLines(file);
    assertEquals("!fitnesse.authentication.HashingCipher", afterSaving.get(0));
    assertEquals("admin:UqKBNj590CeI3kOLiZXL", afterSaving.get(1));
  }

  private static void saveFileWithUser(String username, String password) throws Exception {
    new Password().savePassword(username, password);
  }

  @Test
  public void testArgsJustUser() throws Exception {
    password = new Password();
    boolean valid = password.args(new String[]{"splinter"});
    assertTrue(valid);
    assertEquals("splinter", password.getUsername());
    assertEquals("passwords.txt", password.getFilename());
  }

  @Test
  public void testArgsWithFilename() throws Exception {
    boolean valid = password.args(new String[]{"-f", "somefile.txt", "shredder"});
    assertTrue(valid);
    assertEquals("shredder", password.getUsername());
    assertEquals("somefile.txt", password.getFilename());
  }

  @Test
  public void testbadArgs() throws Exception {
    boolean valid = password.args(new String[]{});
    assertFalse(valid);
    valid = password.args(new String[]{"-d", "filename", "beebop"});
    assertFalse(valid);
  }

  @Test
  public void testArgsWithNewCipher() throws Exception {
    boolean valid = password.args(new String[]{"-c", "fitnesse.authentication.TransparentCipher", "shredder"});
    assertTrue(valid);
    assertEquals(TransparentCipher.class, password.getCipher().getClass());
  }
}
