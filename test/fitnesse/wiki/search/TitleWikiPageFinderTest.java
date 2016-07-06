package fitnesse.wiki.search;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.*;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TitleWikiPageFinderTest implements TraversalListener<WikiPage> {
  WikiPage root;

  private List<WikiPage> hits = new ArrayList<>();

  @Override
  public void process(WikiPage page) {
    hits.add(page);
  }

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "has PageOne content");
    WikiPageUtil.addPage(root, PathParser.parse("PageOne.PageOneChild"), "PageChild is a child of PageOne");
    WikiPage pageTwo = WikiPageUtil.addPage(root, PathParser.parse("PageTwo"), "PageTwo has a bit of content too\n^PageOneChild");
    PageData data = pageTwo.getData();
    pageTwo.commit(data);
    hits.clear();
  }

  @Test
  public void titleSearch() throws Exception {
    TitleWikiPageFinder searcher = new TitleWikiPageFinder("one", this);
    hits.clear();
    searcher.search(root);
    assertPagesFound("PageOne", "PageOneChild");
  }

  private void assertPagesFound(String... pageNames) throws Exception {
    assertEquals(pageNames.length, hits.size());

    List<String> pageNameList = Arrays.asList(pageNames);
    for (WikiPage page: hits) {
      assertTrue(pageNameList.contains(page.getName()));
    }
  }

}
