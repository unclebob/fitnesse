package fitnesse.responders.search;

import static org.junit.Assert.assertEquals;

import java.io.StringWriter;
import java.io.Writer;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeInstance;
import org.junit.Before;
import org.junit.Test;

import fitnesse.VelocityFactory;
import fitnesse.components.SearchObserver;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class ResultDirectiveTest {

  private WikiPage root;
  
  @Before
  public void setUp() {
    root = InMemoryPage.makeRoot("root");
    FitNesseUtil.makeTestContext(root);
    VelocityFactory.getVelocityEngine().loadDirective(ResultDirective.class.getName());
  }
  
  @Test
  public void testRender() {
    
    VelocityContext context = new VelocityContext();
    
    context.put("resultResponder", new MockResultResponder());
    
    String tmpl = VelocityFactory.translateTemplate(context, "searchResults.vm");
    
    assertEquals("\n\n test\n lorem ipsu...", tmpl);
  }

  public static class MockResultResponder extends ResultResponder {

    @Override
    protected String getTitle() {
      return "mock-title";
    }

    @Override
    protected void startSearching(SearchObserver observer) {
      WikiPage root = InMemoryPage.makeRoot("root");
      PageCrawler crawler = root.getPageCrawler();
      observer.hit(crawler.addPage(root, PathParser.parse("PageOne"), "PageOne"));
      observer.hit(crawler.addPage(root, PathParser.parse("PageTwo"), "PageOne"));
      observer.hit(crawler.addPage(root, PathParser.parse("ChildPage"), ".PageOne"));
    }
  }
}
