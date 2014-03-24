package fitnesse.testsystems;

import java.io.File;
import java.io.IOException;

import fitnesse.testrunner.WikiPageDescriptor;
import fitnesse.wiki.ClassPathBuilder;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static fitnesse.testsystems.ClientBuilder.replace;
import static org.junit.Assert.assertEquals;

public class ClientBuilderTest {

  private static final String MOCK_TEST_RUNNER = "default-test-runner";


  private WikiPage root;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
  }

  @Test
  public void shouldReplaceMarkWithValue() {
    assertEquals("Hello world", replace("Hello %p", "%p", "world"));
    assertEquals("/path/to/somewhere", replace("/path/%p/somewhere", "%p", "to"));
    assertEquals("/path/to/somewhere", replace("/path%p", "%p", "/to/somewhere"));
    assertEquals("\\path\\to\\somewhere", replace("\\path\\%p\\somewhere", "%p", "to"));
    assertEquals("\\path\\to\\somewhere", replace("\\path%p", "%p", "\\to\\somewhere"));
  }

  @Test
  public void shouldIncludeStandaloneJarByDefault() {
    assertEquals("fitnesse.jar", ClientBuilder.fitnesseJar("fitnesse.jar"));
    assertEquals("fitnesse-20121220.jar",
            ClientBuilder.fitnesseJar("fitnesse-20121220.jar"));
    assertEquals("fitnesse-standalone.jar",
            ClientBuilder.fitnesseJar("fitnesse-standalone.jar"));
    assertEquals("fitnesse-standalone-20121220.jar",
            ClientBuilder.fitnesseJar("fitnesse-standalone-20121220.jar"));
    assertEquals("fitnesse.jar",
            ClientBuilder.fitnesseJar("fitnesse-book.jar"));
    assertEquals(
            "fitnesse-standalone-20121220.jar",
            ClientBuilder.fitnesseJar(String
                    .format("irrelevant.jar%1$sfitnesse-book.jar%1$sfitnesse-standalone-20121220.jar",
                            System.getProperty("path.separator"))));
    assertEquals(String.format("lib%sfitnesse-standalone.jar", File.separator),
            ClientBuilder.fitnesseJar(String.format("lib%sfitnesse-standalone.jar", File.separator)));
  }


  @Test
  public void buildDefaultTestSystemName() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), "");
    WikiPageDescriptor descriptor = new WikiPageDescriptor(testPage.readOnlyData(), false, false, new ClassPathBuilder().getClasspath(testPage));
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    String testSystemName = clientBuilder.getTestSystemName();
    assertEquals("fit:" + MOCK_TEST_RUNNER, testSystemName);
  }

  @Test
  public void buildTestSystemNameWhenTestSystemIsSlim() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), "!define TEST_SYSTEM {slim}\n");
    WikiPageDescriptor descriptor = new WikiPageDescriptor(testPage.readOnlyData(), false, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    String testSystemName = clientBuilder.getTestSystemName();
    assertEquals("slim:" + MOCK_TEST_RUNNER, testSystemName);
  }

  @Test
  public void buildTestSystemNameWhenTestSystemIsUnknownDefaultsToFit() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), "!define TEST_SYSTEM {X}\n");
    WikiPageDescriptor descriptor = new WikiPageDescriptor(testPage.readOnlyData(), false, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    String testSystemName = clientBuilder.getTestSystemName();
    assertEquals("X:" + MOCK_TEST_RUNNER, testSystemName);
  }

  @Test
  public void buildFullySpecifiedTestSystemName() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"),
            "!define TEST_SYSTEM {system}\n" +
                    "!define TEST_RUNNER {runner}\n");
    WikiPageDescriptor descriptor = new WikiPageDescriptor(testPage.readOnlyData(), false, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    String testSystemName = clientBuilder.getTestSystemName();
    Assert.assertEquals("system:runner", testSystemName);
  }

  @Test
  public void buildFullySpecifiedTestSystemNameForDebugRun() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"),
            "!define TEST_SYSTEM {system}\n" +
                    "!define TEST_RUNNER {runner}\n");
    WikiPageDescriptor descriptor = new WikiPageDescriptor(testPage.readOnlyData(), true, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    String testSystemName = clientBuilder.getTestSystemName();
    Assert.assertEquals("system:runner", testSystemName);
  }

  @Test
  public void buildFullySpecifiedTestSystemNameAndIdentifierForDebugRun() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"),
            "!define TEST_SYSTEM {system:A}\n" +
                    "!define TEST_RUNNER {runner}\n");
    WikiPageDescriptor descriptor = new WikiPageDescriptor(testPage.readOnlyData(), true, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    String testSystemName = clientBuilder.getTestSystemName();
    Assert.assertEquals("system:A:runner", testSystemName);
  }



  @Test
  public void testCommandPatternCSharp() throws Exception {
    String specifiedPageText = "!define COMMAND_PATTERN {%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), false, false, getClassPath(specifiedPage));
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p", clientBuilder.getCommandPattern());

  }

  @Test
  public void testCommandPatternCSharpWithDebug() throws Exception {
    String specifiedPageText = "!define COMMAND_PATTERN {%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), false, true, getClassPath(specifiedPage));
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p", clientBuilder.getCommandPattern());
  }

  @Test
  public void testCommandPatternJava() throws Exception {

    String pageText = "!define TEST_SYSTEM {slim}\n";
    WikiPage page = makeTestPage(pageText);

    Descriptor descriptor = new WikiPageDescriptor(page.readOnlyData(), false, false, getClassPath(page));
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    String sep = System.getProperty("path.separator");
    String prefix = javaExecutablePrefix();
    assertEquals(prefix + "java -cp fitnesse.jar" + sep + "%p %m", clientBuilder.getCommandPattern());
  }

  @Test
  public void testCommandPatternJavaWithDebug() throws Exception {

    String pageText = "!define TEST_SYSTEM {slim}\n";
    WikiPage page = makeTestPage(pageText);

    Descriptor descriptor = new WikiPageDescriptor(page.readOnlyData(), false, true, getClassPath(page));
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    String prefix = javaExecutablePrefix();
    assertEquals(
            prefix + "java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -cp %p %m",
            clientBuilder.getCommandPattern());
  }

  @Test
  public void testCommandPatternJavaWithDefinedCommands() {
    String specifiedPageText = "!define COMMAND_PATTERN {java -specialParam -cp %p %m}\n"
            + "!define REMOTE_DEBUG_COMMAND {java -remoteDebug -cp %p %m}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), false, false, getClassPath(specifiedPage));
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("java -specialParam -cp %p %m", clientBuilder.getCommandPattern());

  }

  @Test
  public void testCommandPatternJavaWithDefinedCommandsWithDebug() {
    String specifiedPageText = "!define COMMAND_PATTERN {java -specialParam -cp %p %m}\n"
            + "!define REMOTE_DEBUG_COMMAND {java -remoteDebug -cp %p %m}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), false, true, getClassPath(specifiedPage));
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("java -remoteDebug -cp %p %m", clientBuilder.getCommandPattern());
  }

  @Test
  public void testCommandPatternWithVariable() throws Exception {
    String specifiedPageText = "!define COMMAND_PATTERN (${MY_RUNNER} %p %m)\n"
            + "!define MY_RUNNER {rubyslim}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), false, false, getClassPath(specifiedPage));
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("rubyslim %p %m", clientBuilder.getCommandPattern());
  }

  @Test
  public void testTestRunnerWithVariable() throws Exception {
    String specifiedPageText = "!define TEST_RUNNER (${MY_RUNNER}.rb)\n"
            + "!define MY_RUNNER {rubyslim}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), false, false, getClassPath(specifiedPage));
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("rubyslim.rb", clientBuilder.getTestRunner());
  }

  @Test
  public void testRunnerCSharp() throws Exception {
    String specifiedPageText = "!define TEST_RUNNER {..\\fitnesse\\fitsharp\\Runner.exe}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), false, false, getClassPath(specifiedPage));
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("..\\fitnesse\\fitsharp\\Runner.exe", clientBuilder.getTestRunner());
  }

  @Test
  public void testRunnerCSharpWithDebug() throws Exception {
    String specifiedPageText = "!define TEST_RUNNER {..\\fitnesse\\fitsharp\\Runner.exe}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), false, true, getClassPath(specifiedPage));
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("..\\fitnesse\\fitsharp\\runnerw.exe", clientBuilder.getTestRunner());
  }

  @Test
  public void testRunnerDefault() throws Exception {
    String pageText = "!define TEST_SYSTEM {slim}\n";
    WikiPage page = makeTestPage(pageText);

    Descriptor descriptor = new WikiPageDescriptor(page.readOnlyData(), false, false, getClassPath(page));
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals(MOCK_TEST_RUNNER, clientBuilder.getTestRunner());
  }

  @Test
  public void testRunnerDefaultWithDebug() throws Exception {
    String pageText = "!define TEST_SYSTEM {slim}\n";
    WikiPage page = makeTestPage(pageText);


    Descriptor descriptor = new WikiPageDescriptor(page.readOnlyData(), false, true, getClassPath(page));
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals(MOCK_TEST_RUNNER, clientBuilder.getTestRunner());
  }

  @Test
  public void testCustomRunner() {
    String specifiedPageText = "!define REMOTE_DEBUG_RUNNER {Different runner}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), false, true, getClassPath(specifiedPage));
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("Different runner", clientBuilder.getTestRunner());
  }

  @Test
  public void testTestRunnerWithRootPathVariable() throws Exception {
    String fitnesseRootpath = System.getProperty("user.home");

    String specifiedPageText = "!define TEST_RUNNER (${user.home}/rubyslim.rb)\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage.readOnlyData(), false, false, getClassPath(specifiedPage));
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals(fitnesseRootpath + "/rubyslim.rb", clientBuilder.getTestRunner());
  }

  private String getClassPath(WikiPage page) {
    return new ClassPathBuilder().getClasspath(page);
  }


  private WikiPage makeTestPage(String pageText) {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    return WikiPageUtil.addPage(root, PathParser.parse("TestPage"), pageText);
  }

  private String javaExecutablePrefix() {
    String javaHome = System.getenv("JAVA_HOME");
    if (javaHome == null) {
        return "";
    }
    else {
        String sep = System.getProperty("file.separator");
        return javaHome + sep + "bin" + sep;
    }
  }

  public static class MockClientBuilder extends ClientBuilder<MockClient> {

    public MockClientBuilder(Descriptor descriptor) {
      super(descriptor);
    }

    @Override
    public MockClient build() throws IOException {
      return new MockClient();
    }

    @Override
    protected String defaultTestRunner() {
      return MOCK_TEST_RUNNER;
    }
  }

  public static class MockClient {
  }
}
