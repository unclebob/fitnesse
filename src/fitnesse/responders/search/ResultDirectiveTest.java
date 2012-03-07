package fitnesse.responders.search;

import static org.junit.Assert.assertTrue;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.components.SearchObserver;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class ResultDirectiveTest {

  private WikiPage root;
  private FitNesseContext context;
  
  @Before
  public void setUp() {
    root = InMemoryPage.makeRoot("root");
    context = FitNesseUtil.makeTestContext(root);
    context.pageFactory.getVelocityEngine().loadDirective(ResultDirective.class.getName());
  }
  
  @Test
  public void testRender() {
    
    VelocityContext velocityContext = new VelocityContext();
    
    velocityContext.put("resultResponder", new MockResultResponder());
    
    String tmpl = context.pageFactory.render(velocityContext, "searchResults.vm");
    
    assertTrue(tmpl.contains("<a href=\"PageOne\">PageOne</a>"));
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
