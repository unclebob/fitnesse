package fitnesse.wiki;

import fitnesse.components.TraversalListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HitCollector implements TraversalListener<WikiPage> {
  private List<WikiPage> hits = new ArrayList<>();

  @Override
  public void process(WikiPage page) {
    hits.add(page);
  }

  public void assertPagesFound(String... pageNames) {
    assertEquals(pageNames.length, hits.size());

    List<String> pageNameList = new ArrayList<>(Arrays.asList(pageNames));
    for (WikiPage page: hits) {
      assertTrue(pageNameList.contains(page.getName()));
      pageNameList.remove(page.getName()); // to correctly assert multiple entries with the same name
    }
  }
}
