package fitnesse.responders.search;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;

import static junit.framework.Assert.*;

public class PageSearcherTest {

  private WikiPage root;
  private PageCrawler crawler;
  private WikiPage page;
  private PageSearcher searcher;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    searcher = new PageSearcher();
    page = crawler.addPage(root, PathParser.parse("TestPage"));
  }

  @Test
  public void testPageMatchesQueryWithSingleAttribute() throws Exception {
    String[] suites = new String[0];

    List<String> pageTypes = Arrays.asList("Test");
    Map<String, Boolean> attributes = new HashMap<String, Boolean>();
    setupRequestInputAndPageProperty("Test", attributes, true, page, null);
    assertFalse(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        false, false));

    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        false, false));

    pageTypes = Arrays.asList("Normal", "Suite");
    setupRequestInputAndPageProperty("Test", attributes, false, page, null);
    assertTrue(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        false, false));

    setupRequestInputAndPageProperty("Test", attributes, false, page, "true");
    assertFalse(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        false, false));
  }

  @Test
  public void testPageMatchesQueryWithMultipleAttributes() throws Exception {
    String[] suites = new String[0];

    List<String> pageTypes = Arrays.asList("Test");
    Map<String, Boolean> attributes = new HashMap<String, Boolean>();
    setupRequestInputAndPageProperty("Test", attributes, true, page, null);
    setupRequestInputAndPageProperty("Suite", attributes, true, page, null);
    assertFalse(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        false, false));

    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    setupRequestInputAndPageProperty("Suite", attributes, false, page, null);
    assertTrue(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        false, false));

    setupRequestInputAndPageProperty("Test", attributes, false, page, "true");
    setupRequestInputAndPageProperty("Suite", attributes, false, page, null);
    assertFalse(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        false, false));

    setupRequestInputAndPageProperty("Test", attributes, false, page, null);
    setupRequestInputAndPageProperty("Suite", attributes, false, page, "true");
    assertFalse(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        false, false));
  }

  @Test
  public void testPageMatchesQueryWithExcludedPages() throws Exception {
    Map<String, Boolean> attributes = new HashMap<String, Boolean>();
    String[] suites = new String[0];

    List<String> pageTypes = Arrays.asList("Test");
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        true, true));

    page = crawler.addPage(root, PathParser.parse("SetUp"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        false, true));
    assertTrue(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        false, false));
    assertFalse(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        true, false));

    page = crawler.addPage(root, PathParser.parse("TearDown"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        false, false));
    assertTrue(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        true, false));
    assertFalse(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        false, true));

    page = crawler.addPage(root, PathParser.parse("SuiteSetUp"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        false, false));
    assertFalse(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        true, false));

    page = crawler.addPage(root, PathParser.parse("SuiteTearDown"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        false, false));
    assertFalse(searcher.pageMatchesQuery(page, pageTypes, attributes, suites,
        false, true));
  }

  @Test
  public void testPageMatchQueryWithSuites() throws Exception {
    List<String> pageTypes = Arrays.asList("Test");
    Map<String, Boolean> requestInputs = new HashMap<String, Boolean>();
    assertTrue(searcher.pageMatchesQuery(page, pageTypes, requestInputs, null,
        false, false));
    assertTrue(searcher.pageMatchesQuery(page, pageTypes, requestInputs,
        new String[0], false, false));

    String[] suites = new String[] { "SuiteTest" };
    assertFalse(searcher.pageMatchesQuery(page, pageTypes, requestInputs,
        suites, false, false));

    setUpSuitesProperty(page, "SuiteTest");
    assertTrue(searcher.pageMatchesQuery(page, pageTypes, requestInputs, null,
        false, false));
    assertFalse(searcher.pageMatchesQuery(page, pageTypes, requestInputs,
        new String[0], false, false));
    assertTrue(searcher.pageMatchesQuery(page, pageTypes, requestInputs,
        suites, false, false));

    setUpSuitesProperty(page, "SuiteTest, SuiteTest2");
    assertTrue(searcher.pageMatchesQuery(page, pageTypes, requestInputs,
        suites, false, false));

    setUpSuitesProperty(page, "SuiteTest2 , SuiteTest3");
    assertFalse(searcher.pageMatchesQuery(page, pageTypes, requestInputs,
        suites, false, false));

    suites = new String[] { "SuiteTest2", "SuiteTest3" };
    assertTrue(searcher.pageMatchesQuery(page, pageTypes, requestInputs,
        suites, false, false));

    setUpSuitesProperty(page, "SuiteTest, SuiteTest2");
    assertFalse(searcher.pageMatchesQuery(page, pageTypes, requestInputs,
        suites, false, false));
  }

  private void setUpSuitesProperty(WikiPage page, String value)
  throws Exception {
    PageData data = page.getData();
    data.getProperties().set("Suites", value);
    page.commit(data);
  }

  private void setupRequestInputAndPageProperty(String attributeName,
      Map<String, Boolean> requestInputs, boolean requestValue, WikiPage page,
      String pageDataValue) throws Exception {
    requestInputs.put(attributeName, requestValue);

    PageData pageData = page.getData();
    if (pageDataValue == null)
      pageData.getProperties().remove(attributeName);
    else
      pageData.getProperties().set(attributeName, pageDataValue);
    page.commit(pageData);
  }

  public void testCheckAttributeValue() {
    assertTrue(searcher.attributeMatchesInput(false, false));
    assertTrue(searcher.attributeMatchesInput(true, true));
    assertFalse(searcher.attributeMatchesInput(false, true));
    assertFalse(searcher.attributeMatchesInput(true, false));
  }

}
