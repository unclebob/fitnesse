package fitnesse.wiki.search;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Before;
import org.junit.Test;


public class WhereUsedPageFinderTest implements TraversalListener<WikiPage> {
  private WikiPage root;
  private WikiPage pageOne;
  private WikiPage pageTwo;
  private WikiPage pageThree;
  private WikiPage pageTwoChild;

  private List<WikiPage> hits = new ArrayList<WikiPage>();

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
    List<WikiPage> resultList = whereUsed.search(root);
    assertEquals(2, resultList.size());
    assertEquals(pageTwo, resultList.get(0));
    assertEquals(pageTwoChild, resultList.get(1));
  }

  @Test
  public void testFindReferencingPagesOnSibling() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageTwo, this);
    List<WikiPage> resultList = whereUsed.search(root);
    assertEquals(1, resultList.size());
    assertEquals(pageThree, resultList.get(0));
  }

  @Test
  public void testFindReferencingPagesNotReferenced() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, this);
    List<WikiPage> resultList = whereUsed.search(root);
    assertEquals(0, resultList.size());
  }

  @Test
  public void testFindReferencingPagesFromChild() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageTwoChild, this);
    List<WikiPage> resultList = whereUsed.search(root);
    assertEquals(2, resultList.size());
    assertEquals(pageOne, resultList.get(0));
    assertEquals(pageTwo, resultList.get(1));
  }

  @Test
  public void testObserving() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageOne, this);
    List<WikiPage> resultList = whereUsed.search(root);
    assertEquals(2, resultList.size());
  }

  @Test
  public void testOnlyOneReferencePerPage() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, this);
    WikiPage newPage = WikiPageUtil.addPage(root, PathParser.parse("NewPage"), "one reference to PageThree.  Two reference to PageThree");
    List<WikiPage> resultList = whereUsed.search(root);
    assertEquals(1, resultList.size());
    assertEquals(newPage, resultList.get(0));
  }

  @Test
  public void testWordsNotFoundInPreprocessedText() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, this);
    WikiPageUtil.addPage(root, PathParser.parse("NewPage"), "{{{ PageThree }}}");
    List<WikiPage> resultList = whereUsed.search(root);
    assertEquals(0, resultList.size());
  }

  @Test
  public void testFindReferencingPagesWithLinksWithAlternateText() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, this);
    WikiPage newPage = WikiPageUtil.addPage(root, PathParser.parse("NewPage"), "I enjoy being a sibling of [[the third page][PageThree]]");
    List<WikiPage> resultList = whereUsed.search(root);
    assertEquals(1, resultList.size());
    assertEquals(newPage, resultList.get(0));
  }

  @Test
  public void pleaseMindPagesWithSuffixAreNotFound() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, this);
    WikiPage newPage = WikiPageUtil.addPage(root, PathParser.parse("NewPage"), "I enjoy being a sibling of [[the third page][PageThree?edit]]");
    List<WikiPage> resultList = whereUsed.search(root);
    assertEquals(1, resultList.size());
    assertEquals(newPage, resultList.get(0));
  }

  @Test
  public void testFinderShouldDealWithOtherLinks() throws Exception {
    WhereUsedPageFinder whereUsed = new WhereUsedPageFinder(pageThree, this);
    WikiPage newPage = WikiPageUtil.addPage(root, PathParser.parse("NewPage"), "I enjoy being a sibling of [[the third page][http://fitnesse.org]]");
    List<WikiPage> resultList = whereUsed.search(root);
    assertEquals(0, resultList.size());
  }


}
