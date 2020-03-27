package fitnesse.testrunner.run;

import fitnesse.wiki.WikiPage;

import java.util.List;
import java.util.function.Function;

/**
 * Split a test run into a number of partitions, test runs or suites.
 * All partitions combined contain all tests of the full test run.
 * Splitting a run like this allows a suite to be cut into a number of smaller separate suites,
 * that could for instance be run in parallel to reduce test time.
 */
public interface PageListPartitioner {

  /**
   * Creates requested partition.
   *
   * @param factory function to create PagesByTestSystem
   * @param pages pages to be executed.
   * @param partitionCount number of partitions that will be created will be larger than 1.
   * @param partitionIndex partition to return.
   * @return pages to include in run.
   */
  PagesByTestSystem partition(Function<List<WikiPage>, ? extends PagesByTestSystem> factory, List<WikiPage> pages, int partitionCount, int partitionIndex);

  /**
   * List indices per page (including suite set ups and tear downs).
   * SuiteSetUp or SuiteTearDown can appear in multiple partitions, therefore a list of indices is returned.
   *
   * @param factory function to create PagesByTestSystem
   * @param pages pages to be executed.
   * @param partitionCount number of partitions to create
   * @return page to partition and index within that partition, for each partition to include page in.
   */
  PagePositions findPagePositions(Function<List<WikiPage>, ? extends PagesByTestSystem> factory, List<WikiPage> pages, int partitionCount);
}
