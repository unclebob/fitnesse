// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.editing;

import fitnesse.*;
import fitnesse.http.*;
import fitnesse.responders.WikiImportProperty;
import fitnesse.testutil.RegexTest;
import fitnesse.wiki.*;

public class PropertiesResponderTest extends RegexTest
{
	private WikiPage root;

	private PageCrawler crawler;

	private MockRequest request;

	private Responder responder;

	private String content;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
	}

	public void testResponse() throws Exception
	{
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

		assertSubString("<input type=\"checkbox\" name=\"Test\" checked=\"true\"/>", content);
		assertSubString("<input type=\"checkbox\" name=\"Search\" checked=\"true\"/>", content);
		assertSubString("<input type=\"checkbox\" name=\"Edit\" checked=\"true\"/>", content);
		assertSubString("<input type=\"checkbox\" name=\"Properties\" checked=\"true\"/>", content);
		assertSubString("<input type=\"checkbox\" name=\"Suite\"/>", content);
		assertSubString("<input type=\"checkbox\" name=\"Versions\" checked=\"true\"/>", content);
		assertSubString("<input type=\"checkbox\" name=\"Refactor\" checked=\"true\"/>", content);
		assertSubString("<input type=\"checkbox\" name=\"WhereUsed\" checked=\"true\"/>", content);
		assertSubString("<input type=\"checkbox\" name=\"RecentChanges\" checked=\"true\"/>", content);

		assertSubString("<input type=\"checkbox\" name=\"" + WikiPage.SECURE_READ + "\"/>", content);
		assertSubString("<input type=\"checkbox\" name=\"" + WikiPage.SECURE_WRITE + "\"/>", content);
		assertSubString("<input type=\"checkbox\" name=\"" + WikiPage.SECURE_TEST + "\"/>", content);
	}

	public void testGetVirtualWikiValue() throws Exception
	{
		WikiPage page = crawler.addPage(root, PathParser.parse("PageOne"));
		PageData data = page.getData();

		assertEquals("", PropertiesResponder.getVirtualWikiValue(data));

		data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, "http://www.objectmentor.com");
		assertEquals("http://www.objectmentor.com", PropertiesResponder.getVirtualWikiValue(data));
	}

	public void testUsernameDisplayed() throws Exception
	{
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

	private WikiPage getContentFromSimplePropertiesPage() throws Exception
	{
		WikiPage page = root.addChildPage("SomePage");

		return getPropertiesContentFromPage(page);
	}

	private WikiPage getPropertiesContentFromPage(WikiPage page) throws Exception
	{
		request = new MockRequest();
		request.setResource(page.getName());
		responder = new PropertiesResponder();
		SimpleResponse response = (SimpleResponse) responder.makeResponse(new FitNesseContext(root), request);
		content = response.getContent();
		return page;
	}

	public void testWikiImportForm() throws Exception
	{
		getContentFromSimplePropertiesPage();

		checkUpdateForm();
		assertSubString("Wiki Import.", content);
		assertSubString("value=\"Import\"", content);
		assertSubString("type=\"text\"", content);
		assertSubString("name=\"remoteUrl\"", content);
	}

	private void checkUpdateForm()
	{
		assertSubString("<form", content);
		assertSubString("action=\"SomePage#end\"", content);
		assertSubString("<input", content);
		assertSubString("type=\"hidden\"", content);
		assertSubString("name=\"responder\"", content);
		assertSubString("value=\"import\"", content);
	}

	public void testWikiImportUpdate() throws Exception
	{
		WikiImportProperty property = new WikiImportProperty("http://my.host.com/PageRoot");
		property.setRoot(true);
		testWikiImportUpdateWith(property);
		assertSubString(" imports its subpages from ", content);
		assertSubString("value=\"Update Subpages\"", content);

		assertSubString("Automatically update imported content when executing tests", content);
	}

	public void testWikiImportUpdateNonroot() throws Exception
	{
		testWikiImportUpdateWith(new WikiImportProperty("http://my.host.com/PageRoot"));
		assertSubString(" imports its content and subpages from ", content);
		assertSubString("value=\"Update Content and Subpages\"", content);

		assertSubString("Automatically update imported content when executing tests", content);
	}

	private void testWikiImportUpdateWith(WikiImportProperty property) throws Exception
	{
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

	public void testSymbolicLinkForm() throws Exception
	{
		getContentFromSimplePropertiesPage();

		assertSubString("Symbolic Links", content);
		assertSubString("<input type=\"hidden\" name=\"responder\" value=\"symlink\"/>", content);
		assertSubString("<input type=\"text\" name=\"linkName\"/>", content);
		assertSubString("<input type=\"text\" name=\"linkPath\"", content);
		assertSubString("<input type=\"submit\" name=\"submit\" value=\"Create Symbolic Link\"/>", content);
	}

	public void testSymbolicLinkListing() throws Exception
	{
		WikiPage page = root.addChildPage("SomePage");
		PageData data = page.getData();
		WikiPageProperties props = data.getProperties();
		WikiPageProperty symProp = props.set(SymbolicPage.PROPERTY_NAME);
		symProp.set("InternalPage", ".PageOne.ChildOne");
		symProp.set("ExternalPage", "file://some/page");
		page.commit(data);

		getPropertiesContentFromPage(page);

		assertSubString("<a href=\".PageOne.ChildOne\">.PageOne.ChildOne</a>", content);
		assertSubString("<td>file://some/page</td>", content);
	}

	public void testActionPropertiesHtml() throws Exception
	{
		WikiPage page = root.addChildPage("SomePage");
		PageData data = page.getData();
		String html = new PropertiesResponder().makeTestActionCheckboxesHtml(data).html();
		assertSubString("<div style=\"float: left; width: 150px;\">Actions:", html);
		assertSubString("Actions:", html);
		assertSubString("<input type=\"checkbox\" name=\"Test\"/> - Test", html);
		assertSubString("<input type=\"checkbox\" name=\"Suite\"/> - Suite", html);
		assertSubString("<input type=\"checkbox\" name=\"Edit\" checked=\"true\"/> - Edit", html);
		assertSubString("<input type=\"checkbox\" name=\"Versions\" checked=\"true\"/> - Versions", html);
		assertSubString("<input type=\"checkbox\" name=\"Properties\" checked=\"true\"/> - Properties", html);
		assertSubString("<input type=\"checkbox\" name=\"Refactor\" checked=\"true\"/> - Refactor", html);
		assertSubString("<input type=\"checkbox\" name=\"WhereUsed\" checked=\"true\"/> - WhereUsed", html);
	}

	public void testMakeNavigationPropertiesHtml() throws Exception
	{
		WikiPage page = root.addChildPage("SomePage");
		PageData data = page.getData();
		String html = new PropertiesResponder().makeNavigationCheckboxesHtml(data).html();
		assertSubString("<div style=\"float: left; width: 150px;\">Navigation:", html);
		assertSubString("<input type=\"checkbox\" name=\"Files\" checked=\"true\"/> - Files", html);
		assertSubString("<input type=\"checkbox\" name=\"RecentChanges\" checked=\"true\"/> - RecentChanges", html);
		assertSubString("<input type=\"checkbox\" name=\"Search\" checked=\"true\"/> - Search", html);
	}

	public void testMakeSecurityPropertiesHtml() throws Exception
	{
		WikiPage page = root.addChildPage("SomePage");
		PageData data = page.getData();
		String html = new PropertiesResponder().makeSecurityCheckboxesHtml(data).html();
		assertSubString("<div style=\"float: left; width: 150px;\">Security:", html);
		assertSubString("<input type=\"checkbox\" name=\"secure-read\"/> - secure-read", html);
		assertSubString("<input type=\"checkbox\" name=\"secure-write\"/> - secure-write", html);
		assertSubString("<input type=\"checkbox\" name=\"secure-test\"/> - secure-test", html);
	}

	public void testEmptySuitesForm() throws Exception
	{
		getContentFromSimplePropertiesPage();

		assertSubString("Suites", content);
		assertSubString("<input type=\"text\" name=\"Suites\" value=\"\"/>", content);
	}

	public void testSuitesDisplayed() throws Exception
	{
		WikiPage page = getContentFromSimplePropertiesPage();
		PageData data = page.getData();
		data.setAttribute(PropertiesResponder.SUITES, "smoke");
		page.commit(data);

		getPropertiesContentFromPage(page);

		assertSubString("Suites", content);
		assertSubString("<input type=\"text\" name=\"Suites\" value=\"smoke\"/>", content);
	}
}
