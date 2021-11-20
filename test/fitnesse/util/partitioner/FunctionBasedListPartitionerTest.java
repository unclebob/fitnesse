package fitnesse.util.partitioner;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.TestRoot;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class FunctionBasedListPartitionerTest {
  private Map<String, Integer> positions = new LinkedHashMap<>();
  private List<List<String>> notFoundArgs = new ArrayList<>();

  @Before
  public void setUp() {
    positions.put("a", 2);
    positions.put("b", 0);
    positions.put("c", 3);
    positions.put("0", 1);
    positions.put("e", 4);
    positions.put("6", 5);
  }

  @Test
  public void testAllPlaced() {
    List<String> list = new ArrayList<>(positions.keySet());
    Function<String, Optional<Integer>> f = s -> Optional.ofNullable(positions.get(s));

    FunctionBasedListPartitioner<String> partitioner =
      new FunctionBasedListPartitioner<>(f, (parts, nf) -> {
        notFoundArgs.add(nf);
        return new ArrayList<>(parts.size());
      });

    List<List<String>> parts = partitioner.split(list, 6);
    assertEquals(6, parts.size());

    for (List<String> part : parts) {
      assertEquals(1, part.size());
    }
    assertEquals("b", parts.get(0).get(0));
    assertEquals("0", parts.get(1).get(0));
    assertEquals("a", parts.get(2).get(0));
    assertEquals("c", parts.get(3).get(0));
    assertEquals("e", parts.get(4).get(0));
    assertEquals("6", parts.get(5).get(0));

    assertEquals(Collections.emptyList(), notFoundArgs);
  }

  @Test
  public void testNotAllPlaced() {
    List<String> list = new ArrayList<>(positions.keySet());
    list.add(1, "nf1");
    list.add(3, "nf2");
    list.add("nf3");
    list.add("nf4");
    list.add("nf5");
    list.add("nf6");
    list.add("nf7");
    Function<String, Optional<Integer>> f = s -> Optional.ofNullable(positions.get(s));

    FunctionBasedListPartitioner<String> partitioner =
      new FunctionBasedListPartitioner<>(f, (parts, nf) -> {
        notFoundArgs.add(nf);
        return new EqualLengthListPartitioner<String>().split(nf, parts.size());
      });

    List<List<String>> parts = partitioner.split(list, 6);
    assertEquals(Collections.singletonList(asList("nf1", "nf2", "nf3", "nf4", "nf5", "nf6", "nf7")), notFoundArgs);
    assertEquals(
      asList(
        asList("nf1", "nf2", "b"),
        asList("nf3", "0"),
        asList("nf4", "a"),
        asList("nf5", "c"),
        asList("nf6", "e"),
        asList("nf7", "6")),
      parts);
  }

  @Test
  public void testWithEmptyPartitionFile() {

    LinkedHashMap<String, Integer> positions = new LinkedHashMap<>();
    //Perform the split
    Function<WikiPage, Optional<Integer>> f = s -> Optional.ofNullable(positions.get(s.getFullPath().toString()));
    List<List<WikiPage>> noFoundArgs = new ArrayList<>();

    FunctionBasedListPartitioner<WikiPage> partitioner =
      new FunctionBasedListPartitioner<>(f, (parts, nf) -> {
        noFoundArgs.add(nf);
        return new EqualLengthListPartitioner<WikiPage>().split(nf, parts.size());
      });

    List<List<WikiPage>> parts = partitioner.split(constructTreeAndReturnAllTests(), 2);
    assertEquals(2, parts.get(0).size());
    assertEquals(2, parts.get(1).size());

    List<String> actualPartition1TestCases = new ArrayList<>();
    parts.get(0).forEach(e -> actualPartition1TestCases.add(e.getName()));
    assertEquals(asList("Suite1Test1", "Suite1Test2"), actualPartition1TestCases);

    List<String> actualPartition2TestCases = new ArrayList<>();
    parts.get(1).forEach(e -> actualPartition2TestCases.add(e.getName()));
    assertEquals(asList("Suite1Test3", "Suite2Test1"), actualPartition2TestCases);
  }

  @Test
  public void testWithSuiteInPartitionFile() {
    //Define the partition file
    LinkedHashMap<String, Integer> positions = new LinkedHashMap<>();
    positions.put("Suite1", 0);
    positions.put("Suite2", 1);

    Function<WikiPage, Optional<Integer>> f = s -> Optional.ofNullable(positions.get(s.getFullPath().toString()));
    List<List<WikiPage>> noFoundArgs = new ArrayList<>();

    FunctionBasedListPartitioner<WikiPage> partitioner =
      new FunctionBasedListPartitioner<>(f, (parts, nf) -> {
        noFoundArgs.add(nf);
        return new ArrayList<>(parts.size());
      });

    List<List<WikiPage>> parts = partitioner.split(constructTreeAndReturnAllTests(), 2);

    //Assertions
    assertEquals(3, parts.get(0).size());
    assertEquals(1, parts.get(1).size());

    List<String> actualPartition1TestCases = new ArrayList<>();
    parts.get(0).forEach(e -> actualPartition1TestCases.add(e.getName()));
    assertEquals(asList("Suite1Test1", "Suite1Test2", "Suite1Test3"), actualPartition1TestCases);

    List<String> actualPartition2TestCases = new ArrayList<>();
    parts.get(1).forEach(e -> actualPartition2TestCases.add(e.getName()));
    assertEquals(asList("Suite2Test1"), actualPartition2TestCases);
  }

  //Construct tree with 3 tests in first suite and 1 test in second suite
  private List<WikiPage> constructTreeAndReturnAllTests() {
    TestRoot root = new TestRoot();
    WikiPage suite1 = root.makePage("Suite1", "!contents -R1 -g -p -f -h");
    setPageProperties(suite1, "Suite");
    WikiPage suite1Test1 = setPageProperties(suite1.addChildPage("Suite1Test1"), "Test");
    WikiPage suite1Test2 = setPageProperties(suite1.addChildPage("Suite1Test2"), "Test");
    WikiPage suite1Test3 = setPageProperties(suite1.addChildPage("Suite1Test3"), "Test");
    WikiPage suite2 = root.makePage("Suite2", "!contents -R1 -g -p -f -h");
    setPageProperties(suite2, "Suite");
    WikiPage suite2Test1 = setPageProperties(suite2.addChildPage("Suite2Test1"), "Test");

    List<WikiPage> list = new ArrayList<>();
    list.add(0, suite1Test1);
    list.add(1, suite1Test2);
    list.add(2, suite1Test3);
    list.add(3, suite2Test1);
    return list;
  }

  private WikiPage setPageProperties(WikiPage wikiPage, String properties) {
    PageData pageData = wikiPage.getData();
    pageData.setAttribute(properties);
    wikiPage.commit(pageData);
    return wikiPage;
  }

}
