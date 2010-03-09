// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain;

import fitnesse.Arguments;
import fitnesse.ComponentFactory;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.testutil.FitNesseUtil;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;
import static org.junit.matchers.JUnitMatchers.*;
import util.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

public class FitNesseMainTest {

  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    context = new FitNesseContext();
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("testFitnesseRoot");
  }

  @Test
  public void testInstallOnly() throws Exception {
    Arguments args = new Arguments();
    args.setInstallOnly(true);
    FitNesse fitnesse = mock(FitNesse.class);
    FitNesseMain.updateAndLaunch(args, context, fitnesse);
    verify(fitnesse, never()).start();
    verify(fitnesse, times(1)).applyUpdates();
  }

  @Test
  public void commandArgCallsExecuteSingleCommand() throws Exception {
    FitNesseMain.dontExitAfterSingleCommand = true;
    Arguments args = new Arguments();
    args.setCommand("command");
    FitNesse fitnesse = mock(FitNesse.class);
    when(fitnesse.start()).thenReturn(true);
    FitNesseMain.updateAndLaunch(args, context, fitnesse);
    verify(fitnesse, times(1)).applyUpdates();
    verify(fitnesse, times(1)).start();
    verify(fitnesse, times(1)).executeSingleCommand("command", System.out);
    verify(fitnesse, times(1)).stop();
  }

  @Test
  public void testDirCreations() throws Exception {
    context.port = 80;
    context.rootPagePath = "testFitnesseRoot";
    new FitNesse(context);

    assertTrue(new File("testFitnesseRoot").exists());
    assertTrue(new File("testFitnesseRoot/files").exists());
  }

  @Test
  public void testMakeNullAuthenticator() throws Exception {
    Authenticator a = FitNesseMain.makeAuthenticator(null,
      new ComponentFactory("blah"));
    assertTrue(a instanceof PromiscuousAuthenticator);
  }

  @Test
  public void testMakeOneUserAuthenticator() throws Exception {
    Authenticator a = FitNesseMain.makeAuthenticator("bob:uncle",
      new ComponentFactory("blah"));
    assertTrue(a instanceof OneUserAuthenticator);
    OneUserAuthenticator oua = (OneUserAuthenticator) a;
    assertEquals("bob", oua.getUser());
    assertEquals("uncle", oua.getPassword());
  }

  @Test
  public void testMakeMultiUserAuthenticator() throws Exception {
    final String passwordFilename = "testpasswd";
    File passwd = new File(passwordFilename);
    passwd.createNewFile();
    Authenticator a = FitNesseMain.makeAuthenticator(passwordFilename,
      new ComponentFactory("blah"));
    assertTrue(a instanceof MultiUserAuthenticator);
    passwd.delete();
  }

  @Test
  public void testContextFitNesseGetSet() throws Exception {
    FitNesse fitnesse = new FitNesse(context, false);
    assertSame(fitnesse, context.fitnesse);
  }

  @Test
  public void testIsRunning() throws Exception {
    context.port = FitNesseUtil.port;
    FitNesse fitnesse = new FitNesse(context, false);

    assertFalse(fitnesse.isRunning());

    fitnesse.start();
    assertTrue(fitnesse.isRunning());

    fitnesse.stop();
    assertFalse(fitnesse.isRunning());
  }

  @Test
  public void testShouldInitializeFitNesseContext() {
    context.port = FitNesseUtil.port;
    new FitNesse(context, false);
    assertNotNull(FitNesseContext.globalContext);
  }

  @Test
  public void canRunSingleCommand() throws Exception {
    String response = runFitnesseMainWith("-o",  "-c", "/root");
    assertThat(response, containsString("Command Output"));
  }

  @Test
  public void canRunSingleCommandWithAuthentication() throws Exception {
    String response = runFitnesseMainWith("-o", "-a", "user:pwd", "-c", "user:pwd:/FitNesse.ReadProtectedPage");
    assertThat(response, containsString("HTTP/1.1 200 OK"));
  }

  private String runFitnesseMainWith(String... args) throws Exception {
    FitNesseMain.dontExitAfterSingleCommand = true;
    PrintStream out = System.out;
    ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputBytes));
    FitNesseMain.main(args);
    System.setOut(out);
    String response = outputBytes.toString();
    return response;
  }
}
