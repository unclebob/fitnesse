package fitnesse.html.template;

import static org.junit.Assert.assertTrue;

import fitnesse.wiki.*;
import org.apache.velocity.VelocityContext;
import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.components.TraversalListener;
import fitnesse.components.Traverser;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.fs.InMemoryPage;

public class TraverseDirectiveTest {

  private FitNesseContext context;
  
  @Before
  public void setUp() {
    context = FitNesseUtil.makeTestContext();
    context.pageFactory.getVelocityEngine().loadDirective(TraverseDirective.class.getName());
  }
  
  @Test
  public void testRender() {
    
    VelocityContext velocityContext = new VelocityContext();
    
    velocityContext.put("resultResponder", new MockTraverser());
    
    String tmpl = context.pageFactory.render(velocityContext, "searchResults.vm");
    
    assertTrue(tmpl.contains("<a href=\"PageOne\">PageOne</a>"));
  }

  public static class MockTraverser implements Traverser<WikiPage> {

    @Override
    public void traverse(TraversalListener<WikiPage> observer) {
      WikiPage root = InMemoryPage.makeRoot("root");
      observer.process(WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "PageOne"));
      observer.process(WikiPageUtil.addPage(root, PathParser.parse("PageTwo"), "PageOne"));
      observer.process(WikiPageUtil.addPage(root, PathParser.parse("ChildPage"), ".PageOne"));
    }
  }
}
