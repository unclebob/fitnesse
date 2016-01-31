package fitnesse.testsystems;

import java.io.IOException;
import java.util.Arrays;

import fitnesse.testrunner.WikiPageDescriptor;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static fitnesse.testsystems.ClientBuilder.replace;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
  public void buildDefaultTestSystemName() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), "");
    WikiPageDescriptor descriptor = new WikiPageDescriptor(testPage, false, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    String testSystemName = clientBuilder.getTestSystemName();
    assertEquals("fit:" + MOCK_TEST_RUNNER, testSystemName);
  }

  @Test
  public void buildTestSystemNameWhenTestSystemIsSlim() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), "!define TEST_SYSTEM {slim}\n");
    WikiPageDescriptor descriptor = new WikiPageDescriptor(testPage, false, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    String testSystemName = clientBuilder.getTestSystemName();
    assertEquals("slim:" + MOCK_TEST_RUNNER, testSystemName);
  }

  @Test
  public void buildTestSystemNameWhenTestSystemIsUnknownDefaultsToFit() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), "!define TEST_SYSTEM {X}\n");
    WikiPageDescriptor descriptor = new WikiPageDescriptor(testPage, false, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    String testSystemName = clientBuilder.getTestSystemName();
    assertEquals("X:" + MOCK_TEST_RUNNER, testSystemName);
  }

  @Test
  public void buildFullySpecifiedTestSystemName() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"),
            "!define TEST_SYSTEM {system}\n" +
                    "!define TEST_RUNNER {runner}\n");
    WikiPageDescriptor descriptor = new WikiPageDescriptor(testPage, false, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    String testSystemName = clientBuilder.getTestSystemName();
    Assert.assertEquals("system:runner", testSystemName);
  }

  @Test
  public void buildFullySpecifiedTestSystemNameForDebugRun() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"),
            "!define TEST_SYSTEM {system}\n" +
                    "!define TEST_RUNNER {runner}\n");
    WikiPageDescriptor descriptor = new WikiPageDescriptor(testPage, true, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    String testSystemName = clientBuilder.getTestSystemName();
    Assert.assertEquals("system:runner", testSystemName);
  }

  @Test
  public void buildFullySpecifiedTestSystemNameAndIdentifierForDebugRun() throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"),
            "!define TEST_SYSTEM {system:A}\n" +
                    "!define TEST_RUNNER {runner}\n");
    WikiPageDescriptor descriptor = new WikiPageDescriptor(testPage, true, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    String testSystemName = clientBuilder.getTestSystemName();
    Assert.assertEquals("system:A:runner", testSystemName);
  }



  @Test
  public void testCommandPatternCSharp() throws Exception {
    String specifiedPageText = "!define COMMAND_PATTERN {%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);
    WikiPageDescriptor descriptor = new WikiPageDescriptor(specifiedPage, false, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p", join(clientBuilder.getCommandPattern()));

  }

  @Test
  public void testCommandPatternCSharpWithDebug() throws Exception {
    String specifiedPageText = "!define COMMAND_PATTERN {%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);
    WikiPageDescriptor descriptor = new WikiPageDescriptor(specifiedPage, false, true, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p", join(clientBuilder.getCommandPattern()));
  }

  @Test
  public void testCommandPatternJava() throws Exception {

    String pageText = "!define TEST_SYSTEM {slim}\n";
    WikiPage page = makeTestPage(pageText);
    WikiPageDescriptor descriptor = new WikiPageDescriptor(page, false, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    String prefix = join(clientBuilder.getCommandPattern());
    assertTrue(prefix.contains("java"));
    assertTrue(prefix.contains(" -cp %p %m"));
  }

  @Test
  public void testCommandPatternJavaWithDebug() throws Exception {
    String pageText = "!define TEST_SYSTEM {slim}\n";
    WikiPage page = makeTestPage(pageText);
    WikiPageDescriptor descriptor = new WikiPageDescriptor(page, false, true, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    String prefix = join(clientBuilder.getCommandPattern());
    assertTrue(prefix.contains("java"));
    assertTrue(prefix.contains(" -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -cp %p %m"));
  }

  @Test
  public void testCommandPatternJavaWithDefinedCommands() {
    String specifiedPageText = "!define COMMAND_PATTERN {java -specialParam -cp %p %m}\n"
            + "!define REMOTE_DEBUG_COMMAND {java -remoteDebug -cp %p %m}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage, false, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("java -specialParam -cp %p %m", join(clientBuilder.getCommandPattern()));

  }

  @Test
  public void testCommandPatternJavaWithDefinedCommandsWithDebug() {
    String specifiedPageText = "!define COMMAND_PATTERN {java -specialParam -cp %p %m}\n"
            + "!define REMOTE_DEBUG_COMMAND {java -remoteDebug -cp %p %m}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);

    Descriptor descriptor = new WikiPageDescriptor(specifiedPage, false, true, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("java -remoteDebug -cp %p %m", join(clientBuilder.getCommandPattern()));
  }

  @Test
  public void testCommandPatternWithVariable() throws Exception {
    String specifiedPageText = "!define COMMAND_PATTERN (${MY_RUNNER} %p %m)\n"
            + "!define MY_RUNNER {rubyslim}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);
    WikiPageDescriptor descriptor = new WikiPageDescriptor(specifiedPage, false, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("rubyslim %p %m", join(clientBuilder.getCommandPattern()));
  }

  @Test
  public void testTestRunnerWithVariable() throws Exception {
    String specifiedPageText = "!define TEST_RUNNER (${MY_RUNNER}.rb)\n"
            + "!define MY_RUNNER {rubyslim}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);
    WikiPageDescriptor descriptor = new WikiPageDescriptor(specifiedPage, false, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("rubyslim.rb", clientBuilder.getTestRunner());
  }

  @Test
  public void testRunnerCSharp() throws Exception {
    String specifiedPageText = "!define TEST_RUNNER {..\\fitnesse\\fitsharp\\Runner.exe}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);
    WikiPageDescriptor descriptor = new WikiPageDescriptor(specifiedPage, false, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("..\\fitnesse\\fitsharp\\Runner.exe", clientBuilder.getTestRunner());
  }

  @Test
  public void testRunnerCSharpWithDebug() throws Exception {
    String specifiedPageText = "!define TEST_RUNNER {..\\fitnesse\\fitsharp\\Runner.exe}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);
    WikiPageDescriptor descriptor = new WikiPageDescriptor(specifiedPage, false, true, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("..\\fitnesse\\fitsharp\\runnerw.exe", clientBuilder.getTestRunner());
  }

  @Test
  public void testRunnerDefault() throws Exception {
    String pageText = "!define TEST_SYSTEM {slim}\n";
    WikiPage page = makeTestPage(pageText);
    WikiPageDescriptor descriptor = new WikiPageDescriptor(page, false, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals(MOCK_TEST_RUNNER, clientBuilder.getTestRunner());
  }

  @Test
  public void testRunnerDefaultWithDebug() throws Exception {
    String pageText = "!define TEST_SYSTEM {slim}\n";
    WikiPage page = makeTestPage(pageText);
    WikiPageDescriptor descriptor = new WikiPageDescriptor(page, false, true, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals(MOCK_TEST_RUNNER, clientBuilder.getTestRunner());
  }

  @Test
  public void testCustomRunner() {
    String specifiedPageText = "!define REMOTE_DEBUG_RUNNER {Different runner}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);
    WikiPageDescriptor descriptor = new WikiPageDescriptor(specifiedPage, false, true, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals("Different runner", clientBuilder.getTestRunner());
  }

  @Test
  public void testTestRunnerWithRootPathVariable() throws Exception {
    String fitnesseRootpath = System.getProperty("user.home");
    String specifiedPageText = "!define TEST_RUNNER (${user.home}/rubyslim.rb)\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);
    WikiPageDescriptor descriptor = new WikiPageDescriptor(specifiedPage, false, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertEquals(fitnesseRootpath + "/rubyslim.rb", clientBuilder.getTestRunner());
  }

  @Test
  public void testCommandPatternCSharpWithSuiteConfig() throws Exception {
    String specifiedPageText = "!define COMMAND_PATTERN {%m -r fitSharp.Slim.Service.Runner,fitsharp.dll -c \"c:\\program files\\suite.config\" %p}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);
    WikiPageDescriptor descriptor = new WikiPageDescriptor(specifiedPage, false, false, "");
    MockClientBuilder clientBuilder = new MockClientBuilder(descriptor);
    assertTrue(Arrays.asList(clientBuilder.getCommandPattern()).contains("c:\\program files\\suite.config"));

  }

  private WikiPage makeTestPage(String pageText) {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    return WikiPageUtil.addPage(root, PathParser.parse("TestPage"), pageText);
  }

  private String join(String[] args) {
    return StringUtils.join(Arrays.asList(args), " ");
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
