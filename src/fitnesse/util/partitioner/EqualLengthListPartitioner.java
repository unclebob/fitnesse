package fitnesse.util.partitioner;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits a list into 'near equal' length sublists.
 * Near equal means the difference in length between any two sublists is at most one.
 * If a split into sublists of exactly the same length is possible this is created.
 */
public class EqualLengthListPartitioner<T> implements ListPartitioner<T> {

  /**
   * Splits a list into 'near equal' length sublists.
   * @param source list to split
   * @param partitionCount number of sublists desired
   * @return list of partitionCount sublists of source
   */
  @Override
  public List<List<T>> split(List<T> source, int partitionCount) {
    int sourceSize = source.size();
    int smallerPartitionSize = sourceSize / partitionCount;
    int largerPartitionSize = smallerPartitionSize + 1;

    int fullPartitions = sourceSize % partitionCount;
    if (fullPartitions == 0) {
      fullPartitions = partitionCount;
      largerPartitionSize = smallerPartitionSize;
    }

    List<List<T>> result = new ArrayList<>(partitionCount);
    int index = 0;
    for (int i = 0; i < partitionCount; i++) {
      int partitionSize = i < fullPartitions ? largerPartitionSize : smallerPartitionSize;
      int end = index + partitionSize;

      List<T> partition = source.subList(index, end);
      result.add(partition);

      index = end;
    }
    return result;
  }
}
