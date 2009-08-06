package fitnesse.components;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SuiteSpecificationMatchFinderTest implements SearchObserver {

  WikiPage root;
  private List<WikiPage> hits = new ArrayList<WikiPage>();
  private PageCrawler crawler;
  SuiteSpecificationMatchFinder finder;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    crawler.addPage(root, PathParser.parse("TestPageOne"), "TestPageOne has some testing content and a child\nThe meaning of life, the universe, and evertything is 42");
    crawler.addPage(root, PathParser.parse("TestPageOne.ChildPage"), "ChildPage is a child of TestPageOne\nDo you believe in love after life?");
    crawler.addPage(root, PathParser.parse("TestPageTwo"), "TestPageTwo has a bit of content too\nThere is no life without death");
    hits.clear();
  }

  @Test
  public void shouldBeAbleToFindAPageFromItsTitle() throws Exception {
    finder = new SuiteSpecificationMatchFinder("Test","",this);
    finder.search(root);
    assertPagesFound("TestPageOne","TestPageTwo");
  }

  @Test
  public void shouldBeAbleToFindAPageFromItsContent() throws Exception {
    finder = new SuiteSpecificationMatchFinder("","content",this);
    finder.search(root);
    assertPagesFound("TestPageOne","TestPageTwo");
  }

  @Test
  public void shouldHandleNullTitle() throws Exception {
    finder = new SuiteSpecificationMatchFinder(null,"child",this);
    finder.search(root);
    assertPagesFound("TestPageOne","ChildPage");
  }

  @Test
  public void shouldHandleNullContent() throws Exception {
    finder = new SuiteSpecificationMatchFinder("Child",null,this);
    finder.search(root);
    assertPagesFound("ChildPage"); 
  }

  @Test
  public void shouldBeAbleToUseRegExForContent() throws Exception {
    finder = new SuiteSpecificationMatchFinder(null,"has.*content", this);
    finder.search(root);
    assertPagesFound("TestPageOne", "TestPageTwo");
  }

  @Test
  public void shouldBeAbleToFindContentOverManyLines() throws Exception {
    finder = new SuiteSpecificationMatchFinder(null, "child.*life", this);
    finder.search(root);
    assertPagesFound("TestPageOne", "ChildPage");
  }
  public void hit(WikiPage page) throws Exception {
    hits.add(page);
  }

  private void assertPagesFound(String... pageNames) throws Exception {
    assertEquals(pageNames.length, hits.size());

    List<String> pageNameList = Arrays.asList(pageNames);
    for (WikiPage page: hits) {
      assertTrue(pageNameList.contains(page.getName()));
    }
  }
}
