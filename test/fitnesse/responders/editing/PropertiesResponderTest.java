// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;

import static fitnesse.wiki.PageData.PropertyHELP;
import static fitnesse.wiki.PageData.PropertySUITES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.RegexTestCase.assertDoesntHaveRegexp;
import static util.RegexTestCase.assertHasRegexp;
import static util.RegexTestCase.assertMatches;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

public class PropertiesResponderTest {
  private FitNesseContext context;

  private WikiPage root;

  private MockRequest request;

  private Responder responder;

  private String content;

  @Before
  public void setUp() throws Exception {
    context = FitNesseUtil.makeTestContext();
    root = context.getRootPage();
    request = new MockRequest();
  }

  @Test
  public void testResponse() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), "");
    PageData data = page.getData();
    data.setContent("some content");
    WikiPageProperty properties = data.getProperties();
    properties.set(PageData.PropertySUITES, "Page Tags");
    properties.set("Test", "true");
    page.commit(data);

    MockRequest request = new MockRequest();
    request.setResource("PageOne");

    Responder responder = new PropertiesResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals("max-age=0", response.getHeader("Cache-Control"));

    String content = response.getContent();
    assertSubString("PageOne", content);
    assertDoesntHaveRegexp("textarea name=\"extensionXml\"", content);
    assertHasRegexp("<input.*value=\"Save Properties\".*>", content);

    assertHasRegexp("<input.*value=\"saveProperties\"", content);
    assertSubString(" value=\"Page Tags\" ", content);
    for (String attribute : new String[]{"Search", "Edit", "Properties", "Versions", "Refactor", "WhereUsed", "RecentChanges"})
      assertCheckboxChecked(attribute, content);

    for (String attribute : new String[]{"Prune", WikiPageProperty.SECURE_READ, WikiPageProperty.SECURE_WRITE, WikiPageProperty.SECURE_TEST})
      assertCheckboxNotChecked(content, attribute);
  }

  private void assertCheckboxNotChecked(String content, String attribute) {
    assertSubString("<input type=\"checkbox\" id=\"" + attribute + "\" name=\"" + attribute + "\"/>", content);
  }

  private void assertCheckboxChecked(String attribute, String content) {
    assertSubString("<input type=\"checkbox\" id=\"" + attribute + "\" name=\"" + attribute + "\" checked=\"checked\"/>", content);
  }

  @Test
  public void testJsonResponse() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("PageOne"));
    PageData data = page.getData();
    WikiPageProperty properties = data.getProperties();
    properties.set(PageType.TEST.toString(), "true");
    page.commit(data);

    MockRequest request = new MockRequest();
    request.setResource("PageOne");
    request.addInput("format", "json");

    Responder responder = new PropertiesResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals("application/json", response.getContentType());
    String jsonText = response.getContent();
    JSONObject jsonObject = new JSONObject(jsonText);
    assertTrue(jsonObject.getBoolean("Test"));
    assertTrue(jsonObject.getBoolean("Search"));
    assertTrue(jsonObject.getBoolean("Edit"));
    assertTrue(jsonObject.getBoolean("Properties"));
    assertTrue(jsonObject.getBoolean("Versions"));
    assertTrue(jsonObject.getBoolean("Refactor"));
    assertTrue(jsonObject.getBoolean("WhereUsed"));
    assertTrue(jsonObject.getBoolean("RecentChanges"));
    assertTrue(jsonObject.getBoolean("Files"));

    assertFalse(jsonObject.has("Help"));
    assertFalse(jsonObject.getBoolean("Suite"));
    assertFalse(jsonObject.getBoolean("Prune"));
    assertFalse(jsonObject.getBoolean(WikiImportProperty.SECURE_READ));
    assertFalse(jsonObject.getBoolean(WikiImportProperty.SECURE_WRITE));
    assertFalse(jsonObject.getBoolean(WikiImportProperty.SECURE_TEST));
  }

  @Test
  public void testJsonResponseWithHelpTextAndTags() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("PageOne"));
    PageData data = page.getData();
    WikiPageProperty properties = data.getProperties();
    properties.set(WikiPageProperty.HELP, "help text");
    properties.set(WikiPageProperty.SUITES, "foo,bar");
    page.commit(data);

    MockRequest request = new MockRequest();
    request.setResource("PageOne");
    request.addInput("format", "json");

    Responder responder = new PropertiesResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    assertEquals("application/json", response.getContentType());
    String jsonText = response.getContent();
    JSONObject jsonObject = new JSONObject(jsonText);

    assertEquals("help text", jsonObject.getString("Help"));
    JSONArray suites = jsonObject.getJSONArray("Suites");
    assertEquals("foo", suites.getString(0));
    assertEquals("bar", suites.getString(1));

    assertFalse(jsonObject.getBoolean("Test"));
    assertFalse(jsonObject.getBoolean("Suite"));
    assertFalse(jsonObject.getBoolean("Prune"));
    assertFalse(jsonObject.getBoolean(WikiPageProperty.SECURE_READ));
    assertFalse(jsonObject.getBoolean(WikiPageProperty.SECURE_WRITE));
    assertFalse(jsonObject.getBoolean(WikiPageProperty.SECURE_TEST));
  }


  @Test
  public void testUsernameDisplayed() throws Exception {
    WikiPage page = getContentFromSimplePropertiesPage();

    assertSubString("Last modified anonymously", content);

    PageData data = page.getData();
    data.setAttribute(PageData.LAST_MODIFYING_USER, "Bill");
    page.commit(data);

    request.setResource("SomePage");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    content = response.getContent();

    assertSubString("Last modified by Bill", content);
  }

  private WikiPage getContentFromSimplePropertiesPage() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SomePage"));

    return getPropertiesContentFromPage(page);
  }

  private WikiPage getPropertiesContentFromPage(WikiPage page) throws Exception {
    request = new MockRequest();
    request.setResource(page.getName());
    responder = new PropertiesResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
    content = response.getContent();
    return page;
  }

  @Test
  public void testWikiImportForm() throws Exception {
    getContentFromSimplePropertiesPage();

    checkUpdateForm();
    assertSubString("Wiki Import", content);
    assertSubString("value=\"Import\"", content);
    assertSubString("type=\"text\"", content);
    assertSubString("name=\"remoteUrl\"", content);
  }

  private void checkUpdateForm() {
    assertSubString("<form", content);
    assertSubString("action=\"\"", content);
    assertSubString("<input", content);
    assertSubString("type=\"hidden\"", content);
    assertSubString("name=\"responder\"", content);
    assertSubString("value=\"import\"", content);
  }

  @Test
  public void testWikiImportUpdate() throws Exception {
    WikiImportProperty property = new WikiImportProperty("http://my.host.com/PageRoot");
    property.setRoot(true);
    testWikiImportUpdateWith(property);
    assertSubString("imports its subpages from", content);
    assertSubString("value=\"Update Subpages\"", content);

    assertSubString("Automatically update imported content when executing tests", content);
  }

  @Test
  public void testWikiImportUpdateNonroot() throws Exception {
    testWikiImportUpdateWith(new WikiImportProperty("http://my.host.com/PageRoot"));
    assertSubString("imports its content and subpages from", content);
    assertSubString("value=\"Update Content and Subpages\"", content);

    assertSubString("Automatically update imported content when executing tests", content);
  }

  private void testWikiImportUpdateWith(WikiImportProperty property) throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SomePage"));
    PageData data = page.getData();
    property.addTo(data.getProperties());
    page.commit(data);

    getPropertiesContentFromPage(page);
    checkUpdateForm();
    assertSubString("Wiki Import Update", content);
    assertSubString("<a href=\"http://my.host.com/PageRoot\">http://my.host.com/PageRoot</a>", content);

    assertNotSubString("value=\"Import\"", content);
  }

  @Test
  public void testSymbolicLinkForm() throws Exception {
    getContentFromSimplePropertiesPage();

    assertSubString("Symbolic Links", content);
    assertSubString("<input type=\"hidden\" name=\"responder\" value=\"symlink\"", content);
    assertSubString("<input type=\"text\" name=\"linkName\"", content);
    assertSubString("<input type=\"text\" name=\"linkPath\"", content);
    assertSubString("<input type=\"submit\" name=\"submit\" value=\"Create/Replace\"", content);
  }

  @Test
  public void testSymbolicLinkListing() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SomePage"), "");
    WikiPageUtil.addPage(page, PathParser.parse("SomeChild"), "");
    WikiPage pageOne = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), ""); //...page must exist!
    WikiPageUtil.addPage(pageOne, PathParser.parse("ChildOne"), "");                //...page must exist!

    PageData data = page.getData();
    WikiPageProperty props = data.getProperties();
    WikiPageProperty symProp = props.set(SymbolicPage.PROPERTY_NAME);
    symProp.set("InternalAbsPage", ".PageOne.ChildOne");
    symProp.set("InternalRelPage", "PageOne.ChildOne");
    symProp.set("InternalSubPage", ">SomeChild");
    symProp.set("ExternalPage", "file://some/page");
    page.commit(data);

    getPropertiesContentFromPage(page);

    assertSubString("<input type=\"text\" name=\"InternalAbsPage\"", content);
    assertSubString("<a href=\".PageOne.ChildOne\">.PageOne.ChildOne</a>", content);
    assertMatches("<a href=\".*\">Rename</a>", content);

    assertSubString("<input type=\"text\" name=\"InternalRelPage\"", content);
    assertSubString("<a href=\".PageOne.ChildOne\">PageOne.ChildOne</a>", content);

    assertSubString("<input type=\"text\" name=\"InternalSubPage\"", content);
    assertSubString("<a href=\".SomePage.SomeChild\">&gt;SomeChild</a>", content);

    assertHasRegexp("<td>\\W*file://some/page\\W*</td>", content);
  }

  @Test
  public void testSymbolicLinkListingForBackwardPath() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SomePage"), "");
    WikiPage child = WikiPageUtil.addPage(page, PathParser.parse("SomeChild"), "");
    WikiPageUtil.addPage(page, PathParser.parse("OtherChild"), "");

    PageData data = child.getData();
    WikiPageProperty props = data.getProperties();
    WikiPageProperty symProp = props.set(SymbolicPage.PROPERTY_NAME);
    symProp.set("InternalBackPage", "<SomePage.OtherChild");
    page.commit(data);

    getPropertiesContentFromPage(page);

    assertSubString("InternalBackPage", content);
    assertSubString("<a href=\".SomePage.OtherChild\">&lt;SomePage.OtherChild</a>", content);
  }

  @Test
  public void testPageTypePropertiesHtml() throws Exception {
    SimpleResponse response = (SimpleResponse) new PropertiesResponder().makeResponse(context, request);
    String html = response.getContent();
    assertSubString("Page type:", html);
    assertSubString("<input type=\"radio\" id=\"Static\" name=\"PageType\" value=\"Static\" checked=\"checked\"/>", html);
    assertSubString("<input type=\"radio\" id=\"Test\" name=\"PageType\" value=\"Test\"/>", html);
    assertSubString("<input type=\"radio\" id=\"Suite\" name=\"PageType\" value=\"Suite\"/>", html);
    assertSubString("<input type=\"checkbox\" id=\"Prune\" name=\"Prune\"/>", html);
  }

  @Test
  public void testPageTypePropertiesSuiteHtml() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SomePage"));
    PageData data = page.getData();
    data.setAttribute("Suite");
    page.commit(data);
    assertEquals(page, context.getRootPage().getPageCrawler().getPage(PathParser.parse(".SomePage")));
    request.setResource(page.getPageCrawler().getFullPath().toString());
    SimpleResponse response = (SimpleResponse) new PropertiesResponder().makeResponse(context, request);
    String html = response.getContent();
    assertSubString("Page type:", html);
    assertSubString("<input type=\"radio\" id=\"Static\" name=\"PageType\" value=\"Static\"/>", html);
    assertSubString("<input type=\"radio\" id=\"Test\" name=\"PageType\" value=\"Test\"/>", html);
    assertSubString("<input type=\"radio\" id=\"Suite\" name=\"PageType\" value=\"Suite\" checked=\"checked\"/>", html);
    assertSubString("<input type=\"checkbox\" id=\"Prune\" name=\"Prune\"/>", html);
  }

  @Test
  public void testPageTypePropertiesTestHtml() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SomePage"));
    PageData data = page.getData();
    data.setAttribute("Test");
    page.commit(data);
    request.setResource(page.getPageCrawler().getFullPath().toString());
    SimpleResponse response = (SimpleResponse) new PropertiesResponder().makeResponse(context, request);
    String html = response.getContent();
    assertSubString("Page type:", html);
    assertSubString("<input type=\"radio\" id=\"Static\" name=\"PageType\" value=\"Static\"/>", html);
    assertSubString("<input type=\"radio\" id=\"Test\" name=\"PageType\" value=\"Test\" checked=\"checked\"/>", html);
    assertSubString("<input type=\"radio\" id=\"Suite\" name=\"PageType\" value=\"Suite\"/>", html);
    assertSubString("<input type=\"checkbox\" id=\"Prune\" name=\"Prune\"/>", html);
  }

  @Test
  public void testPageTypePropertiesSkippedHtml() throws Exception {
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("SomePage"));
    PageData data = page.getData();
    data.setAttribute("Prune");
    page.commit(data);
    request.setResource(page.getPageCrawler().getFullPath().toString());
    SimpleResponse response = (SimpleResponse) new PropertiesResponder().makeResponse(context, request);
    String html = response.getContent();
    assertSubString("Page type:", html);
    assertSubString("<input type=\"checkbox\" id=\"Prune\" name=\"Prune\" checked=\"checked\"/>", html);
  }

  @Test
  public void testActionPropertiesHtml() throws Exception {
    SimpleResponse response = (SimpleResponse) new PropertiesResponder().makeResponse(context, request);
    String html = response.getContent();
    assertSubString("Actions:", html);
    assertSubString("<input type=\"checkbox\" id=\"Edit\" name=\"Edit\" checked=\"checked\"/>", html);
    assertSubString("<input type=\"checkbox\" id=\"Versions\" name=\"Versions\" checked=\"checked\"/>", html);
    assertSubString("<input type=\"checkbox\" id=\"Properties\" name=\"Properties\" checked=\"checked\"/>", html);
    assertSubString("<input type=\"checkbox\" id=\"Refactor\" name=\"Refactor\" checked=\"checked\"/>", html);
    assertSubString("<input type=\"checkbox\" id=\"WhereUsed\" name=\"WhereUsed\" checked=\"checked\"/>", html);
  }

  @Test
  public void testMakeNavigationPropertiesHtml() throws Exception {
    SimpleResponse response = (SimpleResponse) new PropertiesResponder().makeResponse(context, request);
    String html = response.getContent();
    assertSubString("Navigation:", html);
    assertSubString("<input type=\"checkbox\" id=\"Files\" name=\"Files\" checked=\"checked\"/>", html);
    assertSubString("<input type=\"checkbox\" id=\"RecentChanges\" name=\"RecentChanges\" checked=\"checked\"/>", html);
    assertSubString("<input type=\"checkbox\" id=\"Search\" name=\"Search\" checked=\"checked\"/>", html);
  }

  @Test
  public void testMakeSecurityPropertiesHtml() throws Exception {
    SimpleResponse response = (SimpleResponse) new PropertiesResponder().makeResponse(context, request);
    String html = response.getContent();
    assertSubString("Security:", html);
    assertSubString("<input type=\"checkbox\" id=\"secure-read\" name=\"secure-read\"/>", html);
    assertSubString("<input type=\"checkbox\" id=\"secure-write\" name=\"secure-write\"/>", html);
    assertSubString("<input type=\"checkbox\" id=\"secure-test\" name=\"secure-test\"/>", html);
  }

  @Test
  public void testEmptySuitesForm() throws Exception {
    getContentFromSimplePropertiesPage();

    assertSubString("Suites", content);
    assertSubString("<input type=\"text\" id=\"Suites\" title=\"Separate tags by a comma\" name=\"Suites\" value=\"\"", content);
  }

  @Test
  public void testSuitesDisplayed() throws Exception {
    WikiPage page = getContentFromSimplePropertiesPage();
    PageData data = page.getData();
    data.setAttribute(WikiPageProperty.SUITES, "smoke");
    page.commit(data);

    getPropertiesContentFromPage(page);

    assertSubString("Suites", content);
    assertSubString("<input type=\"text\" id=\"Suites\" title=\"Separate tags by a comma\" name=\"Suites\" value=\"smoke\"", content);
    assertSubString(" value=\"smoke\" ", content);
  }
}
