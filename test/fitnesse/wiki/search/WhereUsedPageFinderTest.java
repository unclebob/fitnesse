package fitnesse.wiki.search;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;

import org.junit.Before;
import org.junit.Test;


public class WhereUsedPageFinderTest implements TraversalListener<WikiPage> {
  private WikiPage root;
  private WikiPage pageOne;
  private WikiPage pageTwo;
  private WikiPage pageThree;
  private WikiPage pageTwoChild;

  private List<WikiPage> hits = new ArrayList<>();

  @Override
  public void process(WikiPage page) {
    hits.add(page);
  }

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    pageOne = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "this is page one, uncle of PageTwo.ChildPage");
    pageTwo = WikiPageUtil.addPage(root, PathParser.parse("PageTwo"), "I am Page Two my brother is PageOne. I have a >ChildPage. (and SomeMissingPage)");
    pageThree = WikiPageUtil.addPage(root, PathParser.parse("PageThree"), "This is !-PageThree-!, I Have \n!include PageTwo");
    pageTwoChild = WikiPageUtil.addPage(pageTwo, PathParser.parse("ChildPage"), "I am Child page, my uncle is .PageOne ");

    hits.clear();
  }

  @Test
  public void testFindReferencingPagesOnSiblingAndChild() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageOne, this);
    whereUsed.search(root);
    assertEquals(2, hits.size());
    assertTrue(hits.contains(pageTwo));
    assertTrue(hits.contains(pageTwoChild));
  }

  @Test
  public void testFindReferencingPagesOnSibling() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageTwo, this);
    whereUsed.search(root);
    assertEquals(1, hits.size());
    assertEquals(pageThree, hits.get(0));
  }

  @Test
  public void testFindReferencingPagesNotReferenced() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, this);
    whereUsed.search(root);
    assertEquals(0, hits.size());
  }

  @Test
  public void testFindReferencingPagesFromChild() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageTwoChild, this);
    whereUsed.search(root);
    assertEquals(2, hits.size());
    assertTrue(hits.contains(pageOne));
    assertTrue(hits.contains(pageTwo));
  }

  @Test
  public void testObserving() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageOne, this);
    whereUsed.search(root);
    assertEquals(2, hits.size());
  }

  @Test
  public void testOnlyOneReferencePerPage() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, this);
    WikiPage newPage = WikiPageUtil.addPage(root, PathParser.parse("NewPage"), "one reference to PageThree.  Two reference to PageThree");
    whereUsed.search(root);
    assertEquals(1, hits.size());
    assertEquals(newPage, hits.get(0));
  }

  @Test
  public void testWordsNotFoundInPreprocessedText() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, this);
    WikiPageUtil.addPage(root, PathParser.parse("NewPage"), "{{{ PageThree }}}");
    whereUsed.search(root);
    assertEquals(0, hits.size());
  }

  @Test
  public void testFindReferencingPagesWithLinksWithAlternateText() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, this);
    WikiPage newPage = WikiPageUtil.addPage(root, PathParser.parse("NewPage"), "I enjoy being a sibling of [[the third page][PageThree]]");
    whereUsed.search(root);
    assertEquals(1, hits.size());
    assertEquals(newPage, hits.get(0));
  }

  @Test
  public void pleaseMindPagesWithSuffixAreNotFound() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, this);
    WikiPage newPage = WikiPageUtil.addPage(root, PathParser.parse("NewPage"), "I enjoy being a sibling of [[the third page][PageThree?edit]]");
    whereUsed.search(root);
    assertEquals(1, hits.size());
    assertEquals(newPage, hits.get(0));
  }

  @Test
  public void testFinderShouldDealWithOtherLinks() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, this);
    WikiPage newPage = WikiPageUtil.addPage(root, PathParser.parse("NewPage"), "I enjoy being a sibling of [[the third page][http://fitnesse.org]]");
    whereUsed.search(root);
    assertEquals(0, hits.size());
  }


}
