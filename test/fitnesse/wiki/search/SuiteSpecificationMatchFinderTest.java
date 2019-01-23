package fitnesse.wiki.search;

import fitnesse.wiki.HitCollector;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

public class SuiteSpecificationMatchFinderTest {

  private WikiPage root;
  private SuiteSpecificationMatchFinder finder;
  private HitCollector hits;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    WikiPageUtil.addPage(root, PathParser.parse("TestPageOne"), "TestPageOne has some testing content and a child\nThe meaning of life, the universe, and evertything is 42");
    WikiPageUtil.addPage(root, PathParser.parse("TestPageOne.ChildPage"), "ChildPage is a child of TestPageOne\nDo you believe in love after life?");
    WikiPageUtil.addPage(root, PathParser.parse("TestPageTwo"), "TestPageTwo has a bit of content too\nThere is no life without death");
    hits = new HitCollector();
  }

  @Test
  public void shouldBeAbleToFindAPageFromItsTitle() throws Exception {
    finder = new SuiteSpecificationMatchFinder("Test","",hits);
    finder.search(root);
    hits.assertPagesFound("TestPageOne","TestPageTwo");
  }

  @Test
  public void shouldBeAbleToFindAPageFromItsContent() throws Exception {
    finder = new SuiteSpecificationMatchFinder("","content",hits);
    finder.search(root);
    hits.assertPagesFound("TestPageOne","TestPageTwo");
  }

  @Test
  public void shouldHandleNullTitle() throws Exception {
    finder = new SuiteSpecificationMatchFinder(null,"child",hits);
    finder.search(root);
    hits.assertPagesFound("TestPageOne","ChildPage");
  }

  @Test
  public void shouldHandleNullContent() throws Exception {
    finder = new SuiteSpecificationMatchFinder("Child",null,hits);
    finder.search(root);
    hits.assertPagesFound("ChildPage");
  }

  @Test
  public void shouldBeAbleToUseRegExForContent() throws Exception {
    finder = new SuiteSpecificationMatchFinder(null,"has.*content", hits);
    finder.search(root);
    hits.assertPagesFound("TestPageOne", "TestPageTwo");
  }

  @Test
  public void shouldBeAbleToFindContentOverManyLines() throws Exception {
    finder = new SuiteSpecificationMatchFinder(null, "child.*life", hits);
    finder.search(root);
    hits.assertPagesFound("TestPageOne", "ChildPage");
  }
}

