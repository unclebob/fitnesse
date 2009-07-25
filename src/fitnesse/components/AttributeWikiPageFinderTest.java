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
        new HashMap<String, Boolean>(), "");
    page = crawler.addPage(root, PathParser.parse("TestPage"));
    hits.clear();
  }

  @Test
  public void testPageMatchesQueryWithSingleAttribute() throws Exception {
    Map<String, Boolean> attributes = new HashMap<String, Boolean>();
    searcher = new AttributeWikiPageFinder(this, Arrays.asList("Test"), attributes, "");
    setupRequestInputAndPageProperty("Test", attributes, true, page, null);
    assertFalse(searcher.pageMatches(page));

    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(searcher.pageMatches(page));

    searcher = new AttributeWikiPageFinder(this, Arrays.asList("Normal", "Suite"), attributes,
    "");
    setupRequestInputAndPageProperty("Test", attributes, false, page, null);
    assertTrue(searcher.pageMatches(page));

    setupRequestInputAndPageProperty("Test", attributes, false, page, "true");
    assertFalse(searcher.pageMatches(page));
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
  public void testPageMatchesQueryWithMultipleAttributes() throws Exception {
    Map<String, Boolean> attributes = new HashMap<String, Boolean>();
    searcher = new AttributeWikiPageFinder(this, Arrays.asList("Test"), attributes, "");
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
    attributes.put("SetUp", false);

    List<String> pageTypes = Arrays.asList("Test", "Normal", "Suite");
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    searcher = new AttributeWikiPageFinder(this, pageTypes, attributes, "");
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
    attributes.put("SetUp", true);

    List<String> pageTypes = Arrays.asList("Test", "Normal", "Suite");
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    searcher = new AttributeWikiPageFinder(this, pageTypes, attributes, "");
    assertFalse(searcher.pageMatches(page));

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
  public void testPageMatchesQueryWithExcludedTearDowns() throws Exception {
    Map<String, Boolean> attributes = new HashMap<String, Boolean>();
    attributes.put("TearDown", false);

    List<String> pageTypes = Arrays.asList("Suite", "Test", "Normal");
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    searcher = new AttributeWikiPageFinder(this, pageTypes, attributes, "");
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
  public void testPageMatchesQueryWithIncludedTearDowns() throws Exception {
    Map<String, Boolean> attributes = new HashMap<String, Boolean>();
    attributes.put("TearDown", true);

    List<String> pageTypes = Arrays.asList("Test", "Normal", "Suite");
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    searcher = new AttributeWikiPageFinder(this, pageTypes, attributes, "");
    assertFalse(searcher.pageMatches(page));

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
  public void testPageMatchWithNullSuites() throws Exception {
    List<String> pageTypes = Arrays.asList("Test");
    Map<String, Boolean> requestInputs = new HashMap<String, Boolean>();
    searcher = new AttributeWikiPageFinder(this, pageTypes, requestInputs, null);
    assertTrue(searcher.pageMatches(page));

    setUpSuitesProperty(page, "SuiteTest");
    assertTrue(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchWithEmptySuites() throws Exception {
    List<String> pageTypes = Arrays.asList("Test");
    Map<String, Boolean> requestInputs = new HashMap<String, Boolean>();
    searcher = new AttributeWikiPageFinder(this, pageTypes, requestInputs, "");
    assertTrue(searcher.pageMatches(page));

    setUpSuitesProperty(page, "SuiteTest");
    assertFalse(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchQueryWithSingleSuite() throws Exception {
    List<String> pageTypes = Arrays.asList("Test");
    Map<String, Boolean> requestInputs = new HashMap<String, Boolean>();
    searcher = new AttributeWikiPageFinder(this, pageTypes, requestInputs, "SuiteTest");

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
    searcher = new AttributeWikiPageFinder(this, pageTypes, requestInputs, "SuiteTest2,SuiteTest3");

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

  @Test
  public void testCheckAttributeValue() {
    assertTrue(searcher.attributeMatchesInput(false, false));
    assertTrue(searcher.attributeMatchesInput(true, true));
    assertFalse(searcher.attributeMatchesInput(false, true));
    assertFalse(searcher.attributeMatchesInput(true, false));
  }

}
