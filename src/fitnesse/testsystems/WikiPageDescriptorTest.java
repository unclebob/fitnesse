// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.components.ClassPathBuilder;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.WikiPageUtil;
import org.junit.Test;

import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

import static org.junit.Assert.assertEquals;

public class WikiPageDescriptorTest {

  private String getClassPath(WikiPage page) {
    return new ClassPathBuilder().getClasspath(page);
  }

  @Test
  public void testCommandPatternCSharp() throws Exception {
    String specifiedPageText = "!define COMMAND_PATTERN {%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), false, getClassPath(specifiedPage));
    assertEquals("%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p", descriptor.getCommandPattern());

    Descriptor descriptor1 = new WikiPageDescriptor(specifiedPage.readOnlyData(), true, getClassPath(specifiedPage));
    assertEquals("%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p", descriptor1.getCommandPattern());
  }


  @Test
  public void testCommandPatternJava() throws Exception {

    String pageText = "!define TEST_SYSTEM {slim}\n";
    WikiPage page = makeTestPage(pageText);

    Descriptor descriptor = new WikiPageDescriptor(page.readOnlyData(), false, getClassPath(page));
    String sep = System.getProperty("path.separator");
    assertEquals("java -cp fitnesse.jar" + sep + "%p %m", descriptor.getCommandPattern());

    Descriptor debugDescriptor = new WikiPageDescriptor(page.readOnlyData(), true, getClassPath(page));
    assertEquals(
            "java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -cp %p %m",
            debugDescriptor.getCommandPattern());
  }

  @Test
  public void testCommandPatternJavaWithDefinedCommands() {
    String specifiedPageText = "!define COMMAND_PATTERN {java -specialParam -cp %p %m}\n"
            + "!define REMOTE_DEBUG_COMMAND {java -remoteDebug -cp %p %m}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), false, getClassPath(specifiedPage));
    assertEquals("java -specialParam -cp %p %m", descriptor.getCommandPattern());

    Descriptor debugDescriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), true, getClassPath(specifiedPage));
    assertEquals("java -remoteDebug -cp %p %m", debugDescriptor.getCommandPattern());
  }

  @Test
  public void testCommandPatternWithVariable() throws Exception {
    String specifiedPageText = "!define COMMAND_PATTERN (${MY_RUNNER} %p %m)\n"
            + "!define MY_RUNNER {rubyslim}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), false, getClassPath(specifiedPage));
    assertEquals("rubyslim %p %m", descriptor.getCommandPattern());
  }

  @Test
  public void testTestRunnerWithVariable() throws Exception {
    String specifiedPageText = "!define TEST_RUNNER (${MY_RUNNER}.rb)\n"
            + "!define MY_RUNNER {rubyslim}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), false, getClassPath(specifiedPage));
    assertEquals("rubyslim.rb", descriptor.getTestRunner());
  }

  @Test
  public void testRunnerCSharp() throws Exception {
    String specifiedPageText = "!define TEST_RUNNER {..\\fitnesse\\fitsharp\\Runner.exe}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), false, getClassPath(specifiedPage));
    assertEquals("..\\fitnesse\\fitsharp\\Runner.exe", descriptor.getTestRunner());
    Descriptor debugDescriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), true, getClassPath(specifiedPage));
    assertEquals("..\\fitnesse\\fitsharp\\runnerw.exe", debugDescriptor.getTestRunner());
  }

  @Test
  public void testRunnerDefault() throws Exception {
    String pageText = "!define TEST_SYSTEM {slim}\n";
    WikiPage page = makeTestPage(pageText);

    Descriptor descriptor = new WikiPageDescriptor(page.readOnlyData(), false, getClassPath(page));
    assertEquals("fitnesse.slim.SlimService", descriptor.getTestRunner());
    Descriptor debugDescriptor = new WikiPageDescriptor(page.readOnlyData(), true, getClassPath(page));
    assertEquals("fitnesse.slim.SlimService", debugDescriptor.getTestRunner());
  }

  @Test
  public void testCustomRunner() {
    String specifiedPageText = "!define REMOTE_DEBUG_RUNNER {Different runner}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), true, getClassPath(specifiedPage));
    assertEquals("Different runner", descriptor.getTestRunner());
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

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), false, getClassPath(specifiedPage));
    assertEquals(fitnesseRootpath + "/rubyslim.rb", descriptor.getTestRunner());
  }

}
