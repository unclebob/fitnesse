package fitnesse.components;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.ArrayList;
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

public class AttributeWikiPageFinderTest implements SearchObserver {

  private WikiPage root;
  private PageCrawler crawler;
  private WikiPage page;
  private AttributeWikiPageFinder searcher;

  private List<WikiPage> hits = new ArrayList<WikiPage>();

  public void hit(WikiPage page) throws Exception {
    hits.add(page);
  }

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    searcher = new AttributeWikiPageFinder(this, Arrays.asList("Test"),
        new HashMap<String, Boolean>(), new String[0], false, false);
    page = crawler.addPage(root, PathParser.parse("TestPage"));
    hits.clear();
  }

  @Test
  public void testPageMatchesQueryWithSingleAttribute() throws Exception {
    String[] suites = new String[0];

    Map<String, Boolean> attributes = new HashMap<String, Boolean>();
    searcher = new AttributeWikiPageFinder(this, Arrays.asList("Test"), attributes, suites,
        false, false);
    setupRequestInputAndPageProperty("Test", attributes, true, page, null);
    assertFalse(searcher.pageMatches(page));

    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(searcher.pageMatches(page));

    searcher = new AttributeWikiPageFinder(this, Arrays.asList("Normal", "Suite"), attributes,
        suites, false, false);
    setupRequestInputAndPageProperty("Test", attributes, false, page, null);
    assertTrue(searcher.pageMatches(page));

    setupRequestInputAndPageProperty("Test", attributes, false, page, "true");
    assertFalse(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchesQueryWithMultipleAttributes() throws Exception {
    String[] suites = new String[0];

    Map<String, Boolean> attributes = new HashMap<String, Boolean>();
    searcher = new AttributeWikiPageFinder(this, Arrays.asList("Test"), attributes, suites,
        false, false);
    setupRequestInputAndPageProperty("Test", attributes, true, page, null);
    setupRequestInputAndPageProperty("Suite", attributes, true, page, null);
    assertFalse(searcher.pageMatches(page));

    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    setupRequestInputAndPageProperty("Suite", attributes, false, page, null);
    assertTrue(searcher.pageMatches(page));

    setupRequestInputAndPageProperty("Test", attributes, false, page, "true");
    setupRequestInputAndPageProperty("Suite", attributes, false, page, null);
    assertFalse(searcher.pageMatches(page));

    setupRequestInputAndPageProperty("Test", attributes, false, page, null);
    setupRequestInputAndPageProperty("Suite", attributes, false, page, "true");
    assertFalse(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchesQueryWithExcludedSetUps() throws Exception {
    Map<String, Boolean> attributes = new HashMap<String, Boolean>();
    String[] suites = new String[0];

    List<String> pageTypes = Arrays.asList("Test");
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    searcher = new AttributeWikiPageFinder(this, pageTypes, attributes, suites,
        true, false);
    assertTrue(searcher.pageMatches(page));

    page = crawler.addPage(root, PathParser.parse("SetUp"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertFalse(searcher.pageMatches(page));

    page = crawler.addPage(root, PathParser.parse("TearDown"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(searcher.pageMatches(page));

    page = crawler.addPage(root, PathParser.parse("SuiteSetUp"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertFalse(searcher.pageMatches(page));

    page = crawler.addPage(root, PathParser.parse("SuiteTearDown"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchesQueryWithIncludedSetUps() throws Exception {
    Map<String, Boolean> attributes = new HashMap<String, Boolean>();
    String[] suites = new String[0];

    List<String> pageTypes = Arrays.asList("Test");
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    searcher = new AttributeWikiPageFinder(this, pageTypes, attributes, suites,
        false, false);
    assertTrue(searcher.pageMatches(page));

    page = crawler.addPage(root, PathParser.parse("SetUp"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(searcher.pageMatches(page));

    page = crawler.addPage(root, PathParser.parse("TearDown"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(searcher.pageMatches(page));

    page = crawler.addPage(root, PathParser.parse("SuiteSetUp"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(searcher.pageMatches(page));

    page = crawler.addPage(root, PathParser.parse("SuiteTearDown"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchesQueryWithExcludedTearDowns() throws Exception {
    Map<String, Boolean> attributes = new HashMap<String, Boolean>();
    String[] suites = new String[0];

    List<String> pageTypes = Arrays.asList("Test");
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    searcher = new AttributeWikiPageFinder(this, pageTypes, attributes, suites,
        false, true);
    assertTrue(searcher.pageMatches(page));

    page = crawler.addPage(root, PathParser.parse("SetUp"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(searcher.pageMatches(page));

    page = crawler.addPage(root, PathParser.parse("TearDown"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertFalse(searcher.pageMatches(page));

    page = crawler.addPage(root, PathParser.parse("SuiteSetUp"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(searcher.pageMatches(page));

    page = crawler.addPage(root, PathParser.parse("SuiteTearDown"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertFalse(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchWithNullSuites() throws Exception {
    List<String> pageTypes = Arrays.asList("Test");
    Map<String, Boolean> requestInputs = new HashMap<String, Boolean>();
    searcher = new AttributeWikiPageFinder(this, pageTypes, requestInputs, null, false, false);
    assertTrue(searcher.pageMatches(page));

    setUpSuitesProperty(page, "SuiteTest");
    assertTrue(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchWithEmptySuites() throws Exception {
    List<String> pageTypes = Arrays.asList("Test");
    Map<String, Boolean> requestInputs = new HashMap<String, Boolean>();
    searcher = new AttributeWikiPageFinder(this, pageTypes, requestInputs, new String[0], false, false);
    assertTrue(searcher.pageMatches(page));

    setUpSuitesProperty(page, "SuiteTest");
    assertFalse(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchQueryWithSingleSuite() throws Exception {
    List<String> pageTypes = Arrays.asList("Test");
    Map<String, Boolean> requestInputs = new HashMap<String, Boolean>();
    String[] suites = new String[] { "SuiteTest" };

    searcher = new AttributeWikiPageFinder(this, pageTypes, requestInputs, suites, false, false);

    assertFalse(searcher.pageMatches(page));

    setUpSuitesProperty(page, "SuiteTest");
    assertTrue(searcher.pageMatches(page));

    setUpSuitesProperty(page, "SuiteTest, SuiteTest2");
    assertTrue(searcher.pageMatches(page));

    setUpSuitesProperty(page, "SuiteTest2 , SuiteTest3");
    assertFalse(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchQueryWithMultipleSuites() throws Exception {
    List<String> pageTypes = Arrays.asList("Test");
    Map<String, Boolean> requestInputs = new HashMap<String, Boolean>();
    String[] suites = new String[] { "SuiteTest2", "SuiteTest3" };

    searcher = new AttributeWikiPageFinder(this, pageTypes, requestInputs, suites, false, false);

    setUpSuitesProperty(page, "SuiteTest2 , SuiteTest3");
    assertTrue(searcher.pageMatches(page));

    setUpSuitesProperty(page, "SuiteTest, SuiteTest2");
    assertFalse(searcher.pageMatches(page));
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

  @Test
  public void testCheckAttributeValue() {
    assertTrue(searcher.attributeMatchesInput(false, false));
    assertTrue(searcher.attributeMatchesInput(true, true));
    assertFalse(searcher.attributeMatchesInput(false, true));
    assertFalse(searcher.attributeMatchesInput(true, false));
  }

}
