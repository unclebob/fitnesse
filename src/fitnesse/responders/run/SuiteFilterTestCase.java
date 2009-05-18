/**
 * Copyright AdScale GmbH, Germany, 2009
 */
package fitnesse.responders.run;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

public class SuiteFilterTestCase {
  private WikiPage root;
  private PageCrawler crawler;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    PageData data = root.getData();
    root.commit(data);
  }

  private WikiPage addTestPage(WikiPage page, String name, String content)
      throws Exception {
    WikiPage testPage = crawler.addPage(page, PathParser.parse(name), content);
    PageData data = testPage.getData();
    data.setAttribute("Test");
    testPage.commit(data);
    return testPage;
  }
  
  private WikiPage addSuitePage(WikiPage page, String name, String content)
  throws Exception {
    WikiPage suitePage = crawler.addPage(page, PathParser.parse(name), content);
    PageData data = suitePage.getData();
    data.setAttribute("Suite");
    suitePage.commit(data);
    return suitePage;
  }

  @Test
  public void testPrunesTests() throws Exception {
    SuiteFilter filter = new SuiteFilter(null, null);
    
    WikiPage prunedTest = addTestPage(root, "PrunedTest", "Pruned Test");
    PageData data = prunedTest.getData();
    data.setAttribute(PageData.PropertyPRUNE);
    prunedTest.commit(data);
    assertFalse(filter.isMatchingTest(prunedTest));
    
    WikiPage test = addTestPage(root, "TestPage", "Test test");
    assertTrue(filter.isMatchingTest(test));
  }

  @Test
  public void testPrunesNonTests() throws Exception {
    SuiteFilter filter = new SuiteFilter(null, null);

    assertFalse(filter.isMatchingTest(root));
  }
  
  @Test
  public void testPrunesSuites() throws Exception {
    SuiteFilter filter = new SuiteFilter(null, null);
    
    WikiPage prunedSuite = crawler.addPage(root, PathParser.parse("MySuite"), "the suite");
    PageData data = prunedSuite.getData();
    data.setAttribute(PageData.PropertyPRUNE);
    data.setAttribute("Suite");
    prunedSuite.commit(data);
    
    assertFalse(filter.getFilterForTestsInSuite(prunedSuite).hasMatchingTests());

    assertTrue(filter.getFilterForTestsInSuite(root).hasMatchingTests());
  }

  @Test
  public void testTestRequiresTag() throws Exception {
    SuiteFilter filter = new SuiteFilter("good", "");
    
    WikiPage goodTest = addTestPage(root, "GoodTest", "Good Test");
    PageData data = goodTest.getData();
    data.setAttribute(PageData.PropertySUITES, "good");
    goodTest.commit(data);
    assertTrue(filter.isMatchingTest(goodTest));
    
    WikiPage notGoodTest = addTestPage(root, "NotGoodTest", "Not Good Test");
    assertFalse(filter.isMatchingTest(notGoodTest));
  }

  @Test
  public void testSuiteWithTag() throws Exception {
    SuiteFilter filter = new SuiteFilter("good", null);

    WikiPage goodSuite = crawler.addPage(root, PathParser.parse("MySuite"), "the suite");
    PageData data = goodSuite.getData();
    data.setAttribute("Suite");
    data.setAttribute(PageData.PropertySUITES, "good");
    goodSuite.commit(data);
    
    WikiPage goodSuiteTest = addTestPage(goodSuite, "GoodTest", "Good Test");
    assertTrue(filter.getFilterForTestsInSuite(goodSuite).isMatchingTest(goodSuiteTest));
    assertFalse(filter.getFilterForTestsInSuite(root).isMatchingTest(goodSuiteTest));
  }
  
  @Test 
  public void testChecksStartFilter() throws Exception {
    WikiPage bobSuite = addSuitePage(root, "BobsTests", "B tests");
    WikiPage testPage = addTestPage(bobSuite, "MyTest", "my test");
    
    SuiteFilter andyFilter = new SuiteFilter(null, "AndyTest");
    assertTrue(andyFilter.isMatchingTest(testPage));

    SuiteFilter andyFilter2 = new SuiteFilter(null, "AndyTest.TestsA.FirstTest");
    assertTrue(andyFilter2.isMatchingTest(testPage));

    SuiteFilter bobsFilter = new SuiteFilter(null, "BobsTests");
    assertTrue(bobsFilter.isMatchingTest(testPage));

    SuiteFilter sisterMatchFilter = new SuiteFilter(null, "BobsTests.CharlesTest");
    assertTrue(sisterMatchFilter.isMatchingTest(testPage));
    
    SuiteFilter exactMatchFilter = new SuiteFilter(null, "BobsTests.MyTest");
    assertTrue(exactMatchFilter.isMatchingTest(testPage));

    SuiteFilter tooMuchFilter = new SuiteFilter(null, "BobsTests.MyTest.AnotherTest");
    assertFalse(tooMuchFilter.isMatchingTest(testPage));

    SuiteFilter sisterNotMatchFilter = new SuiteFilter(null, "BobsTests.PaulTest.TestingFirst");
    assertFalse(sisterNotMatchFilter.isMatchingTest(testPage));

    SuiteFilter carlyFilter = new SuiteFilter(null, "ZzzsTests.CarlyFirstTest");
    assertFalse(carlyFilter.isMatchingTest(testPage));
    
    SuiteFilter filter = new SuiteFilter("mytage, tag2", "IntegrationTests.BlahTest");
    System.out.println(filter.toString());
  }
}
