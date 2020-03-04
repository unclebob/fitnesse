package fitnesse.testrunner.run;

import fitnesse.testrunner.WikiPageIdentity;
import fitnesse.util.partitioner.ListPartitioner;
import fitnesse.wiki.WikiPage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

/**
 * Splits the lists of pages into partitions, using the same ordering as the PagesByTestSystem will.
 */
public class PageListPartitionerImpl implements PageListPartitioner {
  public static final String PARTITION_HEADER = "Partition";
  private ListPartitioner<WikiPage> partitionFunction;

  /**
   * Creates new, but does not yet initialize the partition function.
   * DO NOT forget to call setPartitionFunction() before actually using it!
   */
  public PageListPartitionerImpl() {
  }

  public PageListPartitionerImpl(ListPartitioner<WikiPage> partitionFunction) {
    this.partitionFunction = partitionFunction;
  }

  @Override
  public PagesByTestSystem partition(Function<List<WikiPage>, ? extends PagesByTestSystem> factory, List<WikiPage> pages, int partitionCount, int partitionIndex) {
    List<List<WikiPage>> partitionedTests = getPartitionsWithOnlyTests(partitionCount, factory.apply(pages));
    List<WikiPage> selectedPartition = partitionedTests.get(partitionIndex);
    return factory.apply(selectedPartition);
  }

  @Override
  public PagePositions findPagePositions(
    Function<List<WikiPage>, ? extends PagesByTestSystem> factory,
    List<WikiPage> pages,
    int partitionCount) {
    List<List<WikiPage>> partitionedTests;
    if (partitionCount > 1) {
      partitionedTests = getPartitionsWithOnlyTests(partitionCount, factory.apply(pages));
    } else {
      partitionedTests = Collections.singletonList(pages);
    }
    List<List<WikiPage>> partitions = getPartitionsIncludingSetUpAndTearDown(factory, partitionedTests);
    return getIndicesPerPage(pages, partitions);
  }

  protected List<List<WikiPage>> getPartitionsWithOnlyTests(int partitionCount, PagesByTestSystem pagesByTestSystem) {
    List<WikiPage> orderedPages = getOrderedTestWikiPages(pagesByTestSystem);
    return partitionFunction.split(orderedPages, partitionCount);
  }

  protected List<WikiPage> getOrderedTestWikiPages(PagesByTestSystem pagesByTestSystem) {
    List<WikiPage> orderedPages = orderPagesByTestSystem(pagesByTestSystem);
    orderedPages.removeIf(WikiPage::isSuiteSetupOrTearDown);
    return orderedPages;
  }

  protected List<List<WikiPage>> getPartitionsIncludingSetUpAndTearDown(
    Function<List<WikiPage>, ? extends PagesByTestSystem> factory,
    List<List<WikiPage>> partitionedTests) {

    return partitionedTests
      .stream()
      .map(factory)
      .map(this::orderPagesByTestSystem)
      .collect(Collectors.toList());
  }

  protected PagePositions getIndicesPerPage(List<WikiPage> allPages, List<List<WikiPage>> partitions) {
    PagePositions allPositions = createPagePositions(partitions);
    return orderResults(allPages, allPositions);
  }

  protected PagePositions createPagePositions(List<List<WikiPage>> partitions) {
    PagePositions result = new PagePositions();
    result.getGroupNames().addAll(asList(PARTITION_HEADER, "Test System"));

    for (int partitionIndex = 0; partitionIndex < partitions.size(); partitionIndex++) {
      List<WikiPage> partition = partitions.get(partitionIndex);
      for (int indexInPartition = 0; indexInPartition < partition.size(); indexInPartition++) {
        WikiPage page = partition.get(indexInPartition);
        WikiPageIdentity identity = new WikiPageIdentity(page);
        result.addPosition(page.getFullPath().toString(), asList(partitionIndex, identity), indexInPartition);
      }
    }
    return result;
  }

  protected PagePositions orderResults(List<WikiPage> inputPages, PagePositions allPositions) {
    PagePositions result = copySelectedKeys(inputPages, allPositions);

    for (String pageName : allPositions.getPages()) {
      if (!result.hasPositions(pageName)) {
        // page not present in input, but we did calculate one (or more) positions
        // (probably SuiteSetUp and -TearDown pages) these are added to result as well
        result.getPositions(pageName).addAll(allPositions.getPositions(pageName));
      }
    }
    return result;
  }

  /**
   * Creates page positions for keys to keep, where they are listed in original order
   * @param keysToKeep defines positions for which pages are needed and in which order they should be listed
   * @param allPositions all known page positions
   * @return subset of allPositions where pages are filtered and ordered according to keysToKeep
   */
  protected PagePositions copySelectedKeys(List<WikiPage> keysToKeep, PagePositions allPositions) {
    PagePositions results = new PagePositions();
    results.getGroupNames().addAll(allPositions.getGroupNames());
    for (WikiPage page : keysToKeep) {
      String pageName = page.getFullPath().toString();
      results.getPositions(pageName).addAll(allPositions.getPositions(pageName));
    }
    return results;
  }

  protected List<WikiPage> orderPagesByTestSystem(PagesByTestSystem pagesByTestSystem) {
    List<WikiPage> orderedPages = new ArrayList<>(pagesByTestSystem.getSourcePages().size());
    for (WikiPageIdentity identity : pagesByTestSystem.identities()) {
      List<WikiPage> pages = pagesByTestSystem.wikiPagesForIdentity(identity);
      orderedPages.addAll(pages);
    }
    return orderedPages;
  }

  public ListPartitioner<WikiPage> getPartitionFunction() {
    return partitionFunction;
  }

  public void setPartitionFunction(ListPartitioner<WikiPage> partitionFunction) {
    this.partitionFunction = partitionFunction;
  }
}
