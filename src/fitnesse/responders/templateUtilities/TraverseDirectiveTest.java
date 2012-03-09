package fitnesse.responders.templateUtilities;

import static org.junit.Assert.assertTrue;

import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.components.TraversalListener;
import fitnesse.components.Traverser;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

public class TraverseDirectiveTest {

  private WikiPage root;
  private FitNesseContext context;
  
  @Before
  public void setUp() {
    root = InMemoryPage.makeRoot("root");
    context = FitNesseUtil.makeTestContext(root);
    context.pageFactory.getVelocityEngine().loadDirective(TraverseDirective.class.getName());
  }
  
  @Test
  public void testRender() {
    
    VelocityContext velocityContext = new VelocityContext();
    
    velocityContext.put("resultResponder", new MockTraverser());
    
    String tmpl = context.pageFactory.render(velocityContext, "searchResults.vm");
    
    assertTrue(tmpl.contains("<a href=\"PageOne\">PageOne</a>"));
  }

  public static class MockTraverser implements Traverser {

    @Override
    public void traverse(TraversalListener observer) {
      WikiPage root = InMemoryPage.makeRoot("root");
      PageCrawler crawler = root.getPageCrawler();
      observer.process(crawler.addPage(root, PathParser.parse("PageOne"), "PageOne"));
      observer.process(crawler.addPage(root, PathParser.parse("PageTwo"), "PageOne"));
      observer.process(crawler.addPage(root, PathParser.parse("ChildPage"), ".PageOne"));
    }
  }
}
