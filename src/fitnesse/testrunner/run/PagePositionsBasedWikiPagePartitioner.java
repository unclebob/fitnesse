package fitnesse.testrunner.run;

import fitnesse.wiki.WikiPage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Partitions pages based on provided PagePositions.
 */
public class PagePositionsBasedWikiPagePartitioner extends PositionMapBasedWikiPagePartitioner {
  private Comparator<WikiPage> pageComparator;

  public PagePositionsBasedWikiPagePartitioner(PagePositions pagePositions) {
    if (pagePositions != null) {
      setPagePositions(pagePositions);
    }
  }

  public void setPagePositions(PagePositions pagePositions) {
    Map<String, Integer> partitionMap = loadPartitionFile(pagePositions);
    setPartitionMap(partitionMap);
    setPageComparator(createComparator(pagePositions));
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
    return super.split(pages, partitionCount);
  }

  protected Comparator<WikiPage> createComparator(PagePositions pagePositions) {
    Comparator<String> comp = pagePositions.createByPositionInGroupComparator();
    return Comparator.comparing(this::getFullPath, comp);
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

  public Comparator<WikiPage> getPageComparator() {
    return pageComparator;
  }

  public void setPageComparator(Comparator<WikiPage> pageComparator) {
    this.pageComparator = pageComparator;
  }
}
