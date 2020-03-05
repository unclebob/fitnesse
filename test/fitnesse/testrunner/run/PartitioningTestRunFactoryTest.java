package fitnesse.testrunner.run;

import fitnesse.testrunner.WikiPageIdentity;
import fitnesse.util.partitioner.EqualLengthListPartitioner;
import fitnesse.wiki.WikiPage;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static fitnesse.wiki.PageData.SUITE_SETUP_NAME;
import static fitnesse.wiki.PageData.SUITE_TEARDOWN_NAME;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PartitioningTestRunFactoryTest extends PageListTestBase {
  private List<List<WikiPage>> pagesProvidedToConstr = new ArrayList<>();
  private List<List<WikiPage>> pagesProvidedToPartitioner = new ArrayList<>();
  private Function<List<WikiPage>, ? extends PagesByTestSystem> constr = pages -> {
    pagesProvidedToConstr.add(pages);
    return new PagesByTestSystem(pages);
  };
  private Function<List<WikiPage>, PageListPartitioner> partitioner = pages -> {
    pagesProvidedToPartitioner.add(pages);
    return new PageListPartitionerImpl(new EqualLengthListPartitioner<>());
  };
  private PartitioningTestRunFactory partitioningTestRunFactory = new PartitioningTestRunFactory(constr, partitioner);

  @Test
  public void testCanDealWithEmptyList() {
    assertTrue(partitioningTestRunFactory.canRun(emptyList()));

    assertNotNull(partitioningTestRunFactory.createRun(emptyList()));

    assertEquals(singletonList(emptyList()), pagesProvidedToConstr);
    assertEquals(emptyList(), pagesProvidedToPartitioner);

    assertEquals(emptyList(), partitioningTestRunFactory.findPagePositions(emptyList()).getPages());
  }

  @Test
  public void testNoPartitionCountPages() {
    List<WikiPage> pageList = addTestPages();
    PagesByTestSystem pages = partitioningTestRunFactory.getPagesByTestSystem(pageList);

    List<String> paths = getPagePaths(pages.getSourcePages());
    assertEquals(
      "[SuitePageName.SlimPage1Suite.SlimPage1Test, " +
        "SuitePageName.SlimPage2Suite.SlimPage2Test, " +
        "SuitePageName.SlimPage3Suite.SlimPage3Test, SuitePageName.TestPage]",
      paths.toString());

    assertEquals(singletonList(pageList), pagesProvidedToConstr);
    assertEquals(singletonList(pageList), pagesProvidedToPartitioner);
  }

  @Test
  public void testPartitionedPages0() {
    customProperties.put(PartitioningTestRunFactory.PARTITION_COUNT_ARG, "2");
    customProperties.put(PartitioningTestRunFactory.PARTITION_INDEX_ARG, "0");
    List<WikiPage> pageList = addTestPages();
    PagesByTestSystem pages = partitioningTestRunFactory.getPagesByTestSystem(pageList);

    List<String> paths = getPagePaths(pages.getSourcePages());
    assertEquals(
      "[SuitePageName.SlimPage1Suite.SlimPage1Test, SuitePageName.SlimPage2Suite.SlimPage2Test]",
      paths.toString());

    assertEquals(asList(pageList, pageList.subList(0, 2)), pagesProvidedToConstr);
    assertEquals(singletonList(pageList), pagesProvidedToPartitioner);
  }

  @Test
  public void testPartitionedPages1() {
    customProperties.put(PartitioningTestRunFactory.PARTITION_COUNT_ARG, "2");
    customProperties.put(PartitioningTestRunFactory.PARTITION_INDEX_ARG, "1");
    List<WikiPage> pageList = addTestPages();
    PagesByTestSystem pages = partitioningTestRunFactory.getPagesByTestSystem(pageList);

    List<String> paths = getPagePaths(pages.getSourcePages());
    assertEquals(
      "[SuitePageName.SlimPage3Suite.SlimPage3Test, SuitePageName.TestPage]",
      paths.toString());

    assertEquals(asList(pageList, pageList.subList(2, 4)), pagesProvidedToConstr);
    assertEquals(singletonList(pageList), pagesProvidedToPartitioner);
  }

  @Test
  public void testPartitionedPages2() {
    customProperties.put(PartitioningTestRunFactory.PARTITION_COUNT_ARG, "3");
    customProperties.put(PartitioningTestRunFactory.PARTITION_INDEX_ARG, "1");
    List<WikiPage> pageList = addTestPages();
    PagesByTestSystem pages = partitioningTestRunFactory.getPagesByTestSystem(pageList);

    List<String> paths = getPagePaths(pages.getSourcePages());
    assertEquals(
      "[SuitePageName.SlimPage3Suite.SlimPage3Test]",
      paths.toString());

    assertEquals(asList(pageList, pageList.subList(2, 3)), pagesProvidedToConstr);
    assertEquals(singletonList(pageList), pagesProvidedToPartitioner);
  }

  @Test
  public void testPartitionedPages4() {
    customProperties.put(PartitioningTestRunFactory.PARTITION_COUNT_ARG, "3");
    customProperties.put(PartitioningTestRunFactory.PARTITION_INDEX_ARG, "2");
    List<WikiPage> pageList = addTestPages();
    PagesByTestSystem pages = partitioningTestRunFactory.getPagesByTestSystem(pageList);

    List<String> paths = getPagePaths(pages.getSourcePages());
    assertEquals(
      "[SuitePageName.TestPage]",
      paths.toString());

    assertEquals(asList(pageList, pageList.subList(3, 4)), pagesProvidedToConstr);
    assertEquals(singletonList(pageList), pagesProvidedToPartitioner);
  }

  @Test
  public void testNoPartitionFindPages() {
    List<WikiPage> pageList = addTestPages();
    PagePositions pagePositions = partitioningTestRunFactory.findPagePositions(pageList);
    List<String> pages = pagePositions.getPages();
    assertEquals(8, pages.size());
    assertEquals("[SuitePageName.SlimPage1Suite.SlimPage1Test, " +
        "SuitePageName.SlimPage2Suite.SlimPage2Test, " +
        "SuitePageName.SlimPage3Suite.SlimPage3Test, " +
        "SuitePageName.TestPage, SuiteSetUp, " +
        "SuitePageName.SlimPage1Suite.SuiteTearDown, " +
        "SuitePageName.SlimPage2Suite.SuiteSetUp, " +
        "SuiteTearDown]",
      pages.toString());

    assertEquals(singletonList(pageList), pagesProvidedToConstr);
    assertEquals(singletonList(pageList), pagesProvidedToPartitioner);
  }

  @Test
  public void testPartitionedFindPages() {
    customProperties.put(PartitioningTestRunFactory.PARTITION_COUNT_ARG, "2");
    List<WikiPage> pageList = addTestPages();
    PagePositions pagePositions = partitioningTestRunFactory.findPagePositions(pageList);
    List<String> pages = pagePositions.getPages();
    assertEquals(8, pages.size());

    assertEquals("[SuitePageName.SlimPage1Suite.SlimPage1Test, " +
        "SuitePageName.SlimPage2Suite.SlimPage2Test, " +
        "SuitePageName.SlimPage3Suite.SlimPage3Test, " +
        "SuitePageName.TestPage, " +
        "SuiteSetUp, " +
        "SuitePageName.SlimPage1Suite.SuiteTearDown, " +
        "SuitePageName.SlimPage2Suite.SuiteSetUp, " +
        "SuiteTearDown]",
      pages.toString());

    assertEquals(asList(pageList, pageList.subList(0, 2), pageList.subList(2, 4)), pagesProvidedToConstr);
    assertEquals(singletonList(pageList), pagesProvidedToPartitioner);

    String lastTearDown = pages.get(5);
    assertEquals("SuitePageName.SlimPage1Suite.SuiteTearDown", lastTearDown);
    List<PagePosition> lastTPos = pagePositions.getPositions(lastTearDown);
    assertEquals(1, lastTPos.size());
    PagePosition tdPagePosition = lastTPos.get(0);
    assertEquals(2, tdPagePosition.getPositionInGroup());
    assertEquals(2, tdPagePosition.getGroup().size());
    assertEquals(0, tdPagePosition.getGroup().get(0));
    assertEquals(WikiPageIdentity.class, tdPagePosition.getGroup().get(1).getClass());

    String lastSetUp = pages.get(4);
    assertEquals("SuiteSetUp", lastSetUp);
    List<PagePosition> lastPos = pagePositions.getPositions(lastSetUp);
    assertEquals(2, lastPos.size());
    PagePosition setupPagePosition1 = lastPos.get(0);
    assertEquals(0, setupPagePosition1.getPositionInGroup());
    assertEquals(2, setupPagePosition1.getGroup().size());
    assertEquals(0, setupPagePosition1.getGroup().get(0));
    assertEquals(WikiPageIdentity.class, setupPagePosition1.getGroup().get(1).getClass());
    PagePosition setupPagePosition2 = lastPos.get(1);
    assertEquals(0, setupPagePosition2.getPositionInGroup());
    assertEquals(2, setupPagePosition2.getGroup().size());
    assertEquals(1, setupPagePosition2.getGroup().get(0));
    assertEquals(WikiPageIdentity.class, setupPagePosition2.getGroup().get(1).getClass());
  }

  private List<WikiPage> addTestPages() {
    addChildPage(root, SUITE_SETUP_NAME);
    addChildPage(root, SUITE_TEARDOWN_NAME);
    WikiPage slimSuite1 = addChildPage(suite, "SlimPage1Suite");
    addChildPage(slimSuite1, "SlimPage1Test");
    addChildPage(slimSuite1, SUITE_TEARDOWN_NAME);
    WikiPage slimSuite2 = addChildPage(suite, "SlimPage2Suite");
    addChildPage(slimSuite2, "SlimPage2Test");
    WikiPage slimSuite3 = addChildPage(suite, "SlimPage3Suite");
    addChildPage(slimSuite3, "SlimPage3Test");
    addChildPage(slimSuite2, SUITE_SETUP_NAME);

    return makeTestPageList();
  }


}
