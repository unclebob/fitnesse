// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain;

import fitnesse.ConfigurationParameter;
import fitnesse.ContextConfigurator;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.testutil.FitNesseUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import util.FileUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.URL;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

public class FitNesseMainTest {

  private static final String TEST_FITNESSE_ROOT = "testFitnesseRoot";

  private ContextConfigurator context;

  @Before
  public void setUp() throws Exception {
    context = ContextConfigurator.systemDefaults()
      .withRootPath(".")
      .withRootDirectoryName(TEST_FITNESSE_ROOT)
      .withPort(80);
  }

  @After
  public void tearDown() throws Exception {
    FileUtil.deleteFileSystemDirectory(TEST_FITNESSE_ROOT);
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
    verify(fitNesse, never()).start(any());
  }

  @Test
  public void commandArgCallsExecuteSingleCommand() throws Exception {
    context.withParameter(ConfigurationParameter.OMITTING_UPDATES, "true");
    context.withParameter(ConfigurationParameter.COMMAND, "command");

    FitNesse fitNesse = mock(FitNesse.class);

    context = spy(context);
    doAnswer(fitNesseContextWith(fitNesse)).when(context).makeFitNesseContext();

    int exitCode = new FitNesseMain().launchFitNesse(context);
    assertThat(exitCode, is(0));
    verify(fitNesse, never()).start(any());
    verify(fitNesse).executeSingleCommand("command", System.out);
    verify(fitNesse).stop();
  }

  @Test
  public void testDirCreations() throws Exception {
    runFitnesseMainWith("-o", "-c", "/root", "-r", TEST_FITNESSE_ROOT);

    assertTrue(new File(TEST_FITNESSE_ROOT).exists());
    assertTrue(new File(TEST_FITNESSE_ROOT, "files").exists());
  }

  @Test
  public void testIsRunning() throws Exception {
    FitNesseContext context = FitNesseUtil.makeTestContext();
    FitNesse fitnesse = context.fitNesse;

    assertFalse(fitnesse.isRunning());

    fitnesse.start(new ServerSocket(0));
    assertTrue(fitnesse.isRunning());

    fitnesse.stop();
    assertFalse(fitnesse.isRunning());
  }

  @Test
  public void canRunSingleCommand() throws Exception {
    String response = runFitnesseMainWith("-o", "-c", "/root");
    assertThat(response, containsString("Executing command:"));
    assertThat(response, not(containsString("Starting FitNesse on port:")));
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
      new FitNesseMain().launchFitNesse(arguments);
    } catch (Exception e) {
      assertEquals("error loading page: 404", e.getMessage());
      throw e;
    }
  }

  @Test
  public void canUseSingleCommandToCreateSymbolicLink() throws Exception {
    // create page to link to in test root
    FileUtil.createFile(TEST_FITNESSE_ROOT + "/LinkTarget.wiki", "Target for symbolic link unit test");

    String response = runFitnesseMainWith("-o", "-r", TEST_FITNESSE_ROOT, "-c", "?responder=symlink&linkName=MyLink&linkPath=LinkTarget");
    assertThat(response, containsString("Executing command:"));
    assertThat(response, not(containsString("Starting FitNesse on port:")));
  }

  @Test
  public void localhostOnlyFlagResultsInConnectableFitnesseOnLocalHost() throws Exception {
    String[] args = {"-p", "1999", "-lh"};
    new FitNesseMain().launchFitNesse(new Arguments(args));
    URL url = new URL("http://localhost:1999/?shutdown");
    HttpURLConnection con = (HttpURLConnection) url.openConnection();
    con.setRequestMethod("GET");
    int responseCode = con.getResponseCode();
    con.disconnect();
    assertEquals(200, responseCode);
  }

  @Test
  public void systemPropertiesTakePrecedenceOverConfiguredProperties() throws Exception {
    final String configFileName = "systemPropertiesTakePrecedenceOverConfiguredProperties.properties";
    String themeKey = ConfigurationParameter.THEME.getKey();
    FileUtil.createFile(configFileName, themeKey + "=example");

    System.setProperty(themeKey, "othertheme");
    try {
      // Checked via logging:
      String output = runFitnesseMainWith("-o", "-c", "/root", "-f", configFileName);
      assertThat(output, containsString("othertheme"));
    } finally {
      System.getProperties().remove(themeKey);
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
    return outputBytes.toString();
  }

  private Answer<FitNesseContext> fitNesseContextWith(final FitNesse fitNesse) {
    return invocation -> {
      FitNesseContext fitNesseContext = (FitNesseContext) invocation.callRealMethod();
      Field aField = fitNesseContext.getClass().getDeclaredField("fitNesse");
      aField.setAccessible(true);
      aField.set(fitNesseContext, fitNesse);
      return fitNesseContext;
    };
  }

}
