package fitnesse.responders.run;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import fitnesse.responders.run.TestSystem.Descriptor;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class TestSystemTest {
  
  @Test
  public void testCommandPattern() throws Exception {
    
    String pageText = "!define TEST_SYSTEM {slim}\n";
    WikiPage page = makeTestPage(pageText);
    
    Descriptor defaultDescriptor = TestSystem.getDescriptor(page.getData(), false);
    String sep = System.getProperty("path.separator");
    assertEquals("java -cp fitnesse.jar"+sep+"%p %m", defaultDescriptor.commandPattern);

    Descriptor debugDescriptor = TestSystem.getDescriptor(page.getData(), true);
    assertEquals("java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -cp %p %m", debugDescriptor.commandPattern);
    
    
    String specifiedPageText = "!define COMMAND_PATTERN {java -specialParam -cp %p %m}\n" +
                               "!define REMOTE_DEBUG_COMMAND {java -remoteDebug -cp %p %m}";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);
    
    Descriptor defaultDescriptor2 = TestSystem.getDescriptor(specifiedPage.getData(), false);
    assertEquals("java -specialParam -cp %p %m", defaultDescriptor2.commandPattern);

    Descriptor debugDescriptor2 = TestSystem.getDescriptor(specifiedPage.getData(), true);
    assertEquals("java -remoteDebug -cp %p %m", debugDescriptor2.commandPattern);
  }

  @Test
  public void testCommandPatternWithVariable() throws Exception {
    String specifiedPageText = "!define COMMAND_PATTERN (${MY_RUNNER} %p %m)\n" +
                               "!define MY_RUNNER {rubyslim}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);
    
    Descriptor myDescriptor = TestSystem.getDescriptor(specifiedPage.getData(), false);
    assertEquals("rubyslim %p %m", myDescriptor.commandPattern);
  }

  @Test
  public void testTestRunnerWithVariable() throws Exception {
    String specifiedPageText = "!define TEST_RUNNER (${MY_RUNNER}.rb)\n" +
                               "!define MY_RUNNER {rubyslim}\n";
    WikiPage specifiedPage = makeTestPage(specifiedPageText);
    
    Descriptor myDescriptor = TestSystem.getDescriptor(specifiedPage.getData(), false);
    assertEquals("rubyslim.rb", myDescriptor.testRunner);
  }

  WikiPage makeTestPage(String pageText) throws Exception {
    WikiPage root = InMemoryPage.makeRoot("RooT");
    PageCrawler crawler = root.getPageCrawler();
    return crawler.addPage(root, PathParser.parse("TestPage"), pageText);
  }

}
