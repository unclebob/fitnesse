package fitnesse.testrunner.run;

import fitnesse.util.partitioner.EqualLengthListPartitioner;
import fitnesse.util.partitioner.ListPartitioner;
import fitnesse.util.partitioner.MapBasedListPartitioner;
import fitnesse.wiki.WikiPage;

import java.util.List;
import java.util.Map;

/**
 * Partitions pages based on provided map with partition indices.
 */
public class PositionMapBasedWikiPagePartitioner implements ListPartitioner<WikiPage> {
  private Map<String, Integer> partitionMap;

  @Override
  public List<List<WikiPage>> split(List<WikiPage> source, int partitionCount) {
    return createPartitioner(partitionMap).split(source, partitionCount);
  }

  protected String getFullPath(WikiPage wikiPage) {
    return wikiPage.getFullPath().toString();
  }

  protected List<List<WikiPage>> handleUnknownPages(
    List<List<WikiPage>> partitionsFromFile,
    List<WikiPage> pagesNotPresent) {
    return new EqualLengthListPartitioner<WikiPage>().split(pagesNotPresent, partitionsFromFile.size());
  }

  protected ListPartitioner<WikiPage> createPartitioner(Map<String, Integer> partitionMap) {
    return new MapBasedListPartitioner<>(this::getFullPath, partitionMap, this::handleUnknownPages);
  }

  public void setPartitionMap(Map<String, Integer> partitionMap) {
    this.partitionMap = partitionMap;
  }

  public Map<String, Integer> getPartitionMap() {
    return partitionMap;
  }
}
