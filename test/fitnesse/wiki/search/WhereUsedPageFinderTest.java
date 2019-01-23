package fitnesse.wiki.search;

import fitnesse.wiki.HitCollector;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;


public class WhereUsedPageFinderTest {
  private WikiPage root;
  private WikiPage pageOne;
  private WikiPage pageTwo;
  private WikiPage pageThree;
  private WikiPage pageTwoChild;

  private HitCollector hits = new HitCollector();

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    pageOne = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "this is page one, uncle of PageTwo.ChildPage");
    pageTwo = WikiPageUtil.addPage(root, PathParser.parse("PageTwo"), "I am Page Two my brother is PageOne. I have a >ChildPage. (and SomeMissingPage)");
    pageThree = WikiPageUtil.addPage(root, PathParser.parse("PageThree"), "This is !-PageThree-!, I Have \n!include PageTwo");
    pageTwoChild = WikiPageUtil.addPage(pageTwo, PathParser.parse("ChildPage"), "I am Child page, my uncle is .PageOne ");
  }

  @Test
  public void testFindReferencingPagesOnSiblingAndChild() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageOne, hits);
    whereUsed.search(root);
    hits.assertPagesFound(pageTwo.getName(), pageTwoChild.getName());
  }

  @Test
  public void testFindReferencingPagesOnSibling() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageTwo, hits);
    whereUsed.search(root);
    hits.assertPagesFound(pageThree.getName());
  }

  @Test
  public void testFindReferencingPagesNotReferenced() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, hits);
    whereUsed.search(root);
    hits.assertPagesFound();
  }

  @Test
  public void testFindReferencingPagesFromChild() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageTwoChild, hits);
    whereUsed.search(root);
    hits.assertPagesFound(pageOne.getName(), pageTwo.getName());
  }

  @Test
  public void testObserving() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageOne, hits);
    whereUsed.search(root);
    hits.assertPagesFound(pageTwo.getName(), pageTwoChild.getName());
  }

  @Test
  public void testOnlyOneReferencePerPage() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, hits);
    WikiPage newPage = WikiPageUtil.addPage(root, PathParser.parse("NewPage"), "one reference to PageThree.  Two reference to PageThree");
    whereUsed.search(root);
    hits.assertPagesFound(newPage.getName());
  }

  @Test
  public void testWordsNotFoundInPreprocessedText() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, hits);
    WikiPageUtil.addPage(root, PathParser.parse("NewPage"), "{{{ PageThree }}}");
    whereUsed.search(root);
    hits.assertPagesFound();
  }

  @Test
  public void testFindReferencingPagesWithLinksWithAlternateText() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, hits);
    WikiPage newPage = WikiPageUtil.addPage(root, PathParser.parse("NewPage"), "I enjoy being a sibling of [[the third page][PageThree]]");
    whereUsed.search(root);
    hits.assertPagesFound(newPage.getName());
  }

  @Test
  public void pleaseMindPagesWithSuffixAreNotFound() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, hits);
    WikiPage newPage = WikiPageUtil.addPage(root, PathParser.parse("NewPage"), "I enjoy being a sibling of [[the third page][PageThree?edit]]");
    whereUsed.search(root);
    hits.assertPagesFound(newPage.getName());
  }

  @Test
  public void testFinderShouldDealWithOtherLinks() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, hits);
    WikiPage newPage = WikiPageUtil.addPage(root, PathParser.parse("NewPage"), "I enjoy being a sibling of [[the third page][http://fitnesse.org]]");
    whereUsed.search(root);
    hits.assertPagesFound();
  }
}
