// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.editing;

import fitnesse.*;
import fitnesse.testutil.RegexTest;
import fitnesse.wiki.*;
import fitnesse.http.*;

public class PropertiesResponderTest extends RegexTest
{
	private WikiPage root;
	private PageCrawler crawler;
	private MockRequest request;
	private Responder responder;
	private SimpleResponse response;
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
		SimpleResponse response = (SimpleResponse)responder.makeResponse(new FitNesseContext(root), request);
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
		response = (SimpleResponse)responder.makeResponse(new FitNesseContext(root), request);
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
		response = (SimpleResponse)responder.makeResponse(new FitNesseContext(root), request);
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
		testWikiImportUpdateWith("WikiImportRoot");
		assertSubString(" imports its subpages from ", content);
		assertSubString("value=\"Update Subpages\"", content);
	}

	public void testWikiImportUpdateNonroot() throws Exception
	{
		testWikiImportUpdateWith("WikiImportSource");
		assertSubString(" imports its content and subpages from ", content);
		assertSubString("value=\"Update Content and Subpages\"", content);
	}

	private void testWikiImportUpdateWith(String propertyName) throws Exception
	{
		WikiPage page = root.addChildPage("SomePage");
		PageData data = page.getData();
		data.setAttribute(propertyName, "http://my.host.com/PageRoot");
		page.commit(data);

		getPropertiesContentFromPage(page);
		checkUpdateForm();
		assertSubString("Wiki Import Update.", content);
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

	public void testSybolicLinkList() throws Exception
	{
		WikiPage page = root.addChildPage("SomePage");
		PageData data = page.getData();
		WikiPageProperties props = data.getProperties();
		props.addSymbolicLink("LinkOne", PathParser.parse("PatH.ToAnother.PagE"));
		props.addSymbolicLink("LinkTwo", PathParser.parse("PatH.ToYetAnother.PagE"));
		page.commit(data);

		getPropertiesContentFromPage(page);

		assertSubString("LinkOne", content);
		assertSubString("<a href=\"SomePage?responder=symlink&removal=LinkOne\">remove</a>", content);
		assertSubString("LinkTwo", content);
		assertSubString("<a href=\"SomePage?responder=symlink&removal=LinkTwo\">remove</a>", content);
	}
}
