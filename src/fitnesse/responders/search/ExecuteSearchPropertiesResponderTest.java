package fitnesse.responders.search;

import static fitnesse.responders.search.SearchFormResponder.ATTRIBUTE;
import static fitnesse.responders.search.SearchFormResponder.SELECTED;
import static fitnesse.responders.search.SearchFormResponder.VALUE;

import java.util.HashMap;
import java.util.Map;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;

public class ExecuteSearchPropertiesResponderTest extends RegexTestCase {
  private WikiPage root;
  private PageCrawler crawler;
  private WikiPage page;
  private ExecuteSearchPropertiesResponder responder;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    responder = new ExecuteSearchPropertiesResponder();
    page = crawler.addPage(root, PathParser.parse("TestPage"));
  }

  public void testResponseWithNoParametersWillReturnEmptyPage() throws Exception {
    MockRequest request = setupRequest();
    String content = invokeResponder(request);
    assertSubString("Search Page Properties Results", content);
    assertSubString("No search properties", content);
  }

  public void testResponseWithNoMatchesWillReturnEmptyPageList() throws Exception {
    MockRequest request = setupRequest();
    setAttributeInInput(request, "Test", "off");

    String content = invokeResponder(request);

    assertSubString("No pages", content);
  }

  private void setAttributeInInput(MockRequest request, String attributeName, String attributeValue) {
    request.addInput(attributeName + ATTRIBUTE + SELECTED, "on");
    request.addInput(attributeName + VALUE, attributeValue);
  }

  public void testResponseWithMatchesWillReturnPageList() throws Exception {
    MockRequest request = setupRequest();
    setAttributeInInput(request, "Test", "on");

    String content = invokeResponder(request);

    assertOutputHasHeaderRowWithTitles(content, "Page", "Test");
    assertOutputHasRowWithLabels(content, "PageOne");

    request.addInput("Suites" + SELECTED, "on");
    request.addInput("Suites", "filter1");

    content = invokeResponder(request);

    assertHasRegexp("Number of pages.*: 1", content);
    assertOutputHasHeaderRowWithTitles(content, "Page", "Test", "Tags");
    assertOutputHasRowWithLabels(content, "PageOne", "filter1,filter2");
  }

  private String invokeResponder(MockRequest request) throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
    return response.getContent();
  }

  private MockRequest setupRequest() throws Exception {
    WikiPage page = crawler.addPage(root, PathParser.parse("PageOne"));
    PageData data = page.getData();
    data.setContent("some content");
    WikiPageProperties properties = data.getProperties();
    properties.set("Test", "true");
    properties.set("Suites", "filter1,filter2");
    page.commit(data);

    MockRequest request = new MockRequest();
    request.setResource("PageOne");
    return request;
  }

  private void assertOutputHasHeaderRowWithTitles(String content, String... titles) {
    for (String title : titles) {
      assertOutputHasRow(content, title, "strong");
    }
  }

  private void assertOutputHasRowWithLabels(String content, String... labels) {
    for (String label : labels) {
      assertOutputHasRow(content, label, "label");
    }
  }

  private void assertOutputHasRow(String content, String title, String tagName) {
    assertHasRegexp("<table.*<tr.*<td.*<" + tagName + ">" + title + "</" + tagName + ">", content);
  }

  public void testGetAttributesFromInput() {
    MockRequest request = new MockRequest();
    request.addInput("Test" + ATTRIBUTE + SELECTED, true);
    request.addInput("Suite" + ATTRIBUTE + SELECTED, true);
    request.addInput("Suite" + VALUE, "on");

    Map<String, Boolean> foundAttributes = responder.getAttributesFromInput(request);
    assertTrue(foundAttributes.containsKey("Test"));
    assertFalse(foundAttributes.get("Test"));
    assertTrue(foundAttributes.containsKey("Suite"));
    assertTrue(foundAttributes.get("Suite"));

    request.addInput("TestValue", "on");
    foundAttributes = responder.getAttributesFromInput(request);
    assertTrue(foundAttributes.get("Test"));
  }

  public void testGetSuitesFromInput() {
    MockRequest request = new MockRequest();

    String[] suites = responder.getSuitesFromInput(request);
    assertNull(suites);

    request.addInput("SuitesSelected", "on");
    suites = responder.getSuitesFromInput(request);
    assertEquals(0, suites.length);

    request.addInput("Suites", "    ");
    suites = responder.getSuitesFromInput(request);
    assertEquals(0, suites.length);

    request.addInput("Suites", "SuiteOne");
    suites = responder.getSuitesFromInput(request);
    assertEquals(1, suites.length);
    assertEquals("SuiteOne", suites[0]);

    request.addInput("Suites", "SuiteOne,SuiteTwo");
    suites = responder.getSuitesFromInput(request);
    assertEquals(2, suites.length);
    assertEquals("SuiteOne", suites[0]);
    assertEquals("SuiteTwo", suites[1]);

    request.addInput("Suites", "SuiteOne , SuiteTwo");
    suites = responder.getSuitesFromInput(request);
    assertEquals(2, suites.length);
    assertEquals("SuiteOne", suites[0]);
    assertEquals("SuiteTwo", suites[1]);
  }

  public void testPageMatchesQueryWithSingleAttribute() throws Exception {
    String[] suites = new String[0];

    Map<String, Boolean> attributes = new HashMap<String, Boolean>();
    setupRequestInputAndPageProperty("Test", attributes, true, page, null);
    assertFalse(responder.pageMatchesQuery(page, attributes, suites, false));

    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(responder.pageMatchesQuery(page, attributes, suites, false));

    setupRequestInputAndPageProperty("Test", attributes, false, page, null);
    assertTrue(responder.pageMatchesQuery(page, attributes, suites, false));

    setupRequestInputAndPageProperty("Test", attributes, false, page, "true");
    assertFalse(responder.pageMatchesQuery(page, attributes, suites, false));
  }

  public void testPageMatchesQueryWithMultipleAttributes() throws Exception {
    String[] suites = new String[0];

    Map<String, Boolean> attributes = new HashMap<String, Boolean>();
    setupRequestInputAndPageProperty("Test", attributes, true, page, null);
    setupRequestInputAndPageProperty("Suite", attributes, true, page, null);
    assertFalse(responder.pageMatchesQuery(page, attributes, suites, false));

    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    setupRequestInputAndPageProperty("Suite", attributes, false, page, null);
    assertTrue(responder.pageMatchesQuery(page, attributes, suites, false));

    setupRequestInputAndPageProperty("Test", attributes, false, page, "true");
    setupRequestInputAndPageProperty("Suite", attributes, false, page, null);
    assertFalse(responder.pageMatchesQuery(page, attributes, suites, false));

    setupRequestInputAndPageProperty("Test", attributes, false, page, null);
    setupRequestInputAndPageProperty("Suite", attributes, false, page, "true");
    assertFalse(responder.pageMatchesQuery(page, attributes, suites, false));
  }

  public void testPageMatchesQueryWithExcludedPages() throws Exception {
    Map<String, Boolean> attributes = new HashMap<String, Boolean>();
    String[] suites = new String[0];

    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(responder.pageMatchesQuery(page, attributes, suites, true));

    page = crawler.addPage(root, PathParser.parse("SetUp"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(responder.pageMatchesQuery(page, attributes, suites, false));
    assertFalse(responder.pageMatchesQuery(page, attributes, suites, true));

    page = crawler.addPage(root, PathParser.parse("TearDown"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(responder.pageMatchesQuery(page, attributes, suites, false));
    assertFalse(responder.pageMatchesQuery(page, attributes, suites, true));

    page = crawler.addPage(root, PathParser.parse("SuiteSetUp"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(responder.pageMatchesQuery(page, attributes, suites, false));
    assertFalse(responder.pageMatchesQuery(page, attributes, suites, true));

    page = crawler.addPage(root, PathParser.parse("SuiteTearDown"));
    setupRequestInputAndPageProperty("Test", attributes, true, page, "true");
    assertTrue(responder.pageMatchesQuery(page, attributes, suites, false));
    assertFalse(responder.pageMatchesQuery(page, attributes, suites, true));
  }

  public void testPageMatchQueryWithSuites() throws Exception {
    Map<String, Boolean> requestInputs = new HashMap<String, Boolean>();
    assertTrue(responder.pageMatchesQuery(page, requestInputs, null, false));
    assertTrue(responder.pageMatchesQuery(page, requestInputs, new String[0], false));

    String[] suites = new String[]{"SuiteTest"};
    assertFalse(responder.pageMatchesQuery(page, requestInputs, suites, false));

    setUpSuitesProperty(page, "SuiteTest");
    assertTrue(responder.pageMatchesQuery(page, requestInputs, null, false));
    assertFalse(responder.pageMatchesQuery(page, requestInputs, new String[0], false));
    assertTrue(responder.pageMatchesQuery(page, requestInputs, suites, false));

    setUpSuitesProperty(page, "SuiteTest, SuiteTest2");
    assertTrue(responder.pageMatchesQuery(page, requestInputs, suites, false));

    setUpSuitesProperty(page, "SuiteTest2 , SuiteTest3");
    assertFalse(responder.pageMatchesQuery(page, requestInputs, suites, false));

    suites = new String[]{"SuiteTest2", "SuiteTest3"};
    assertTrue(responder.pageMatchesQuery(page, requestInputs, suites, false));

    setUpSuitesProperty(page, "SuiteTest, SuiteTest2");
    assertFalse(responder.pageMatchesQuery(page, requestInputs, suites, false));
  }

  private void setUpSuitesProperty(WikiPage page, String value) throws Exception {
    PageData data = page.getData();
    data.getProperties().set("Suites", value);
    page.commit(data);
  }

  private void setupRequestInputAndPageProperty(String attributeName,
                                                Map<String, Boolean> requestInputs, boolean requestValue,
                                                WikiPage page, String pageDataValue) throws Exception {
    requestInputs.put(attributeName, requestValue);

    PageData pageData = page.getData();
    if (pageDataValue == null)
      pageData.getProperties().remove(attributeName);
    else
      pageData.getProperties().set(attributeName, pageDataValue);
    page.commit(pageData);
  }

  public void testCheckAttributeValue() {
    assertTrue(responder.attributeMatchesInput(false, false));
    assertTrue(responder.attributeMatchesInput(true, true));
    assertFalse(responder.attributeMatchesInput(false, true));
    assertFalse(responder.attributeMatchesInput(true, false));
  }
}
