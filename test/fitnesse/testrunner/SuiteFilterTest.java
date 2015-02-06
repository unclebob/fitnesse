/**
 * Copyright AdScale GmbH, Germany, 2009
 */
package fitnesse.testrunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import fitnesse.wiki.*;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

public class SuiteFilterTest {
  private WikiPage root;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    PageData data = root.getData();
    root.commit(data);
  }

  private WikiPage addTestPage(WikiPage page, String name, String content)
      throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(page, PathParser.parse(name), content);
    PageData data = testPage.getData();
    data.setAttribute("Test");
    testPage.commit(data);
    return testPage;
  }
  
  private WikiPage addSuitePage(WikiPage page, String name, String content)
  throws Exception {
    WikiPage suitePage = WikiPageUtil.addPage(page, PathParser.parse(name), content);
    PageData data = suitePage.getData();
    data.setAttribute("Suite");
    suitePage.commit(data);
    return suitePage;
  }

  @Test
  public void testPrunesNonTests() throws Exception {
    SuiteFilter filter = new SuiteFilter(null, null, null, null);

    assertFalse(filter.isMatchingTest(root));
  }

  @Test
  public void testTestRequiresTag() throws Exception {
    SuiteFilter filter = new SuiteFilter("good", null, null,  "");
    
    WikiPage goodTest = addTestPage(root, "GoodTest", "Good Test");
    PageData data = goodTest.getData();
    data.setAttribute(PageData.PropertySUITES, "good");
    goodTest.commit(data);
    assertTrue(filter.isMatchingTest(goodTest));
    
    WikiPage notGoodTest = addTestPage(root, "NotGoodTest", "Not Good Test");
    assertFalse(filter.isMatchingTest(notGoodTest));
  }
  
  @Test
  public void testTestRequiresAllTagsWithIntersect() throws Exception {
    SuiteFilter filter = new SuiteFilter(null, null, "good, better",  "");
    
    WikiPage goodTest = addTestPage(root, "GoodTest", "Good Test");
    PageData data = goodTest.getData();
    data.setAttribute(PageData.PropertySUITES, "good, better, best");
    goodTest.commit(data);
    assertTrue(filter.isMatchingTest(goodTest));
    
    WikiPage notGoodTest = addTestPage(root, "NotGoodTest", "Not Good Test");
    PageData data2 = notGoodTest.getData();
    data2.setAttribute(PageData.PropertySUITES, "good, bad");
    notGoodTest.commit(data2);
    assertFalse(filter.isMatchingTest(notGoodTest));
  }

  @Test
  public void testSuiteWithTag() throws Exception {
    SuiteFilter filter = new SuiteFilter("good", null, null,  null);

    WikiPage goodSuite = WikiPageUtil.addPage(root, PathParser.parse("MySuite"), "the suite");
    PageData data = goodSuite.getData();
    data.setAttribute("Suite");
    data.setAttribute(PageData.PropertySUITES, "good");
    goodSuite.commit(data);
    
    WikiPage goodSuiteTest = addTestPage(goodSuite, "GoodTest", "Good Test");
    assertTrue(filter.getFilterForTestsInSuite(goodSuite).isMatchingTest(goodSuiteTest));
    assertFalse(filter.getFilterForTestsInSuite(root).isMatchingTest(goodSuiteTest));
  }
  
  @Test
  public void testSuiteWithTagWithIntersect() throws Exception {
    SuiteFilter filter = new SuiteFilter(null, null, "good, better",  null);

    WikiPage goodSuite = WikiPageUtil.addPage(root, PathParser.parse("MySuite"), "the suite");
    PageData data = goodSuite.getData();
    data.setAttribute("Suite");
    data.setAttribute(PageData.PropertySUITES, "good, better");
    goodSuite.commit(data);
    
    WikiPage goodSuiteTest = addTestPage(goodSuite, "GoodTest", "Good Test");
    assertTrue(filter.getFilterForTestsInSuite(goodSuite).isMatchingTest(goodSuiteTest));
    assertFalse(filter.getFilterForTestsInSuite(root).isMatchingTest(goodSuiteTest));
  }
  
  @Test 
  public void testChecksStartFilter() throws Exception {
    WikiPage bobSuite = addSuitePage(root, "BobsTests", "B tests");
    WikiPage testPage = addTestPage(bobSuite, "MyTest", "my test");
    
    SuiteFilter andyFilter = new SuiteFilter(null, null, null, "AndyTest");
    assertTrue(andyFilter.isMatchingTest(testPage));

    SuiteFilter andyFilter2 = new SuiteFilter(null, null, null, "AndyTest.TestsA.FirstTest");
    assertTrue(andyFilter2.isMatchingTest(testPage));

    SuiteFilter bobsFilter = new SuiteFilter(null, null, null, "BobsTests");
    assertTrue(bobsFilter.isMatchingTest(testPage));

    SuiteFilter sisterMatchFilter = new SuiteFilter(null, null, null, "BobsTests.CharlesTest");
    assertTrue(sisterMatchFilter.isMatchingTest(testPage));
    
    SuiteFilter exactMatchFilter = new SuiteFilter(null, null, null, "BobsTests.MyTest");
    assertTrue(exactMatchFilter.isMatchingTest(testPage));

    SuiteFilter tooMuchFilter = new SuiteFilter(null, null, null, "BobsTests.MyTest.AnotherTest");
    assertFalse(tooMuchFilter.isMatchingTest(testPage));

    SuiteFilter sisterNotMatchFilter = new SuiteFilter(null, null, null, "BobsTests.PaulTest.TestingFirst");
    assertFalse(sisterNotMatchFilter.isMatchingTest(testPage));

    SuiteFilter carlyFilter = new SuiteFilter(null, null, null, "ZzzsTests.CarlyFirstTest");
    assertFalse(carlyFilter.isMatchingTest(testPage));
  }
  
  @Test
  public void testChecksNotMatchFilterTest() throws Exception {
    SuiteFilter filter = new SuiteFilter(null, "bad", null,  null);

    WikiPage failTest = addTestPage(root, "BadTest", "Bad Test");
    PageData data = failTest.getData();
    data.setAttribute(PageData.PropertySUITES, "bad");
    failTest.commit(data);
    assertFalse(filter.isMatchingTest(failTest));
    
    WikiPage passTest = addTestPage(root, "PassTest", "Pass Test");
    assertTrue(filter.isMatchingTest(passTest));
  }
  
  @Test
  public void testChecksNotMatchFilterSuite() throws Exception {
    SuiteFilter filter = new SuiteFilter(null, "bad", null,  null);

    WikiPage failSuite = addTestPage(root, "FailSuite", "Bad Test");
    PageData data = failSuite.getData();
    data.setAttribute(PageData.PropertySUITES, "bad");
    data.setAttribute("Suite");
    failSuite.commit(data);

    assertFalse(filter.getFilterForTestsInSuite(failSuite).hasMatchingTests());
  }

  @Test
  public void testChecksNotMatchFilterWithInvalidTagSuite() throws Exception {
    SuiteFilter filter = new SuiteFilter(null, "bad, notsobad", "",  null);

    WikiPage failSuite = addTestPage(root, "FailSuite", "Bad Test");
    PageData data = failSuite.getData();
    data.setAttribute(PageData.PropertySUITES, "bad");
    data.setAttribute("Suite");
    failSuite.commit(data);

    assertFalse(filter.getFilterForTestsInSuite(failSuite).hasMatchingTests());
  }

  @Test
  public void testFilterDescription() throws Exception {
    SuiteFilter filter = new SuiteFilter("good", "bad", null, "FirstTest");
    assertEquals("matches 'good' & doesn't match 'bad' & starts with test 'FirstTest'", filter.toString());
  }
  
  @Test
  public void testFilterDescriptionWithIntersect() throws Exception {
    SuiteFilter filter = new SuiteFilter(null, "bad", "good, better", "FirstTest");
    assertEquals("matches all of 'good, better' & doesn't match 'bad' & starts with test 'FirstTest'", filter.toString());
  }
}
