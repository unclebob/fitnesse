// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import org.json.JSONObject;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.WikiImportProperty;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wiki.WikiPageProperty;

public class PropertiesResponderTest extends RegexTestCase {
  private WikiPage root;

  private PageCrawler crawler;

  private MockRequest request;

  private Responder responder;

  private String content;

  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
    crawler = root.getPageCrawler();
  }

  public void testResponse() throws Exception {
    WikiPage page = crawler.addPage(root, PathParser.parse("PageOne"));
    PageData data = page.getData();
    data.setContent("some content");
    WikiPageProperties properties = data.getProperties();
    properties.set("Test", "true");
    properties.set(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, "http://www.fitnesse.org");
    page.commit(data);

    MockRequest request = new MockRequest();
    request.setResource("PageOne");

    Responder responder = new PropertiesResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
    assertEquals("max-age=0", response.getHeader("Cache-Control"));

    String content = response.getContent();
    assertSubString("PageOne", content);
    assertSubString("value=\"http://www.fitnesse.org\"", content);
    assertDoesntHaveRegexp("textarea name=\"extensionXml\"", content);
    assertHasRegexp("<input.*value=\"Save Properties\".*>", content);

    assertHasRegexp("<input.*value=\"saveProperties\"", content);
    for (String attribute : new String[]{"Search", "Edit", "Properties", "Versions", "Refactor", "WhereUsed", "RecentChanges"})
      assertCheckboxChecked(attribute, content);

    for (String attribute : new String[]{"Prune", WikiPage.SECURE_READ, WikiPage.SECURE_WRITE, WikiPage.SECURE_TEST})
      assertCheckboxNotChecked(content, attribute);
  }

  private void assertCheckboxNotChecked(String content, String attribute) {
    assertSubString("<input type=\"checkbox\" name=\"" + attribute + "\"/>", content);
  }

  private void assertCheckboxChecked(String attribute, String content) {
    assertSubString("<input type=\"checkbox\" name=\"" + attribute + "\" checked=\"true\"/>", content);
  }

  public void testJsonResponse() throws Exception {
    WikiPage page = crawler.addPage(root, PathParser.parse("PageOne"));
    PageData data = page.getData();
    data.setContent("some content");
    WikiPageProperties properties = data.getProperties();
    properties.set("Test", "true");
    page.commit(data);

    MockRequest request = new MockRequest();
    request.setResource("PageOne");
    request.addInput("format", "json");

    Responder responder = new PropertiesResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
    assertEquals("text/json", response.getContentType());
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

    assertFalse(jsonObject.getBoolean("Suite"));
    assertFalse(jsonObject.getBoolean("Prune"));
    assertFalse(jsonObject.getBoolean(WikiPage.SECURE_READ));
    assertFalse(jsonObject.getBoolean(WikiPage.SECURE_WRITE));
    assertFalse(jsonObject.getBoolean(WikiPage.SECURE_TEST));
  }

  public void testGetVirtualWikiValue() throws Exception {
    WikiPage page = crawler.addPage(root, PathParser.parse("PageOne"));
    PageData data = page.getData();

    assertEquals("", PropertiesResponder.getVirtualWikiValue(data));

    data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, "http://www.objectmentor.com");
    assertEquals("http://www.objectmentor.com", PropertiesResponder.getVirtualWikiValue(data));
  }

  public void testUsernameDisplayed() throws Exception {
    WikiPage page = getContentFromSimplePropertiesPage();

    assertSubString("Last modified anonymously", content);

    PageData data = page.getData();
    data.setAttribute(WikiPage.LAST_MODIFYING_USER, "Bill");
    page.commit(data);

    request.setResource("SomePage");
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
    content = response.getContent();

    assertSubString("Last modified by Bill", content);
  }

  private WikiPage getContentFromSimplePropertiesPage() throws Exception {
    WikiPage page = root.addChildPage("SomePage");

    return getPropertiesContentFromPage(page);
  }

  private WikiPage getPropertiesContentFromPage(WikiPage page) throws Exception {
    request = new MockRequest();
    request.setResource(page.getName());
    responder = new PropertiesResponder();
    SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
    content = response.getContent();
    return page;
  }

  public void testWikiImportForm() throws Exception {
    getContentFromSimplePropertiesPage();

    checkUpdateForm();
    assertSubString("Wiki Import.", content);
    assertSubString("value=\"Import\"", content);
    assertSubString("type=\"text\"", content);
    assertSubString("name=\"remoteUrl\"", content);
  }

  private void checkUpdateForm() {
    assertSubString("<form", content);
    assertSubString("action=\"SomePage#end\"", content);
    assertSubString("<input", content);
    assertSubString("type=\"hidden\"", content);
    assertSubString("name=\"responder\"", content);
    assertSubString("value=\"import\"", content);
  }

  public void testWikiImportUpdate() throws Exception {
    WikiImportProperty property = new WikiImportProperty("http://my.host.com/PageRoot");
    property.setRoot(true);
    testWikiImportUpdateWith(property);
    assertSubString(" imports its subpages from ", content);
    assertSubString("value=\"Update Subpages\"", content);

    assertSubString("Automatically update imported content when executing tests", content);
  }

  public void testWikiImportUpdateNonroot() throws Exception {
    testWikiImportUpdateWith(new WikiImportProperty("http://my.host.com/PageRoot"));
    assertSubString(" imports its content and subpages from ", content);
    assertSubString("value=\"Update Content and Subpages\"", content);

    assertSubString("Automatically update imported content when executing tests", content);
  }

  private void testWikiImportUpdateWith(WikiImportProperty property) throws Exception {
    WikiPage page = root.addChildPage("SomePage");
    PageData data = page.getData();
    property.addTo(data.getProperties());
    page.commit(data);

    getPropertiesContentFromPage(page);
    checkUpdateForm();
    assertSubString("Wiki Import Update", content);
    assertSubString("<a href=\"http://my.host.com/PageRoot\">http://my.host.com/PageRoot</a>", content);

    assertNotSubString("value=\"Import\"", content);
  }

  public void testSymbolicLinkForm() throws Exception {
    getContentFromSimplePropertiesPage();

    assertSubString("Symbolic Links", content);
    assertSubString("<input type=\"hidden\" name=\"responder\" value=\"symlink\"", content);
    assertSubString("<input type=\"text\" name=\"linkName\"", content);
    assertSubString("<input type=\"text\" name=\"linkPath\"", content);
    assertSubString("<input type=\"submit\" name=\"submit\" value=\"Create/Replace\"", content);
  }

  public void testSymbolicLinkListing() throws Exception {
    WikiPage page = root.addChildPage("SomePage");
    page.addChildPage("SomeChild");
    WikiPage pageOne = root.addChildPage("PageOne"); //...page must exist!
    pageOne.addChildPage("ChildOne");                //...page must exist!

    PageData data = page.getData();
    WikiPageProperties props = data.getProperties();
    WikiPageProperty symProp = props.set(SymbolicPage.PROPERTY_NAME);
    symProp.set("InternalAbsPage", ".PageOne.ChildOne");
    symProp.set("InternalRelPage", "PageOne.ChildOne");
    symProp.set("InternalSubPage", ">SomeChild");
    symProp.set("ExternalPage", "file://some/page");
    page.commit(data);

    getPropertiesContentFromPage(page);

    assertSubString("<td>InternalAbsPage</td>", content);
    assertSubString("<input type=\"text\" name=\"InternalAbsPage\"", content);
    assertSubString("<a href=\".PageOne.ChildOne\">.PageOne.ChildOne</a>", content);
    assertMatches("<a href=\".*\">&nbsp;Rename:</a>", content);

    assertSubString("<td>InternalRelPage</td>", content);
    assertSubString("<input type=\"text\" name=\"InternalRelPage\"", content);
    assertSubString("<a href=\".PageOne.ChildOne\">PageOne.ChildOne</a>", content);

    assertSubString("<td>InternalSubPage</td>", content);
    assertSubString("<input type=\"text\" name=\"InternalSubPage\"", content);
    assertSubString("<a href=\".SomePage.SomeChild\">&gt;SomeChild</a>", content);

    assertSubString("<td>file://some/page</td>", content);
  }

  public void testSymbolicLinkListingForBackwardPath() throws Exception {
    WikiPage page = root.addChildPage("SomePage");
    WikiPage child = page.addChildPage("SomeChild");
    page.addChildPage("OtherChild");

    PageData data = child.getData();
    WikiPageProperties props = data.getProperties();
    WikiPageProperty symProp = props.set(SymbolicPage.PROPERTY_NAME);
    symProp.set("InternalBackPage", "<SomePage.OtherChild");
    page.commit(data);

    getPropertiesContentFromPage(page);

    assertSubString("InternalBackPage", content);
    assertSubString("<a href=\".SomePage.OtherChild\">&lt;SomePage.OtherChild</a>", content);
  }

  public void testPageTypePropertiesHtml() throws Exception {
    WikiPage page = root.addChildPage("SomePage");
    PageData data = page.getData();
    String html = new PropertiesResponder().makePageTypeRadiosHtml(data).html();
    assertSubString("<div style=\"float: left; width: 150px;\">Page type:", html);
    assertSubString("Page type:", html);
    assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Normal\" checked=\"checked\"/> - Normal", html);
    assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Test\"/> - Test", html);
    assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Suite\"/> - Suite", html);
  }

  public void testPageTypePropertiesSuiteHtml() throws Exception {
    WikiPage page = root.addChildPage("SomePage");
    PageData data = page.getData();
    data.setAttribute("Suite");
    String html = new PropertiesResponder().makePageTypeRadiosHtml(data).html();
    assertSubString("<div style=\"float: left; width: 150px;\">Page type:", html);
    assertSubString("Page type:", html);
    assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Normal\"/> - Normal", html);
    assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Test\"/> - Test", html);
    assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Suite\" checked=\"checked\"/> - Suite", html);
  }

  public void testPageTypePropertiesTestHtml() throws Exception {
    WikiPage page = root.addChildPage("SomePage");
    PageData data = page.getData();
    data.setAttribute("Test");
    String html = new PropertiesResponder().makePageTypeRadiosHtml(data).html();
    assertSubString("<div style=\"float: left; width: 150px;\">Page type:", html);
    assertSubString("Page type:", html);
    assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Normal\"/> - Normal", html);
    assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Test\" checked=\"checked\"/> - Test", html);
    assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Suite\"/> - Suite", html);
  }

  public void testActionPropertiesHtml() throws Exception {
    WikiPage page = root.addChildPage("SomePage");
    PageData data = page.getData();
    String html = new PropertiesResponder().makeTestActionCheckboxesHtml(data).html();
    assertSubString("<div style=\"float: left; width: 180px;\">Actions:", html);
    assertSubString("Actions:", html);
    assertSubString("<input type=\"checkbox\" name=\"Edit\" checked=\"true\"/> - Edit", html);
    assertSubString("<input type=\"checkbox\" name=\"Versions\" checked=\"true\"/> - Versions", html);
    assertSubString("<input type=\"checkbox\" name=\"Properties\" checked=\"true\"/> - Properties", html);
    assertSubString("<input type=\"checkbox\" name=\"Refactor\" checked=\"true\"/> - Refactor", html);
    assertSubString("<input type=\"checkbox\" name=\"WhereUsed\" checked=\"true\"/> - WhereUsed", html);
  }

  public void testMakeNavigationPropertiesHtml() throws Exception {
    WikiPage page = root.addChildPage("SomePage");
    PageData data = page.getData();
    String html = new PropertiesResponder().makeNavigationCheckboxesHtml(data).html();
    assertSubString("<div style=\"float: left; width: 180px;\">Navigation:", html);
    assertSubString("<input type=\"checkbox\" name=\"Files\" checked=\"true\"/> - Files", html);
    assertSubString("<input type=\"checkbox\" name=\"RecentChanges\" checked=\"true\"/> - RecentChanges", html);
    assertSubString("<input type=\"checkbox\" name=\"Search\" checked=\"true\"/> - Search", html);
    assertSubString("<input type=\"checkbox\" name=\"Prune\"/> - Prune", html);
  }

  public void testMakeSecurityPropertiesHtml() throws Exception {
    WikiPage page = root.addChildPage("SomePage");
    PageData data = page.getData();
    String html = new PropertiesResponder().makeSecurityCheckboxesHtml(data).html();
    assertSubString("<div style=\"float: left; width: 180px;\">Security:", html);
    assertSubString("<input type=\"checkbox\" name=\"secure-read\"/> - secure-read", html);
    assertSubString("<input type=\"checkbox\" name=\"secure-write\"/> - secure-write", html);
    assertSubString("<input type=\"checkbox\" name=\"secure-test\"/> - secure-test", html);
  }

  public void testEmptySuitesForm() throws Exception {
    getContentFromSimplePropertiesPage();

    assertSubString("Suites", content);
    assertSubString("<input type=\"text\" name=\"Suites\" value=\"\" size=\"40\"/>", content);
  }

  public void testSuitesDisplayed() throws Exception {
    WikiPage page = getContentFromSimplePropertiesPage();
    PageData data = page.getData();
    data.setAttribute(PageData.PropertySUITES, "smoke");
    page.commit(data);

    getPropertiesContentFromPage(page);

    assertSubString("Suites", content);
    assertSubString("<input type=\"text\" name=\"Suites\" value=\"smoke\" size=\"40\"/>", content);
  }

  public void testEmptyHelpTextForm() throws Exception {
    getContentFromSimplePropertiesPage();

    assertSubString("Help Text", content);
    assertSubString("<input type=\"text\" name=\"HelpText\" value=\"\" size=\"90\"/>", content);
  }

  public void testHelpTextDisplayed() throws Exception {
    WikiPage page = getContentFromSimplePropertiesPage();
    PageData data = page.getData();
    data.setAttribute(PageData.PropertyHELP, "help text");
    page.commit(data);

    getPropertiesContentFromPage(page);

    assertSubString("Help Text", content);
    assertSubString("<input type=\"text\" name=\"HelpText\" value=\"help text\" size=\"90\"/>", content);
  }

}
