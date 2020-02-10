package fitnesse.testrunner.run;

import fitnesse.util.partitioner.EqualLengthListPartitioner;
import fitnesse.wiki.WikiPage;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.function.Function;

public class PartitioningTestRunFactory implements TestRunFactory {
  public static final String PARTITION_COUNT_ARG = "partitionCount";
  public static final String PARTITION_INDEX_ARG = "partitionIndex";

  private Function<List<WikiPage>, ? extends PagesByTestSystem> factory;
  private Function<List<WikiPage>, PageListPartitioner> partitioner;

  public PartitioningTestRunFactory() {
    this(PagesByTestSystem::new,
      pages -> new PageListPartitionerImpl(new EqualLengthListPartitioner<>()));
  }

  public PartitioningTestRunFactory(Function<List<WikiPage>, ? extends PagesByTestSystem> factory,
                                    Function<List<WikiPage>, PageListPartitioner> partitioner) {
    this.factory = factory;
    this.partitioner = partitioner;
  }

  @Override
  public boolean canRun(List<WikiPage> pages) {
    return true;
  }

  public TestRun createRun(List<WikiPage> pages) {
    PagesByTestSystem pagesByTestSystem = getPagesByTestSystem(pages);
    return createRun(pagesByTestSystem);
  }

  public PagePositions findPagePositions(List<WikiPage> pages) {
    if (pages.isEmpty()) {
      return new PagePositions();
    }
    WikiPage page = pages.get(0);
    PageListPartitioner pageListPartitioner = partitioner.apply(pages);
    int partitionCount = getPartitionCount(page);
    return pageListPartitioner.findPagePositions(factory, pages, partitionCount);
  }

  protected PagesByTestSystem getPagesByTestSystem(List<WikiPage> pages) {
    if (pages.isEmpty()) {
      return factory.apply(pages);
    }
    WikiPage page = pages.get(0);
    PageListPartitioner pageListPartitioner = partitioner.apply(pages);
    int partitionCount = getPartitionCount(page);
    if (partitionCount == 1) {
      return factory.apply(pages);
    } else {
      int partitionIndex = getPartitionIndex(page);
      if (partitionIndex >= partitionCount) {
        throw new IllegalArgumentException(PARTITION_COUNT_ARG + " must be larger than " + PARTITION_INDEX_ARG);
      }
      return pageListPartitioner.partition(factory, pages, partitionCount, partitionIndex);
    }
  }

  protected TestRun createRun(PagesByTestSystem pagesByTestSystem) {
    return new PerTestSystemTestRun(pagesByTestSystem);
  }

  protected int getPartitionCount(WikiPage page) {
    int partitionCount = 1;
    String partitionCountStr = page.getVariable(PARTITION_COUNT_ARG);
    if (StringUtils.isNotEmpty(partitionCountStr)) {
      partitionCount = Integer.parseInt(partitionCountStr);
    }
    return partitionCount;
  }

  protected int getPartitionIndex(WikiPage page) {
    int partitionIndex = 0;
    String partitionIndexStr = page.getVariable(PARTITION_INDEX_ARG);
    if (StringUtils.isNotEmpty(partitionIndexStr)) {
      partitionIndex = Integer.parseInt(partitionIndexStr);
    }
    return partitionIndex;
  }

  public Function<List<WikiPage>, ? extends PagesByTestSystem> getFactory() {
    return factory;
  }

  public void setFactory(Function<List<WikiPage>, ? extends PagesByTestSystem> factory) {
    this.factory = factory;
  }

  public Function<List<WikiPage>, PageListPartitioner> getPartitioner() {
    return partitioner;
  }

  public void setPartitioner(Function<List<WikiPage>, PageListPartitioner> partitioner) {
    this.partitioner = partitioner;
  }
}
