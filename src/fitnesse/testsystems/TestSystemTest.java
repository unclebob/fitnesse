package fitnesse.testsystems;

import static org.junit.Assert.assertEquals;

import java.io.File;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.testsystems.TestSystem.Descriptor;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

public class TestSystemTest {

  private FitNesseContext context;

  @Before
  public void setUp() {
    context = FitNesseUtil.makeTestContext();
  }

  @Test
  public void testCommandPatternCSharp() throws Exception {
    String specifiedPageText = "!define COMMAND_PATTERN {%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor defaultDescriptor2 = TestSystem.getDescriptor(specifiedPage, context.pageFactory, false);
    assertEquals("%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p", defaultDescriptor2.getCommandPattern());

    Descriptor defaultDescriptor3 = TestSystem.getDescriptor(specifiedPage, context.pageFactory, true);
    assertEquals("%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p", defaultDescriptor3.getCommandPattern());
  }


  @Test
  public void testCommandPatternJava() throws Exception {

    String pageText = "!define TEST_SYSTEM {slim}\n";
    WikiPage page = makeTestPage(pageText);

    Descriptor defaultDescriptor = TestSystem.getDescriptor(page, context.pageFactory, false);
    String sep = System.getProperty("path.separator");
    assertEquals("java -cp fitnesse.jar" + sep + "%p %m", defaultDescriptor.getCommandPattern());

    Descriptor debugDescriptor = TestSystem.getDescriptor(page, context.pageFactory, true);
    assertEquals(
        "java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -cp %p %m",
        debugDescriptor.getCommandPattern());

    String specifiedPageText = "!define COMMAND_PATTERN {java -specialParam -cp %p %m}\n"
        + "!define REMOTE_DEBUG_COMMAND {java -remoteDebug -cp %p %m}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor defaultDescriptor2 = TestSystem.getDescriptor(specifiedPage, context.pageFactory, false);
    assertEquals("java -specialParam -cp %p %m", defaultDescriptor2.getCommandPattern());

    Descriptor debugDescriptor2 = TestSystem.getDescriptor(specifiedPage, context.pageFactory, true);
    assertEquals("java -remoteDebug -cp %p %m", debugDescriptor2.getCommandPattern());
  }

  @Test
  public void testCommandPatternWithVariable() throws Exception {
    String specifiedPageText = "!define COMMAND_PATTERN (${MY_RUNNER} %p %m)\n"
        + "!define MY_RUNNER {rubyslim}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor myDescriptor = TestSystem.getDescriptor(specifiedPage, context.pageFactory, false);
    assertEquals("rubyslim %p %m", myDescriptor.getCommandPattern());
  }

  @Test
  public void testTestRunnerWithVariable() throws Exception {
    String specifiedPageText = "!define TEST_RUNNER (${MY_RUNNER}.rb)\n"
        + "!define MY_RUNNER {rubyslim}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor myDescriptor = TestSystem.getDescriptor(specifiedPage, context.pageFactory, false);
    assertEquals("rubyslim.rb", myDescriptor.getTestRunner());
  }

  @Test
  public void testRunnerCSharp() throws Exception {
    String specifiedPageText = "!define TEST_RUNNER {..\\fitnesse\\fitsharp\\Runner.exe}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor defaultDescriptor2 = TestSystem.getDescriptor(specifiedPage, context.pageFactory, false);
    assertEquals("..\\fitnesse\\fitsharp\\Runner.exe", defaultDescriptor2.getTestRunner());
    Descriptor defaultDescriptor3 = TestSystem.getDescriptor(specifiedPage, context.pageFactory, true);
    assertEquals("..\\fitnesse\\fitsharp\\runnerw.exe", defaultDescriptor3.getTestRunner());
  }

  @Test
  public void testRunnerDefault() throws Exception {
    String pageText = "!define TEST_SYSTEM {slim}\n";
    WikiPage page = makeTestPage(pageText);

    Descriptor defaultDescriptor2 = TestSystem.getDescriptor(page, context.pageFactory, false);
    assertEquals("fitnesse.slim.SlimService", defaultDescriptor2.getTestRunner());
    Descriptor defaultDescriptor3 = TestSystem.getDescriptor(page, context.pageFactory, true);
    assertEquals("fitnesse.slim.SlimService", defaultDescriptor3.getTestRunner());

    String specifiedPageText = "!define REMOTE_DEBUG_RUNNER {Different runner}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor specifiedDescriptor = TestSystem.getDescriptor(specifiedPage, context.pageFactory, true);
    assertEquals("Different runner", specifiedDescriptor.getTestRunner());
  }

  WikiPage makeTestPage(String pageText) throws Exception {
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

    Descriptor myDescriptor = TestSystem.getDescriptor(specifiedPage, context.pageFactory, false);
    assertEquals(fitnesseRootpath + "/rubyslim.rb", myDescriptor.getTestRunner());
  }

  @Test
  public void shouldReplaceMarkWithValue() {
    assertEquals("Hello world", TestSystem.replace("Hello %p", "%p", "world"));
    assertEquals("/path/to/somewhere", TestSystem.replace("/path/%p/somewhere", "%p", "to"));
    assertEquals("/path/to/somewhere", TestSystem.replace("/path%p", "%p", "/to/somewhere"));
    assertEquals("\\path\\to\\somewhere", TestSystem.replace("\\path\\%p\\somewhere", "%p", "to"));
    assertEquals("\\path\\to\\somewhere", TestSystem.replace("\\path%p", "%p", "\\to\\somewhere"));
  }

  @Test
  public void shouldIncludeStandaloneJarByDefault() {
    assertEquals("fitnesse.jar", TestSystem.fitnesseJar("fitnesse.jar"));
    assertEquals("fitnesse-20121220.jar",
            TestSystem.fitnesseJar("fitnesse-20121220.jar"));
    assertEquals("fitnesse-standalone.jar",
            TestSystem.fitnesseJar("fitnesse-standalone.jar"));
    assertEquals("fitnesse-standalone-20121220.jar",
            TestSystem.fitnesseJar("fitnesse-standalone-20121220.jar"));
    assertEquals("fitnesse.jar",
            TestSystem.fitnesseJar("fitnesse-book.jar"));
    assertEquals(
            "fitnesse-standalone-20121220.jar",
            TestSystem.fitnesseJar(String
                    .format("irrelevant.jar%1$sfitnesse-book.jar%1$sfitnesse-standalone-20121220.jar",
                            System.getProperty("path.separator"))));
    assertEquals(String.format("lib%sfitnesse-standalone.jar", File.separator),
            TestSystem.fitnesseJar(String.format("lib%sfitnesse-standalone.jar", File.separator)));
  }
}
