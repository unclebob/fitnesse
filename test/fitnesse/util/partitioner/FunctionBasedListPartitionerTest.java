package fitnesse.util.partitioner;

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
}
