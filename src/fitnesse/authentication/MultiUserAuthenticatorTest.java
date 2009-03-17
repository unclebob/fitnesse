// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

public class MultiUserAuthenticatorTest extends TestCase {
  private File passwd;
  private PrintStream ps;
  private final String passwordFilename = "testpasswd";
  private MultiUserAuthenticator a;

  protected void setUp() throws Exception {
    passwd = new File(passwordFilename);
    ps = new PrintStream(new FileOutputStream(passwd));
    ps.println("uncle:bob");
    ps.println("micah:boy");
    ps.close();
    a = new MultiUserAuthenticator(passwordFilename);
  }

  protected void tearDown() throws Exception {
    passwd.delete();
  }

  public void testBuildAuthenticator() throws Exception {
    assertEquals(2, a.userCount());
    assertEquals("bob", a.getPasswd("uncle"));
    assertEquals("boy", a.getPasswd("micah"));
  }

  public void testAuthenticRequest() throws Exception {
    assertTrue(a.isAuthenticated("uncle", "bob"));
  }

  public void testInauthenticRequest() throws Exception {
    assertFalse(a.isAuthenticated("bill", "boob"));
  }
}
