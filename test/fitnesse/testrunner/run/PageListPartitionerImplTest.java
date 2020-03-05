package fitnesse.testrunner.run;

import fitnesse.testrunner.WikiPageIdentity;
import fitnesse.util.partitioner.EqualLengthListPartitioner;
import fitnesse.util.partitioner.ListPartitioner;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PageListPartitionerImplTest extends PageListTestBase {
  private final PageListPartitionerImpl partitioner = new PageListPartitionerImpl(new EqualLengthListPartitioner<>());

  @Test
  public void emptyListIsNoProblem() {
    List<WikiPage> list = Collections.emptyList();

    PagesByTestSystem part0 = partitioner.partition(PagesByTestSystem::new, list, 2, 0);
    assertEquals(0, part0.totalTestsToRun());
    PagesByTestSystem part1 = partitioner.partition(PagesByTestSystem::new, list, 2, 1);
    assertEquals(0, part1.totalTestsToRun());
  }

  @Test
  public void partitions() {
    List<WikiPage> list = createDummyPageList();

    PagesByTestSystem part0 = partitioner.partition(PagesByTestSystem::new, list, 3, 0);
    assertEquals(2, part0.totalTestsToRun());
    assertEquals(list.get(0), part0.getSourcePages().get(0));
    assertEquals(list.get(1), part0.getSourcePages().get(1));

    PagesByTestSystem part1 = partitioner.partition(PagesByTestSystem::new, list, 3, 1);
    assertEquals(1, part1.totalTestsToRun());
    assertEquals(list.get(2), part1.getSourcePages().get(0));

    PagesByTestSystem part2 = partitioner.partition(PagesByTestSystem::new, list, 3, 2);
    assertEquals(1, part2.totalTestsToRun());
    assertEquals(list.get(3), part2.getSourcePages().get(0));
  }
  @Test
  public void findPagePositions() {
    List<WikiPage> list = createDummyPageList();

    PagePositions positions = partitioner.findPagePositions(PagesByTestSystem::new, list, 3);
    assertEquals(asList("Partition", "Test System"), positions.getGroupNames());
    assertEquals(4, positions.getPages().size());

    List<String> pages = positions.getPages();
    for (int i = 0; i < list.size(); i++) {
      String pagename = pages.get(i);
      assertEquals(list.get(i).getFullPath().toString(), pagename);
      List<PagePosition> pagePosList = positions.getPositions(pagename);
      assertEquals(1, pagePosList.size());
      PagePosition pagePosition = pagePosList.get(0);
      assertEquals(2, pagePosition.getGroup().size());
      assertTrue(pagePosition.getGroup().get(1) instanceof WikiPageIdentity);
    }
    assertEquals(0, getPartitionFor(positions, pages.get(0)));
    assertEquals(0, getPartitionFor(positions, pages.get(1)));
    assertEquals(1, getPartitionFor(positions, pages.get(2)));
    assertEquals(2, getPartitionFor(positions, pages.get(3)));
  }

  @Test
  public void testCustomPartitioner() {
    List<WikiPage> list = createDummyPageList();
    Map<WikiPage, Integer> positionInput = new HashMap<>();
    positionInput.put(list.get(0), 2);
    positionInput.put(list.get(1), 1);
    positionInput.put(list.get(2), 0);
    positionInput.put(list.get(3), 2);

    ListPartitioner<WikiPage> customFunction = (l, i) -> {
      List<List<WikiPage>> result = new ArrayList<>(i);
      for (int j = 0; j < i; j++) {
        result.add(new ArrayList<>());
      }
      for (WikiPage page : l) {
        Integer pos = positionInput.get(page);
        result.get(pos).add(page);
      }
      return result;
    };
    PageListPartitioner customPart = new PageListPartitionerImpl(customFunction);
    PagePositions positions = customPart.findPagePositions(PagesByTestSystem::new, list, 3);
    assertEquals(4, positions.getPages().size());

    List<String> pages = positions.getPages();
    assertEquals(2, getPartitionFor(positions, pages.get(0)));
    assertEquals(1, getPartitionFor(positions, pages.get(1)));
    assertEquals(0, getPartitionFor(positions, pages.get(2)));
    assertEquals(2, getPartitionFor(positions, pages.get(3)));

  }

  private Object getPartitionFor(PagePositions posList, String pagename) {
    return posList.getPositions(pagename).get(0).getGroup().get(0);
  }

  private List<WikiPage> createDummyPageList() {
    WikiPageDummy parent = new WikiPageDummy("parent", "p", null);
    return asList(
      new WikiPageDummy("A", "B", parent),
      new WikiPageDummy("a", "b", parent),
      new WikiPageDummy("C", "D", parent),
      new WikiPageDummy("c", "d", parent));
  }
}
