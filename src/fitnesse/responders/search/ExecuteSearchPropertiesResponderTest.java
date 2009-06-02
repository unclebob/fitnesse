package fitnesse.responders.search;

import static fitnesse.responders.search.SearchFormResponder.PAGE_TYPE;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import util.RegexTestCase;
import util.StringUtil;
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
  private ExecuteSearchPropertiesResponder responder;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
    responder = new ExecuteSearchPropertiesResponder();
  }

  public void testResponseWithNoParametersWillReturnEmptyPage()
  throws Exception {
    MockRequest request = setupRequest();
    String content = invokeResponder(request);
    assertSubString("Search Page Properties Results", content);
    assertSubString("No search properties", content);
  }

  public void testResponseWithNoMatchesWillReturnEmptyPageList()
  throws Exception {
    MockRequest request = setupRequest();
    request.addInput(PAGE_TYPE, "Suite,Normal");

    String content = invokeResponder(request);

    assertSubString("No pages", content);
  }

  public void testResponseWithMatchesWillReturnPageList() throws Exception {
    MockRequest request = setupRequest();
    request.addInput(PAGE_TYPE, "Test");

    String content = invokeResponder(request);

    assertOutputHasHeaderRowWithTitles(content, "Page", "Test");
    assertOutputHasRowWithLabels(content, "PageOne");

    request.addInput("Suites", "filter1");

    content = invokeResponder(request);

    assertHasRegexp("Number of pages.*: 1", content);
    assertOutputHasHeaderRowWithTitles(content, "Page", "Test", "Tags");
    assertOutputHasRowWithLabels(content, "PageOne", "filter1,filter2");
  }

  private String invokeResponder(MockRequest request) throws Exception {
    SimpleResponse response = (SimpleResponse) responder.makeResponse(
        new FitNesseContext(root), request);
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
    request.addInput("Action", "Any");
    request.addInput("Security", "Any");
    return request;
  }

  private void assertOutputHasHeaderRowWithTitles(String content,
      String... titles) {
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
    assertHasRegexp("<table.*<tr.*<td.*<" + tagName + ">" + title + "</"
        + tagName + ">", content);
  }

  public void testGetPageTypesFromInput() {
    assertPageTypesMatch("Test");
    assertPageTypesMatch("Test", "Normal");
    assertPageTypesMatch("Test", "Suite", "Normal");
    assertPageTypesMatch("");
  }

  private void assertPageTypesMatch(String... pageTypes) {
    MockRequest request = new MockRequest();
    List<String> types = Arrays.asList(pageTypes);
    request.addInput(SearchFormResponder.PAGE_TYPE, StringUtil.join(types, ","));
    assertEquals(types, responder.getPageTypesFromInput(request));
  }

  public void testGetAttributesFromInput() {
    MockRequest request = new MockRequest();
    request.addInput(SearchFormResponder.ACTION, "Edit");

    Map<String, Boolean> foundAttributes = responder
    .getAttributesFromInput(request);
    assertFalse(foundAttributes.containsKey("Version"));
    assertTrue(foundAttributes.containsKey("Edit"));
    assertTrue(foundAttributes.get("Edit"));

    request.addInput(SearchFormResponder.ACTION, "Edit,Properties");
    foundAttributes = responder.getAttributesFromInput(request);
    assertTrue(foundAttributes.get("Properties"));
  }

  public void testGetSuitesFromInput() {
    MockRequest request = new MockRequest();

    String[] suites = responder.getSuitesFromInput(request);
    // don't know about this one, yet
    // assertNull(suites);

    request.addInput("Suites", "");
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

  public void testPageTypesAreOrEd() throws Exception {
    MockRequest request = setupRequest();
    request.addInput(PAGE_TYPE, "Test,Suite");

    String content = invokeResponder(request);

    assertOutputHasHeaderRowWithTitles(content, "Page", "Test");
    assertOutputHasRowWithLabels(content, "PageOne");

    request.addInput("Suites", "filter1");

    content = invokeResponder(request);

    assertHasRegexp("Number of pages.*: 1", content);
    assertOutputHasHeaderRowWithTitles(content, "Page", "Test", "Tags");
    assertOutputHasRowWithLabels(content, "PageOne", "filter1,filter2");
  }

  public void testPageMatchesWithObsoletePages() throws Exception {
    MockRequest request = setupRequestForObsoletePage();
    request.addInput(PAGE_TYPE, "Test,Suite");

    String content = invokeResponder(request);

    assertOutputHasHeaderRowWithTitles(content, "Page", "Test");
    assertOutputHasRowWithLabels(content, "ObsoletePage");

    request.addInput("ExcludeObsolete", "on");

    content = invokeResponder(request);

    assertSubString("No pages", content);
  }

  private MockRequest setupRequestForObsoletePage() throws Exception {
    WikiPage page = crawler.addPage(root, PathParser.parse("ObsoletePage"));
    PageData data = page.getData();
    data.setContent("some content");
    WikiPageProperties properties = data.getProperties();
    properties.set("Test", "true");
    properties.set("Suites", "filter1,filter2");
    properties.set("Pruned", "true");
    page.commit(data);

    MockRequest request = new MockRequest();
    request.setResource("ObsoletePage");
    request.addInput("Action", "Any");
    request.addInput("Security", "Any");
    return request;
  }

}
