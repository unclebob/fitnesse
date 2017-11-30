package fitnesse.wiki.search;

import fitnesse.components.TraversalListener;
import fitnesse.wiki.WikiPage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

class HitCollector implements TraversalListener<WikiPage> {
  private List<WikiPage> hits = new ArrayList<>();

  @Override
  public void process(WikiPage page) {
    hits.add(page);
  }

  public void assertPagesFound(String... pageNames) throws Exception {
    assertEquals(pageNames.length, hits.size());

    List<String> pageNameList = Arrays.asList(pageNames);
    for (WikiPage page: hits) {
      assertTrue(pageNameList.contains(page.getName()));
    }
  }
}
