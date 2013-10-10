// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;

import fitnesse.Arguments;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.Updater;
import fitnesse.testutil.FitNesseUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

public class FitNesseMainTest {

  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext(null, null, "testFitnesseRoot", 80);
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("testFitnesseRoot");
  }

  @Test
  public void testInstallOnly() throws Exception {
    Arguments args = new Arguments();
    args.setInstallOnly(true);
    FitNesse fitnesse = mockFitNesse();
    new FitNesseMain().launch(args, context);
    verify(fitnesse, never()).start();
  }

  @Test
  public void commandArgCallsExecuteSingleCommand() throws Exception {
    Arguments args = new Arguments();
    args.setCommand("command");
    args.setOmitUpdates(true);
    FitNesse fitnesse = mockFitNesse();
    when(fitnesse.start()).thenReturn(true);
    int exitCode = new FitNesseMain().launch(args, context);
    assertThat(exitCode, is(0));
    verify(fitnesse, times(1)).start();
    verify(fitnesse, times(1)).executeSingleCommand("command", System.out);
    verify(fitnesse, times(1)).stop();
  }

  @Test
  public void testDirCreations() throws Exception {
    FitNesse fitnesse = context.fitNesse;
    fitnesse.start();

    try {
      assertTrue(new File("testFitnesseRoot").exists());
      assertTrue(new File("testFitnesseRoot/files").exists());
    } finally {
      fitnesse.stop();
    }
  }

  @Test
  public void testIsRunning() throws Exception {
    context = FitNesseUtil.makeTestContext(null, null, null, FitNesseUtil.PORT);
    FitNesse fitnesse = context.fitNesse.dontMakeDirs();

    assertFalse(fitnesse.isRunning());

    fitnesse.start();
    assertTrue(fitnesse.isRunning());

    fitnesse.stop();
    assertFalse(fitnesse.isRunning());
  }

  @Test
  public void canRunSingleCommand() throws Exception {
    String response = runFitnesseMainWith("-o",  "-c", "/root");
    assertThat(response, containsString("Command Output"));
  }

  @Test
  public void canRunSingleCommandWithAuthentication() throws Exception {
    String response = runFitnesseMainWith("-o", "-a", "user:pwd", "-c", "user:pwd:/FitNesse.ReadProtectedPage");
    assertThat(response, containsString("fitnesse.authentication.OneUserAuthenticator"));
  }

  private String runFitnesseMainWith(String... args) throws Exception {
    //FitNesseMain.dontExitAfterSingleCommand = true;
    PrintStream out = System.out;
    ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outputBytes));
    Arguments arguments = FitNesseMain.parseCommandLine(args);
    int exitCode = new FitNesseMain().launchFitNesse(arguments);
    assertThat(exitCode, is(0));
    System.setOut(out);
    String response = outputBytes.toString();
    return response;
  }

  private FitNesse mockFitNesse() throws NoSuchFieldException, IllegalAccessException {
    FitNesse fitNesse = mock(FitNesse.class);
    Field aField = context.getClass().getDeclaredField("fitNesse");
    aField.setAccessible(true);
    aField.set(context, fitNesse);
    return fitNesse;
  }

}
