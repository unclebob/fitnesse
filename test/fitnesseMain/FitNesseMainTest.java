// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Field;

import fitnesse.ConfigurationParameter;
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
    context = FitNesseUtil.makeTestContext(null, ".", "testFitnesseRoot", 80);
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("testFitnesseRoot");
  }

  @Test
  public void testInstallOnly() throws Exception {
    context.getProperties().setProperty(ConfigurationParameter.INSTALL_ONLY.getKey(), "true");
    FitNesse fitnesse = mockFitNesse();
    new FitNesseMain().launch(context);
    verify(fitnesse, never()).start();
  }

  @Test
  public void commandArgCallsExecuteSingleCommand() throws Exception {
    context.getProperties().setProperty(ConfigurationParameter.OMITTING_UPDATES.getKey(), "true");
    context.getProperties().setProperty(ConfigurationParameter.COMMAND.getKey(), "command");
    FitNesse fitnesse = mockFitNesse();
    when(fitnesse.start()).thenReturn(true);
    int exitCode = new FitNesseMain().launch(context);
    assertThat(exitCode, is(0));
    verify(fitnesse, never()).start();
    verify(fitnesse, times(1)).executeSingleCommand("command", System.out);
    verify(fitnesse, times(1)).stop();
  }

  @Test
  public void testDirCreations() throws IOException {
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
    String output = runFitnesseMainWith("-o", "-a", "user:pwd", "-c", "user:pwd:/FitNesse.ReadProtectedPage");
    assertThat(output, containsString("fitnesse.authentication.OneUserAuthenticator"));
  }

  @Test
  public void systemPropertiesTakePrecedenceOverConfiguredProperties() throws Exception {
    final String configFileName = "systemPropertiesTakePrecedenceOverConfiguredProperties.properties";
    FileUtil.createFile(configFileName, "Theme=example");

    System.setProperty("Theme", "othertheme");
    try {
      // Checked via logging:
      String output = runFitnesseMainWith("-o", "-c", "/root", "-f", configFileName);
      assertThat(output, containsString("othertheme"));
    } finally {
      System.getProperties().remove("Theme");
      FileUtil.deleteFile(configFileName);
    }
  }

  private String runFitnesseMainWith(String... args) throws Exception {
    PrintStream err = System.err;
    ByteArrayOutputStream outputBytes = new ByteArrayOutputStream();
    System.setErr(new PrintStream(outputBytes));
    Arguments arguments = new Arguments(args);
    Integer exitCode = new FitNesseMain().launchFitNesse(arguments);
    assertThat(exitCode, is(0));
    System.setErr(err);
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
