package fitnesse.responders.search;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;

import static fitnesse.responders.search.SearchPropertiesResponder.ACTION;
import static fitnesse.responders.search.SearchPropertiesResponder.SPECIAL;
import static fitnesse.wiki.PageData.PAGE_TYPE_ATTRIBUTE;
import static fitnesse.wiki.PageData.PropertyPRUNE;
import static fitnesse.wiki.PageType.STATIC;
import static fitnesse.wiki.PageType.SUITE;
import static fitnesse.wiki.PageType.TEST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertSubString;

public class SearchPropertiesResponderTest {
  private SearchPropertiesResponder responder;
  private FitNesseContext context;

  @Before
  public void setUp() throws Exception {
    responder = new SearchPropertiesResponder();
    context = FitNesseUtil.makeTestContext();
  }

  @Test
  public void testResponseWithNoParametersWillReturnEmptyPage()
  throws Exception {
    MockRequest request = setupRequest();
    String content = invokeResponder(request);
    assertSubString("Search Page Properties Results", content);
    assertSubString("No search properties", content);
  }

  @Test
  public void testResponseWithNoMatchesWillReturnEmptyPageList()
  throws Exception {
    MockRequest request = setupRequest();
    request.addInput(PAGE_TYPE_ATTRIBUTE, "Suite,Static");

    String content = invokeResponder(request);

    assertSubString("No pages", content);
  }

  @Test
  public void testResponseWithMatchesWillReturnPageList() throws Exception {
    MockRequest request = setupRequest();
    request.addInput(PAGE_TYPE_ATTRIBUTE, TEST.toString());

    String content = invokeResponder(request);
    String[] titles = { "Page", TEST.toString(), "PageOne"};

    assertOutputHasRowWithLink(content, titles);

    request.addInput("Suites", "filter1");

    content = invokeResponder(request);

    assertHasRegexp("result for your search", content);
    String[] titles1 = { "Page", TEST.toString(), "Tags", "PageOne" };
    assertOutputHasRowWithLink(content, titles1);
    assertOutputHasRowWithLabels("filter1,filter2");
  }


  private String invokeResponder(MockRequest request) throws Exception {
    Response response = responder.makeResponse(context, request);
    MockResponseSender sender = new MockResponseSender();
    sender.doSending(response);
    return sender.sentData();
  }

  private MockRequest setupRequest() throws Exception {
    WikiPage page = WikiPageUtil.addPage(context.getRootPage(), PathParser.parse("PageOne"));
    PageData data = page.getData();
    data.setContent("some content");
    WikiPageProperty properties = data.getProperties();
    properties.set(TEST.toString(), "true");
    properties.set("Suites", "filter1,filter2");
    page.commit(data);

    MockRequest request = new MockRequest();
    request.setResource("PageOne");
    request.addInput("responder", "searchProperties");
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
    assertHasRegexp("<table.*<tr.*<t[dh][^<]*<" + tagName + "[^<]*>[^>]*" + title + "[^<]*</"
        + tagName.split(" ")[0] + ">", content);
  }

  @Test
  public void testGetPageTypesFromInput() {
    assertPageTypesMatch(TEST);
    assertPageTypesMatch(TEST, STATIC);
    assertPageTypesMatch(TEST, SUITE, STATIC);
    //    assertPageTypesMatch("");
  }

  private void assertPageTypesMatch(PageType... pageTypes) {
    MockRequest request = new MockRequest();
    List<PageType> types = Arrays.asList(pageTypes);
    final String commaSeparatedPageTypes = StringUtils.join(pageTypes, ",");
    request.addInput(PAGE_TYPE_ATTRIBUTE, commaSeparatedPageTypes);
    assertEquals(types, responder.getPageTypesFromInput(request));
  }

  @Test
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

  @Test
  public void testPageTypesAreOrEd() throws Exception {
    MockRequest request = setupRequest();
    request.addInput(PAGE_TYPE_ATTRIBUTE, "Test,Suite");

    String content = invokeResponder(request);
    String[] titles = { "Page", TEST.toString(), "PageOne" };

    assertOutputHasRowWithLink(content, titles);

    request.addInput("Suites", "filter1");

    content = invokeResponder(request);

    assertHasRegexp("result for your search", content);
    String[] titles1 = { "Page", TEST.toString(), "Tags", "PageOne" };
    assertOutputHasRowWithLink(content, titles1);
    assertOutputHasRowWithLabels(content, "filter1,filter2");
  }

  @Test
  public void testPageMatchesWithObsoletePages() throws Exception {
    MockRequest request = setupRequestForObsoletePage();
    request.addInput(PAGE_TYPE_ATTRIBUTE, "Test,Suite");

    String content = invokeResponder(request);
    String[] titles = { "Page", TEST.toString(), "ObsoletePage" };

    assertOutputHasRowWithLink(content, titles);

    request.addInput(SPECIAL, "SetUp,TearDown");

    content = invokeResponder(request);

    assertSubString("No pages", content);
  }

  private MockRequest setupRequestForObsoletePage() throws Exception {
    WikiPage page = WikiPageUtil.addPage(context.getRootPage(), PathParser.parse("ObsoletePage"));
    PageData data = page.getData();
    data.setContent("some content");
    WikiPageProperty properties1 = data.getProperties();
    properties1.set(TEST.toString(), "true");
    properties1.set("Suites", "filter1,filter2");
    WikiPageProperty properties = properties1;
    properties.set(PropertyPRUNE, "true");
    page.commit(data);

    MockRequest request = setupRequest();
    request.setResource("ObsoletePage");
    return request;
  }

  @Test
  public void testFindJustObsoletePages() throws Exception {
    MockRequest request = setupRequestForObsoletePage();
    request.addInput(PAGE_TYPE_ATTRIBUTE, "Test,Suite,Static");
    request.addInput(SPECIAL, "obsolete");

    String content = invokeResponder(request);
    String[] titles = { "ObsoletePage" };

    assertOutputHasRowWithLink(content, titles);

  }
}
