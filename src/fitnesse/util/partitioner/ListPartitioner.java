package fitnesse.util.partitioner;

import java.util.List;

/**
 * Splits a list into a number of partitions (i.e. sub-lists).
 */
public interface ListPartitioner<T> {
  /**
   * Splits source.
   * @param source list to split.
   * @param partitionCount number of partitions to create.
   * @return list of partitionCount lists each containing zero or more of source's elements.
   */
  List<List<T>> split(List<T> source, int partitionCount);
}
