// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;
import junit.framework.TestCase;
import junit.swingui.TestRunner;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WikiWordWidgetTest extends TestCase
{
	private WikiPage root;
	private PageCrawler crawler;

	public void setUp() throws Exception
	{
		root = InMemoryPage.makeRoot("RooT");
		crawler = root.getPageCrawler();
	}

	public void tearDown() throws Exception
	{
	}

	public void testIsSingleWikiWord() throws Exception
	{
		assertTrue(WikiWordWidget.isSingleWikiWord("WikiWord"));
		assertFalse(WikiWordWidget.isSingleWikiWord("notWikiWord"));
		assertFalse(WikiWordWidget.isSingleWikiWord("NotSingle.WikiWord"));
	}

	public void testWikiWordRegexp() throws Exception
	{
		checkWord(true, "WikiWord");
		checkWord(true, "WordWordWord");
		checkWord(false, "HelloDDouble");
		checkWord(true, "RcM");
		checkWord(false, "Hello");
		checkWord(true, "WikiWordWithManyWords");
		checkWord(true, "WidgetRoot.ChildPage");
		checkWord(true, "GrandPa.FatheR.SoN");
		checkWord(true, ".RootPage.ChildPage");
		checkWord(false, "lowerCaseAtStart");
		checkWord(true, "^SubPage");
		checkWord(true, "^SubPage.SubPage");
		checkWord(false, "RcMMdM");
		checkWord(false, "WikiPage.");
	}

	public void testWikiWordsWithSlashNotDot() throws Exception
	{
		checkWord(true, "WikiPage/SubPage");
		checkWord(true, "/WikiPage");
		checkWord(false, "WikiPage/");
		checkWord(true, "./WikiPage");
		checkWord(true, "../WikiPage");
		checkWord(true, "../../WikiPage");

		checkWord(false, "WikiWord/../WikiWord");
		checkWord(false, "./../WikiWord");
		checkWord(false, ".././WikiWord");
		checkWord(false, "WikiWord/./WikiWord");
		checkWord(false, "/../WikiWord");
		checkWord(false, "/./WikiWord");
		checkWord(false, "..");
		checkWord(false, "../..");
	}

	public void testWikiWordRegexpWithDigits() throws Exception
	{
		checkWord(true, "TestPage1");
		checkWord(true, "ParentPage1.SubPage5");
		checkWord(true, "The123Page");
		checkWord(false, "123Page");
		checkWord(false, "Page123");
	}

	public void testHtml() throws Exception
	{
		WikiPage page = crawler.addPage(root, PathParser.parse("PageOne"));
		WikiWordWidget widget = new WikiWordWidget(new WidgetRoot(page), "WikiWord");
		assertEquals("WikiWord<a href=\"WikiWord?edit\">?</a>", widget.render());
		page = crawler.addPage(root, PathParser.parse("WikiWord"));
		widget = new WikiWordWidget(new WidgetRoot(page), "WikiWord");
		assertEquals("<a href=\"WikiWord\">WikiWord</a>", widget.render());
	}

	public void testSubPageWidget() throws Exception
	{
		WikiPage root = InMemoryPage.makeRoot("root");
		WikiPage superPage = crawler.addPage(root, PathParser.parse("SuperPage"));
		PageData data = superPage.getData();
		data.setContent("^SubPage");
		superPage.commit(data);
		String renderedText = superPage.getData().getHtml();
		assertEquals("^SubPage<a href=\"SuperPage.SubPage?edit\">?</a>", renderedText);
		crawler.addPage(superPage, PathParser.parse("SubPage"));
		renderedText = superPage.getData().getHtml();
		assertEquals("<a href=\"SuperPage.SubPage\">^SubPage</a>", renderedText);
	}

	private void checkWord(boolean expectedMatch, String word)
	{
		Pattern p = Pattern.compile(WikiWordWidget.REGEXP, Pattern.DOTALL | Pattern.MULTILINE);
		Matcher match = p.matcher(word);
		final boolean matches = match.find();
		final boolean matchEquals = matches ? word.equals(match.group(0)) : false;
		boolean pass = (matches && matchEquals);
		if(!expectedMatch)
			pass = !pass;

		String failureMessage = word + (matches ? (" found " + (matchEquals ? "" : "but matched " + match.group(0))) : " not found");
		assertTrue(failureMessage, pass);
	}

	public void testIsWikiWord() throws Exception
	{
		assertEquals(true, WikiWordWidget.isWikiWord("HelloThere"));
		assertEquals(false, WikiWordWidget.isWikiWord("not.a.wiki.word"));
	}

	public void testAsWikiText() throws Exception
	{
		WikiWordWidget widget = new WikiWordWidget(new WidgetRoot(crawler.addPage(root, PathParser.parse("SomePage"))), "OldText");
		assertEquals("OldText", widget.asWikiText());
		widget.setText("NewText");
		assertEquals("NewText", widget.asWikiText());
	}

	public void testQualifiedReferenceToSubReference() throws Exception
	{
		WikiPage myPage = crawler.addPage(root, PathParser.parse("MyPage"));
		crawler.addPage(myPage, PathParser.parse("SubPage"));
		WikiWordWidget widget = new WikiWordWidget(new WidgetRoot(myPage), "^SubPage");
		assertEquals("^NewName", widget.makeRenamedRelativeReference(".MyPage.NewName"));
	}

	public void testQualifiedReferenceToRelativeReference() throws Exception
	{
		WikiPage myPage = crawler.addPage(root, PathParser.parse("MyPage"));
		crawler.addPage(root, PathParser.parse("MyBrother"));
		WikiWordWidget widget = new WikiWordWidget(new WidgetRoot(myPage), "MyBrother");
		assertEquals("MyBrother", widget.makeRenamedRelativeReference(".MyBrother"));

		WikiPage subPageOne = crawler.addPage(myPage, PathParser.parse("SubPageOne"));
		crawler.addPage(myPage, PathParser.parse("SubPageTwo"));
		widget = new WikiWordWidget(new WidgetRoot(subPageOne), "SubPageTwo");
		assertEquals("SubPageTwo", widget.makeRenamedRelativeReference(".MyPage.SubPageTwo"));
	}

	public void testRefersTo() throws Exception
	{
		assertTrue(WikiWordWidget.refersTo(".PageOne", ".PageOne"));
		assertTrue(WikiWordWidget.refersTo(".PageOne.PageTwo", ".PageOne"));
		assertFalse(WikiWordWidget.refersTo(".PageOne.PageTwo", ".PageOne.PageTw"));
	}

	public void testRenameMovedPageIfReferenced1() throws Exception
	{
		WikiPage page1 = crawler.addPage(root, PathParser.parse("PageOne"));
		WikiPage page2 = crawler.addPage(root, PathParser.parse("PageTwo"));

		WikiWordWidget widget = new WikiWordWidget(new WidgetRoot(page1), "PageTwo");
		widget.renameMovedPageIfReferenced(page2, "PageOne");
		assertEquals(".PageOne.PageTwo", widget.text);
	}

	public void testRenameMovedPageIfReferenced2() throws Exception
	{
		WikiPage page1 = crawler.addPage(root, PathParser.parse("PageOne"));
		WikiPage page2 = crawler.addPage(page1, PathParser.parse("PageTwo"));

		WikiWordWidget widget = new WikiWordWidget(new WidgetRoot(page1), "^PageTwo");
		widget.renameMovedPageIfReferenced(page2, "");
		assertEquals(".PageTwo", widget.text);
	}

	//TODO -MDM- bug I descovered while trying to refactor.
	public void testmakeRenamedRelativeReference() throws Exception
	{
		crawler.addPage(root, PathParser.parse("FitNesse"));
		crawler.addPage(root, PathParser.parse("FitNesse.SuiteAcceptanceTests"));
		WikiPage parentPage = crawler.addPage(root, PathParser.parse("FitNesse.SuiteAcceptanceTests.SuiteWikiPageResponderTests"));
		WikiWordWidget widget = new WikiWordWidget(new WidgetRoot(parentPage), "WikiWord");
		widget.parentPage = parentPage;

		try
		{
			widget.makeRenamedRelativeReference(".FitNesse.SuiteAcceptanceTests.SuiteWidgetTests.WikiWord");
			//Not sure what the result should be but it's the exception that's causing trouble.
		}
		catch(Exception e)
		{
			fail(e.getMessage());
		}

	}
}
