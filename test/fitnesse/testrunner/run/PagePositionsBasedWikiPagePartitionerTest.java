package fitnesse.testrunner.run;

import fitnesse.wiki.WikiPage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class PagePositionsBasedWikiPagePartitionerTest extends PageListTestBase {

  @Test
  public void testComparatorKnownPages() {
    addChildPage(suite, "Test2");
    addChildPage(suite, "Test3");
    List<WikiPage> pageList = makeTestPageList();

    int listSize = pageList.size();
    int max = listSize;
    PagePositions pagePositions = new PagePositions();
    for (WikiPage page : pageList) {
      String path = page.getFullPath().toString();
      pagePositions.getPositions(path).add(new PagePosition(--max,"slim"));
    }

    List<WikiPage> copiedList = new ArrayList<>(pageList);
    copiedList.sort(new PagePositionsBasedWikiPagePartitioner(null).createComparator(pagePositions));

    for (int i = 0; i < listSize; i++) {
      assertEquals("Element: " + i, pageList.get(i), copiedList.get(listSize - 1 - i));
    }
  }
}
