package fitnesse.wiki.search;

import fitnesse.wiki.HitCollector;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PageType;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static fitnesse.wiki.PageData.PropertySUITES;
import static fitnesse.wiki.PageType.STATIC;
import static fitnesse.wiki.PageType.SUITE;
import static fitnesse.wiki.PageType.TEST;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class AttributeWikiPageFinderTest {

  private WikiPage root;
  private WikiPage page;
  private AttributeWikiPageFinder searcher;

  private HitCollector hits = new HitCollector();

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    searcher = new AttributeWikiPageFinder(hits, Arrays.asList(TEST),
        new HashMap<String, Boolean>(), new ArrayList<String>());
    page = WikiPageUtil.addPage(root, PathParser.parse("TestPage"));
  }

  @Test
  public void testPageMatchesQueryWithSingleAttribute() throws Exception {
    Map<String, Boolean> attributes = new HashMap<>();
    searcher = new AttributeWikiPageFinder(hits, Arrays.asList(TEST),
        attributes, new ArrayList<String>());
    attributes.put(TEST.toString(), true);
    assertTrue(searcher.pageMatches(page));

    removePageProperty(page, TEST.toString());
    assertFalse(searcher.pageMatches(page));

    searcher = generateSearcherByPageTypesAndSearchAttributes(Arrays.asList(STATIC, SUITE), attributes);

    attributes.put(TEST.toString(), false);
    assertTrue(searcher.pageMatches(page));

    setPageProperty(page, TEST.toString(), "true");
    assertFalse(searcher.pageMatches(page));
  }

  private AttributeWikiPageFinder generateSearcherByPageTypesAndSearchAttributes(List<PageType> pageTypes,
      Map<String, Boolean> attributes) {
    return new AttributeWikiPageFinder(hits, pageTypes, attributes, new ArrayList<String>());
  }

  private void removePageProperty(WikiPage page, String attributeName)
      throws Exception {
    PageData pageData = page.getData();
    pageData.getProperties().remove(attributeName);
    page.commit(pageData);
  }

  private void setPageProperty(WikiPage page, String propertyName) {
    PageData pageData = page.getData();
    pageData.setAttribute(propertyName);
    page.commit(pageData);
  }


  private void setPageProperty(WikiPage page, String propertyName,
      String propertyValue) {
    PageData pageData = page.getData();
    pageData.setAttribute(propertyName, propertyValue);
    page.commit(pageData);
  }

  @Test
  public void testPageMatchesQueryWithMultipleAttributes() throws Exception {
    Map<String, Boolean> attributes = new HashMap<>();
    searcher = generateSearcherByPageTypesAndSearchAttributes(Arrays.asList(TEST), attributes);
    removePageProperty(page, TEST.toString());
    attributes.put(TEST.toString(), true);
    attributes.put(SUITE.toString(), true);
    assertFalse(searcher.pageMatches(page));

    attributes.put(SUITE.toString(), false);
    setPageProperty(page, TEST.toString());
    assertTrue(searcher.pageMatches(page));

    // No attributes set, so no settings are taken into account
    attributes.put(TEST.toString(), false);
    assertTrue(searcher.pageMatches(page));

    removePageProperty(page, TEST.toString());
    setPageProperty(page, SUITE.toString());
    assertFalse(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchesQueryWithExcludedSetUps() throws Exception {
    Map<String, Boolean> attributes = new HashMap<>();
    attributes.put("SetUp", false);

    List<PageType> pageTypes = Arrays.asList(TEST, STATIC, SUITE);
    searcher = generateSearcherByPageTypesAndSearchAttributes(pageTypes, attributes);
    setPageProperty(page, TEST.toString());
    assertTrue(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SetUp"));
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("TearDown"));
    assertTrue(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"));
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"));
    assertTrue(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchesQueryWithIncludedSetUps() throws Exception {
    Map<String, Boolean> attributes = new HashMap<>();
    attributes.put("SetUp", true);

    List<PageType> pageTypes = Arrays.asList(TEST, STATIC, SUITE);
    searcher = generateSearcherByPageTypesAndSearchAttributes(pageTypes, attributes);
    setPageProperty(page, TEST.toString());
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SetUp"));
    assertTrue(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("TearDown"));
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"));
    assertTrue(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"));
    assertFalse(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchesQueryWithExcludedTearDowns() throws Exception {
    Map<String, Boolean> attributes = new HashMap<>();
    attributes.put("TearDown", false);

    List<PageType> pageTypes = Arrays.asList(SUITE, TEST, STATIC);
    searcher = generateSearcherByPageTypesAndSearchAttributes(pageTypes, attributes);
    setPageProperty(page, TEST.toString());
    assertTrue(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SetUp"));
    assertTrue(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("TearDown"));
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"));
    assertTrue(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"));
    assertFalse(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchesQueryWithIncludedTearDowns() throws Exception {
    Map<String, Boolean> attributes = new HashMap<>();
    attributes.put("TearDown", true);

    List<PageType> pageTypes = Arrays.asList(TEST, STATIC, SUITE);
    searcher = generateSearcherByPageTypesAndSearchAttributes(pageTypes, attributes);
    setPageProperty(page, TEST.toString());
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SetUp"));
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("TearDown"));
    assertTrue(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"));
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"));
    assertTrue(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchWithNullSuites() throws Exception {
    searcher = generateSearcherByPagesTypesAndSuites(Arrays.asList(TEST), null);
    assertTrue(searcher.pageMatches(page));

    setPageProperty(page, PropertySUITES, "SuiteTest");
    assertTrue(searcher.pageMatches(page));
  }

  private AttributeWikiPageFinder generateSearcherByPagesTypesAndSuites(List<PageType> pageTypes, List<String> suites) {
    return new AttributeWikiPageFinder(hits, pageTypes, new HashMap<String, Boolean>(), suites);
  }

  @Test
  public void testPageMatchWithEmptySuites() throws Exception {
    Map<String, Boolean> requestInputs = new HashMap<>();
    searcher = generateSearcherByPageTypesAndSearchAttributes(Arrays.asList(TEST), requestInputs);
    assertTrue(searcher.pageMatches(page));

    setPageProperty(page, PropertySUITES, "SuiteTest");
    assertFalse(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchQueryWithSingleSuite() throws Exception {
    List<String> suites = Arrays.asList("SuiteTest");
    searcher = generateSearcherByPagesTypesAndSuites(Arrays.asList(TEST), suites);

    assertFalse(searcher.pageMatches(page));

    setPageProperty(page, PropertySUITES, "SuiteTest");
    assertTrue(searcher.pageMatches(page));

    setPageProperty(page, PropertySUITES, "SuiteTest, SuiteTest2");
    assertTrue(searcher.pageMatches(page));

    setPageProperty(page, PropertySUITES, "SuiteTest2 , SuiteTest3");
    assertFalse(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchQueryWithMultipleSuites() throws Exception {
    List<String> suites = Arrays.asList("SuiteTest2","SuiteTest3");
    searcher = generateSearcherByPagesTypesAndSuites(Arrays.asList(TEST), suites);

    setPageProperty(page, PropertySUITES, "SuiteTest2 , SuiteTest3");
    assertTrue(searcher.pageMatches(page));

    setPageProperty(page, PropertySUITES, "SuiteTest, SuiteTest2");
    assertFalse(searcher.pageMatches(page));
  }

  @Test
  public void testCheckAttributeValue() {
    assertTrue(searcher.attributeMatchesInput(false, false));
    assertTrue(searcher.attributeMatchesInput(true, true));
    assertFalse(searcher.attributeMatchesInput(false, true));
    assertTrue(searcher.attributeMatchesInput(true, false));
  }

  @Test
  public void testSetUpAndTearDownMatches() throws Exception {
    Map<String, Boolean> attributes = new HashMap<>();
    attributes.put("SetUp", true);
    attributes.put("TearDown", true);

    setPageProperty(page, TEST.toString(), "true");
    searcher = generateSearcherByPageTypesAndSearchAttributes(Arrays.asList(STATIC), attributes);
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SetUp"));
    assertTrue(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("TearDown"));
    assertTrue(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"));
    assertTrue(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"));
    assertTrue(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchesQueryWithExcludedSetUpsAndIncludedTearDowns()
      throws Exception {
    Map<String, Boolean> attributes = new HashMap<>();
    attributes.put("SetUp", false);
    attributes.put("TearDown", true);

    List<PageType> pageTypes = Arrays.asList(TEST, STATIC, SUITE);
    searcher = generateSearcherByPageTypesAndSearchAttributes(pageTypes, attributes);
    setPageProperty(page, TEST.toString(), "true");
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SetUp"));
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("TearDown"));
    assertTrue(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"));
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"));
    assertTrue(searcher.pageMatches(page));
  }

  @Test
  public void testPageMatchesQueryWithIncludedSetUpsAndExcludedSetUps()
      throws Exception {
    Map<String, Boolean> attributes = new HashMap<>();
    attributes.put("SetUp", true);
    attributes.put("TearDown", false);

    List<PageType> pageTypes = Arrays.asList(STATIC);
    searcher = generateSearcherByPageTypesAndSearchAttributes(pageTypes, attributes);
    setPageProperty(page, TEST.toString(), "true");
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SetUp"));
    assertTrue(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("TearDown"));
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"));
    assertTrue(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"));
    assertFalse(searcher.pageMatches(page));
  }

  @Test
  public void testSetUpAndTearDownExcluded() throws Exception {
    Map<String, Boolean> attributes = new HashMap<>();
    attributes.put("SetUp", false);
    attributes.put("TearDown", false);

    List<PageType> pageTypes = Arrays.asList(TEST, SUITE, STATIC);

    setPageProperty(page, TEST.toString(), "true");
    searcher = generateSearcherByPageTypesAndSearchAttributes(pageTypes, attributes);
    assertTrue(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SetUp"));
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("TearDown"));
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SuiteSetUp"));
    assertFalse(searcher.pageMatches(page));

    page = WikiPageUtil.addPage(root, PathParser.parse("SuiteTearDown"));
    assertFalse(searcher.pageMatches(page));
  }

}
