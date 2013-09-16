// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import fitnesse.Arguments;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
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
    FitNesse fitnesse = mock(FitNesse.class);
    FitNesseMain.update(args, fitnesse);
    FitNesseMain.launch(args, context, fitnesse);
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
    FitNesseMain.update(args, fitnesse);
    FitNesseMain.launch(args, context, fitnesse);
    verify(fitnesse, times(1)).applyUpdates();
    verify(fitnesse, times(1)).start();
    verify(fitnesse, times(1)).executeSingleCommand("command", System.out);
    verify(fitnesse, times(1)).stop();
  }

  @Test
  public void testDirCreations() throws Exception {
    new FitNesse(context);

    assertTrue(new File("testFitnesseRoot").exists());
    assertTrue(new File("testFitnesseRoot/files").exists());
  }

  @Test
  public void testIsRunning() throws Exception {
    context = FitNesseUtil.makeTestContext(null, null, null, FitNesseUtil.PORT);
    FitNesse fitnesse = new FitNesse(context, false);

    assertFalse(fitnesse.isRunning());

    fitnesse.start();
    assertTrue(fitnesse.isRunning());

    fitnesse.stop();
    assertFalse(fitnesse.isRunning());
  }

  @Test
  public void testShouldInitializeFitNesseContext() {
    context = FitNesseUtil.makeTestContext(null, null, null, FitNesseUtil.PORT);
    new FitNesse(context, false);
    assertNotNull(FitNesse.FITNESSE_INSTANCE.getContext());
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
