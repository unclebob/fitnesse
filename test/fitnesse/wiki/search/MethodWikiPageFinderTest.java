package fitnesse.wiki.search;

import fitnesse.wiki.HitCollector;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

public class MethodWikiPageFinderTest {

  private WikiPage root;
  private WikiPage pageOne;
  private WikiPage childPage1;
  private WikiPage childPage2;

  HitCollector hits = new HitCollector();
  private WikiPageFinder pageFinder;

  @Before
  public void setUp() {
    root = InMemoryPage.makeRoot("RooT");
    pageOne = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "|method with no param|");
    childPage1 = WikiPageUtil.addPage(root, PathParser.parse("PageOne.PageOneChild1"),
      "|check|method with one param|p1|");
    childPage2 = WikiPageUtil.addPage(root, PathParser.parse("PageOne.PageOneChild2"),
      "|method     with    no    param|" + System.lineSeparator() + "^PageOneChild");
  }

  @Test
  public void searcherNoMethodMatch() {
    pageFinder = pageFinder("no");
    pageFinder.search(root);
    hits.assertPagesFound();
  }

  @Test
  public void singlePageMatches() {
    pageFinder = pageFinder("|method with one param||");
    pageFinder.search(root);
    hits.assertPagesFound(childPage1.getName());
  }

  @Test
  public void multiplePageMatch() {
    pageFinder = pageFinder("|method with no param|");
    pageFinder.search(root);
    hits.assertPagesFound(pageOne.getName(), childPage2.getName());
  }

  @Test
  public void matchWithDifferentTextFormatting() {
    pageFinder = pageFinder("|methodWithNoParam|");
    pageFinder.search(root);
    hits.assertPagesFound(pageOne.getName(), childPage2.getName());
  }

  @Test
  public void matchWithDifferentTextFormatting2() {
    pageFinder = pageFinder("|method with one |p1|param|");
    pageFinder.search(root);
    hits.assertPagesFound(childPage1.getName());
  }

  @Test
  public void matchWithDifferentTextFormatting3() {
    pageFinder = pageFinder("|method |p1|with one param|");
    pageFinder.search(root);
    hits.assertPagesFound(childPage1.getName());
  }

  @Test
  public void matchWithDifferentTextFormatting4() {
    pageFinder = pageFinder("|method       with       one |    p1|    param|");
    pageFinder.search(root);
    hits.assertPagesFound(childPage1.getName());
  }

  @Test
  public void matchWithDifferentKeywordEnsure() {
    pageFinder = pageFinder("|ensure|method withNoParam|");
    pageFinder.search(root);
    hits.assertPagesFound(pageOne.getName(), childPage2.getName());
  }

  @Test
  public void matchWithDifferentKeywordReject() {
    pageFinder = pageFinder("|reject|methodWith noParam|");
    pageFinder.search(root);
    hits.assertPagesFound(pageOne.getName(), childPage2.getName());
  }

  @Test
  public void matchWithDifferentKeywordCheck() {
    pageFinder = pageFinder("|check|method with one param|p1|checkValue|");
    pageFinder.search(root);
    hits.assertPagesFound(childPage1.getName());
  }

  @Test
  public void matchWithDifferentKeywordCheckNot() {
    pageFinder = pageFinder("|check not|method with one param|p1|checkNotValue|");
    pageFinder.search(root);
    hits.assertPagesFound(childPage1.getName());
  }

  @Test
  public void matchWithDifferentKeywordShow() {
    pageFinder = pageFinder("|show|methodWithNoParam|");
    pageFinder.search(root);
    hits.assertPagesFound(pageOne.getName(), childPage2.getName());
  }

  @Test
  public void matchWithDifferentKeywordNote() {
    pageFinder = pageFinder("|note|methodWithNoParam|");
    pageFinder.search(root);
    hits.assertPagesFound(pageOne.getName(), childPage2.getName());
  }

  @Test
  public void matchWithDifferentKeywordEmpty() {
    pageFinder = pageFinder("||methodWithNoParam|");
    pageFinder.search(root);
    hits.assertPagesFound(pageOne.getName(), childPage2.getName());
  }

  @Test
  public void matchWithAssignedValue() {
    pageFinder = pageFinder("|$value=|method with noParam|");
    pageFinder.search(root);
    hits.assertPagesFound(pageOne.getName(), childPage2.getName());
  }

  private WikiPageFinder pageFinder(String searchText) {
    return new MethodWikiPageFinder(searchText, hits);
  }

}
