// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain;

import java.io.File;

import junit.framework.TestCase;
import util.FileUtil;
import fitnesse.ComponentFactory;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.testutil.FitNesseUtil;

public class FitNesseMainTest extends TestCase {
  private FitNesseContext context;

  public void setUp() throws Exception {
    context = new FitNesseContext();
  }

  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("testFitnesseRoot");
  }

  public void testDirCreations() throws Exception {
    context.port = 80;
    context.rootPagePath = "testFitnesseRoot";
    new FitNesse(context);

    assertTrue(new File("testFitnesseRoot").exists());
    assertTrue(new File("testFitnesseRoot/files").exists());
  }

  public void testMakeNullAuthenticator() throws Exception {
    Authenticator a = FitNesseMain.makeAuthenticator(null, new ComponentFactory("blah"));
    assertTrue(a instanceof PromiscuousAuthenticator);
  }

  public void testMakeOneUserAuthenticator() throws Exception {
    Authenticator a = FitNesseMain.makeAuthenticator("bob:uncle", new ComponentFactory("blah"));
    assertTrue(a instanceof OneUserAuthenticator);
    OneUserAuthenticator oua = (OneUserAuthenticator) a;
    assertEquals("bob", oua.getUser());
    assertEquals("uncle", oua.getPassword());
  }

  public void testMakeMultiUserAuthenticator() throws Exception {
    final String passwordFilename = "testpasswd";
    File passwd = new File(passwordFilename);
    passwd.createNewFile();
    Authenticator a = FitNesseMain.makeAuthenticator(passwordFilename, new ComponentFactory("blah"));
    assertTrue(a instanceof MultiUserAuthenticator);
    passwd.delete();
  }

  public void testContextFitNesseGetSet() throws Exception {
    FitNesse fitnesse = new FitNesse(context, false);
    assertSame(fitnesse, context.fitnesse);
  }

  public void testIsRunning() throws Exception {
    context.port = FitNesseUtil.port;
    FitNesse fitnesse = new FitNesse(context, false);

    assertFalse(fitnesse.isRunning());

    fitnesse.start();
    assertTrue(fitnesse.isRunning());

    fitnesse.stop();
    assertFalse(fitnesse.isRunning());
  }

  public void testShouldInitializeFitNesseContext() {
    context.port = FitNesseUtil.port;
    new FitNesse(context, false);
    assertNotNull(FitNesseContext.globalContext);
  }
}
