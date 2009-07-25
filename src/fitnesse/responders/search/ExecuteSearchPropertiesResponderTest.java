package fitnesse.responders.search;

import static fitnesse.wiki.PageData.*;
import static fitnesse.responders.search.ExecuteSearchPropertiesResponder.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import util.RegexTestCase;
import util.StringUtil;
import fitnesse.FitNesseContext;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
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
    FitNesseUtil.makeTestContext(root);
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
    String[] titles = { "Page", "Test", "PageOne"};

    assertOutputHasRowWithLink(content, titles);

    request.addInput("Suites", "filter1");

    content = invokeResponder(request);

    assertHasRegexp("Found 1 result for your search", content);
    String[] titles1 = { "Page", "Test", "Tags", "PageOne" };
    assertOutputHasRowWithLink(content, titles1);
    assertOutputHasRowWithLabels("filter1,filter2");
  }

  private String invokeResponder(MockRequest request) throws Exception {
    Response response = responder.makeResponse(new FitNesseContext(root), request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    return sender.sentData();
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
    request.addInput("Special", "Any");
    return request;
  }

  private void assertOutputHasRowWithLink(String content, String... titles) {
    for (String title : titles) {
      assertOutputHasRow(content, title, "a href.*");
    }
  }

  private void assertOutputHasRowWithLabels(String content, String... labels) {
    for (String label : labels) {
      assertOutputHasRow(content, label, "label");
    }
  }

  private void assertOutputHasRow(String content, String title, String tagName) {
    assertHasRegexp("<table.*<tr.*<td.*<" + tagName + ">" + title + "</"
        + tagName.split(" ")[0] + ">", content);
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
    request
    .addInput(PAGE_TYPE, StringUtil.join(types, ","));
    assertEquals(types, responder.getPageTypesFromInput(request));
  }

  public void testGetAttributesFromInput() {
    MockRequest request = new MockRequest();
    request.addInput(ACTION, "Edit");

    Map<String, Boolean> foundAttributes = responder.getAttributesFromInput(request);
    assertFalse(foundAttributes.containsKey("Version"));
    assertTrue(foundAttributes.containsKey("Edit"));
    assertTrue(foundAttributes.get("Edit"));

    request.addInput(ACTION, "Edit,Properties");
    foundAttributes = responder.getAttributesFromInput(request);
    assertTrue(foundAttributes.get("Properties"));
  }

  public void testPageTypesAreOrEd() throws Exception {
    MockRequest request = setupRequest();
    request.addInput(PAGE_TYPE, "Test,Suite");

    String content = invokeResponder(request);
    String[] titles = { "Page", "Test", "PageOne" };

    assertOutputHasRowWithLink(content, titles);

    request.addInput("Suites", "filter1");

    content = invokeResponder(request);

    assertHasRegexp("Found 1 result for your search", content);
    String[] titles1 = { "Page", "Test", "Tags", "PageOne" };
    assertOutputHasRowWithLink(content, titles1);
    assertOutputHasRowWithLabels(content, "filter1,filter2");
  }

  public void testPageMatchesWithObsoletePages() throws Exception {
    MockRequest request = setupRequestForObsoletePage();
    request.addInput(PAGE_TYPE, "Test,Suite");

    String content = invokeResponder(request);
    String[] titles = { "Page", "Test", "ObsoletePage" };

    assertOutputHasRowWithLink(content, titles);

    request.addInput(SPECIAL, "SetUp,TearDown");

    content = invokeResponder(request);

    assertSubString("No pages", content);
  }

  private MockRequest setupRequestForObsoletePage() throws Exception {
    WikiPage page = crawler.addPage(root, PathParser.parse("ObsoletePage"));
    PageData data = page.getData();
    data.setContent("some content");
    WikiPageProperties properties1 = data.getProperties();
    properties1.set("Test", "true");
    properties1.set("Suites", "filter1,filter2");
    WikiPageProperties properties = properties1;
    properties.set(PropertyPRUNE, "true");
    page.commit(data);

    MockRequest request = setupRequest();
    request.setResource("ObsoletePage");
    return request;
  }

  public void testFindJustObsoletePages() throws Exception {
    MockRequest request = setupRequestForObsoletePage();
    request.addInput(PAGE_TYPE, "Test,Suite,Normal");
    request.addInput(SPECIAL, "obsolete");

    String content = invokeResponder(request);
    String[] titles = { "ObsoletePage" };

    assertOutputHasRowWithLink(content, titles);


  }
}
