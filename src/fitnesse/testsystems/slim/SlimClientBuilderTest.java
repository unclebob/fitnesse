// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.components.ClassPathBuilder;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.testsystems.ClientBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import fitnesse.testsystems.TestSystem;
import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

import java.io.File;

import static org.junit.Assert.assertEquals;

public class SlimClientBuilderTest {

  private String getClassPath(WikiPage page) {
    return new ClassPathBuilder().getClasspath(page);
  }

  @Test
  public void testCommandPatternCSharp() throws Exception {
    String specifiedPageText = "!define COMMAND_PATTERN {%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    ClientBuilder clientBuilder = new SlimClientBuilder(specifiedPage.readOnlyData(), getClassPath(specifiedPage));
    assertEquals("%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p", clientBuilder.getCommandPattern());

    ClientBuilder clientBuilder1 = new SlimClientBuilder(specifiedPage.readOnlyData(), getClassPath(specifiedPage)).withRemoteDebug(true);
    assertEquals("%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p", clientBuilder1.getCommandPattern());
  }


  @Test
  public void testCommandPatternJava() throws Exception {

    String pageText = "!define TEST_SYSTEM {slim}\n";
    WikiPage page = makeTestPage(pageText);

    ClientBuilder clientBuilder = new SlimClientBuilder(page.readOnlyData(), getClassPath(page));
    String sep = System.getProperty("path.separator");
    assertEquals("java -cp fitnesse.jar" + sep + "%p %m", clientBuilder.getCommandPattern());

    ClientBuilder debugClientBuilder = new SlimClientBuilder(page.readOnlyData(), getClassPath(page)).withRemoteDebug(true);
    assertEquals(
            "java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -cp %p %m",
            debugClientBuilder.getCommandPattern());
  }

  @Test
  public void testCommandPatternJavaWithDefinedCommands() {
    String specifiedPageText = "!define COMMAND_PATTERN {java -specialParam -cp %p %m}\n"
            + "!define REMOTE_DEBUG_COMMAND {java -remoteDebug -cp %p %m}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    ClientBuilder clientBuilder = new SlimClientBuilder(specifiedPage.readOnlyData(), getClassPath(specifiedPage));
    assertEquals("java -specialParam -cp %p %m", clientBuilder.getCommandPattern());

    ClientBuilder debugClientBuilder = new SlimClientBuilder(specifiedPage.readOnlyData(), getClassPath(specifiedPage)).withRemoteDebug(true);
    assertEquals("java -remoteDebug -cp %p %m", debugClientBuilder.getCommandPattern());
  }

  @Test
  public void testCommandPatternWithVariable() throws Exception {
    String specifiedPageText = "!define COMMAND_PATTERN (${MY_RUNNER} %p %m)\n"
            + "!define MY_RUNNER {rubyslim}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    ClientBuilder clientBuilder = new SlimClientBuilder(specifiedPage.readOnlyData(), getClassPath(specifiedPage));
    assertEquals("rubyslim %p %m", clientBuilder.getCommandPattern());
  }

  @Test
  public void testTestRunnerWithVariable() throws Exception {
    String specifiedPageText = "!define TEST_RUNNER (${MY_RUNNER}.rb)\n"
            + "!define MY_RUNNER {rubyslim}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    ClientBuilder clientBuilder = new SlimClientBuilder(specifiedPage.readOnlyData(), getClassPath(specifiedPage));
    assertEquals("rubyslim.rb", clientBuilder.getTestRunner());
  }

  @Test
  public void testRunnerCSharp() throws Exception {
    String specifiedPageText = "!define TEST_RUNNER {..\\fitnesse\\fitsharp\\Runner.exe}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    ClientBuilder clientBuilder = new SlimClientBuilder(specifiedPage.readOnlyData(), getClassPath(specifiedPage));
    assertEquals("..\\fitnesse\\fitsharp\\Runner.exe", clientBuilder.getTestRunner());
    ClientBuilder debugClientBuilder = new SlimClientBuilder(specifiedPage.readOnlyData(), getClassPath(specifiedPage)).withRemoteDebug(true);
    assertEquals("..\\fitnesse\\fitsharp\\runnerw.exe", debugClientBuilder.getTestRunner());
  }

  @Test
  public void testRunnerDefault() throws Exception {
    String pageText = "!define TEST_SYSTEM {slim}\n";
    WikiPage page = makeTestPage(pageText);

    ClientBuilder clientBuilder = new SlimClientBuilder(page.readOnlyData(), getClassPath(page));
    assertEquals("fitnesse.slim.SlimService", clientBuilder.getTestRunner());
    ClientBuilder debugClientBuilder = new SlimClientBuilder(page.readOnlyData(), getClassPath(page)).withRemoteDebug(true);
    assertEquals("fitnesse.slim.SlimService", debugClientBuilder.getTestRunner());
  }

  @Test
  public void testCustomRunner() {
    String specifiedPageText = "!define REMOTE_DEBUG_RUNNER {Different runner}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    ClientBuilder clientBuilder = new SlimClientBuilder(specifiedPage.readOnlyData(), getClassPath(specifiedPage)).withRemoteDebug(true);
    assertEquals("Different runner", clientBuilder.getTestRunner());
  }

  WikiPage makeTestPage(String pageText) {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    return WikiPageUtil.addPage(root, PathParser.parse("TestPage"), pageText);
  }

  @Test
  public void testTestRunnerWithRootPathVariable() throws Exception {
    String fitnesseRootpath = "/home/fitnesse";
    FitNesseContext context = FitNesseUtil.makeTestContext(null, fitnesseRootpath, null, 80);
    new FitNesse(context, false);

    String specifiedPageText = "!define TEST_RUNNER (${FITNESSE_ROOTPATH}/rubyslim.rb)\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    ClientBuilder clientBuilder = new SlimClientBuilder(specifiedPage.readOnlyData(), getClassPath(specifiedPage));
    assertEquals(fitnesseRootpath + "/rubyslim.rb", clientBuilder.getTestRunner());
  }

}
