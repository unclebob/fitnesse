package fitnesse.util.partitioner;

import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class EqualLengthListPartitionerTest {
  private final EqualLengthListPartitioner<String> partitioner = new EqualLengthListPartitioner<>();

  @Test
  public void partitionEmpty() {
    assertEquals(Collections.emptyList(), getPartition(Collections.emptyList(), 2, 0));
    assertEquals(Collections.emptyList(), getPartition(Collections.emptyList(), 2, 1));
  }

  @Test
  public void partitionExact() {
    List<String> list = asList("a", "b", "c", "d");
    assertEquals(Collections.singletonList("a"), getPartition(list, 4, 0));
    assertEquals(Collections.singletonList("b"), getPartition(list, 4, 1));
    assertEquals(Collections.singletonList("c"), getPartition(list, 4, 2));
    assertEquals(Collections.singletonList("d"), getPartition(list, 4, 3));

    assertEquals(list, getPartition(list, 1, 0));

    assertEquals(asList("a", "b"), getPartition(list, 2, 0));
    assertEquals(asList("c", "d"), getPartition(list, 2, 1));
  }

  @Test
  public void partitionPartial() {
    List<String> list = asList("a", "b", "c", "d");
    assertEquals(asList("a", "b"), getPartition(list, 3, 0));
    assertEquals(Collections.singletonList("c"), getPartition(list, 3, 1));
    assertEquals(Collections.singletonList("d"), getPartition(list, 3, 2));

    List<String> list2 = asList("a", "b", "c", "d", "e");
    assertEquals(asList("a", "b"), getPartition(list2, 3, 0));
    assertEquals(asList("c", "d"), getPartition(list2, 3, 1));
    assertEquals(Collections.singletonList("e"), getPartition(list2, 3, 2));
  }

  private List<String> getPartition(List<String> source, int partitionCount, int index) {
    return partitioner.split(source, partitionCount).get(index);
  }
}
