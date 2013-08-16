// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class PasswordTest {
  private Password password;

  @Before
  public void setUp() throws Exception {
    password = new Password("testDir/password.txt");
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
