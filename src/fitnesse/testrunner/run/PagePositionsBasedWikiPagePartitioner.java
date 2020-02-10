package fitnesse.testrunner.run;

import fitnesse.util.partitioner.EqualLengthListPartitioner;
import fitnesse.util.partitioner.ListPartitioner;
import fitnesse.util.partitioner.MapBasedListPartitioner;
import fitnesse.wiki.WikiPage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Partitions pages based on provided PagePositions.
 */
public class PagePositionsBasedWikiPagePartitioner implements ListPartitioner<WikiPage> {
  private ListPartitioner<WikiPage> partitioner;
  private Comparator<WikiPage> pageComparator;

  public PagePositionsBasedWikiPagePartitioner(PagePositions pagePositions) {
    if (pagePositions != null) {
      setPagePositions(pagePositions);
    }
  }

  public void setPagePositions(PagePositions pagePositions) {
    pageComparator = createComparator(pagePositions);
    partitioner = createPartitioner(pagePositions);
  }

  @Override
  public List<List<WikiPage>> split(List<WikiPage> source, int partitionCount) {
    List<WikiPage> pages = source;
    if (pageComparator != null) {
      // by ordering the input according to the positions within each partition BEFORE partitioning the
      // resulting partitions will have the ordering prescribed by the page positions
      pages = new ArrayList<>(source);
      pages.sort(pageComparator);
    }
    return partitioner.split(pages, partitionCount);
  }

  protected Comparator<WikiPage> createComparator(PagePositions pagePositions) {
    Comparator<String> comp = pagePositions.createByPositionInGroupComparator();
    return (page1, page2) -> {
      String path1 = page1.getFullPath().toString();
      String path2 = page2.getFullPath().toString();
      return comp.compare(path1, path2);
    };
  }

  protected ListPartitioner<WikiPage> createPartitioner(PagePositions pagePositions) {
    Map<String, Integer> positionMap = loadPartitionFile(pagePositions);
    return createPartitioner(positionMap);
  }

  protected ListPartitioner<WikiPage> createPartitioner(Map<String, Integer> positionMap) {
    return new MapBasedListPartitioner<>(wp -> wp.getFullPath().toString(), positionMap, this::handleUnknownPages);
  }

  protected List<List<WikiPage>> handleUnknownPages(
    List<List<WikiPage>> partitionsFromFile,
    List<WikiPage> pagesNotPresent) {
    return new EqualLengthListPartitioner<WikiPage>().split(pagesNotPresent, partitionsFromFile.size());
  }

  protected Map<String, Integer> loadPartitionFile(PagePositions pagePositions) {
    int part = pagePositions.getGroupIndex(PageListPartitionerImpl.PARTITION_HEADER);
    if (part < 0) {
      throw new IllegalArgumentException("No " + PageListPartitionerImpl.PARTITION_HEADER + " column found");
    }
    return extractPartitionMap(pagePositions, part);
  }

  protected Map<String, Integer> extractPartitionMap(PagePositions pagePositions, int partGroupIndex) {
    Map<String, Integer> indices = new HashMap<>();
    for (String page : pagePositions.getPages()) {
      List<PagePosition> positions = pagePositions.getPositions(page);
      for (PagePosition position : positions) {
        Integer partValue = position.getGroupIntValue(partGroupIndex);
        indices.put(page, partValue);
      }
    }
    return indices;
  }
}
