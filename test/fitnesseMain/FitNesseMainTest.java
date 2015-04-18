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
import fitnesse.ContextConfigurator;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.plugins.PluginException;
import fitnesse.testutil.FitNesseUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import util.FileUtil;

public class FitNesseMainTest {

  private ContextConfigurator context;

  @Before
  public void setUp() throws Exception {
    context = ContextConfigurator.systemDefaults()
            .withRootPath(".")
            .withRootDirectoryName("testFitnesseRoot")
            .withPort(80);
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory("testFitnesseRoot");
  }

  @Test
  public void testInstallOnly() throws Exception {
    final FitNesse fitNesse = mock(FitNesse.class);
    context.withParameter(ConfigurationParameter.INSTALL_ONLY, "true");
    // Avoid doing a real update...
    context.withParameter(ConfigurationParameter.OMITTING_UPDATES, "true");

    context = spy(context);
    doAnswer(fitNesseContextWith(fitNesse)).when(context).makeFitNesseContext();
    new FitNesseMain().launchFitNesse(context);
    verify(fitNesse, never()).start();
  }

  @Test
  public void commandArgCallsExecuteSingleCommand() throws Exception {
    context.withParameter(ConfigurationParameter.OMITTING_UPDATES, "true");
    context.withParameter(ConfigurationParameter.COMMAND, "command");

    FitNesse fitNesse = mock(FitNesse.class);
    when(fitNesse.start()).thenReturn(true);

    context = spy(context);
    doAnswer(fitNesseContextWith(fitNesse)).when(context).makeFitNesseContext();

    int exitCode = new FitNesseMain().launchFitNesse(context);
    assertThat(exitCode, is(0));
    verify(fitNesse, never()).start();
    verify(fitNesse, times(1)).executeSingleCommand("command", System.out);
    verify(fitNesse, times(1)).stop();
  }

  @Test
  public void testDirCreations() throws IOException, PluginException {
    FitNesse fitnesse = context.makeFitNesseContext().fitNesse;
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
    FitNesseContext context = FitNesseUtil.makeTestContext();
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

  @Test(expected = Exception.class)
  public void runningCommandWithNonExistentAddressResultsInError() throws Exception {
    String[] args = {"-o", "-a", "user:pwd", "-c", "user:pwd:/FitNesse.NonExistentTestCase?test"};
    Arguments arguments = new Arguments(args);
    try {
        Integer exitCode = new FitNesseMain().launchFitNesse(arguments);
    } catch (Exception e){
        assertEquals("error loading page: 404", e.getMessage());
        throw e;
    }
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

  private Answer<FitNesseContext> fitNesseContextWith(final FitNesse fitNesse) {
    return new Answer<FitNesseContext>() {
      @Override
      public FitNesseContext answer(InvocationOnMock invocation) throws Throwable {
        FitNesseContext fitNesseContext = (FitNesseContext) invocation.callRealMethod();
        Field aField = fitNesseContext.getClass().getDeclaredField("fitNesse");
        aField.setAccessible(true);
        aField.set(fitNesseContext, fitNesse);
        return fitNesseContext;
      }
    };
  }

}
