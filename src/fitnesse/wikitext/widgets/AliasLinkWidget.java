// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.PathParser;
import fitnesse.wiki.ProxyPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.WidgetVisitor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AliasLinkWidget extends ParentWidget
{
	public static final String REGEXP = "\\[\\[[^\n\r\\]]+\\]\\[[^\n\r\\]]+\\]\\]";
	public static final Pattern pattern = Pattern.compile("\\[\\[([^\n\r\\]]+)\\]\\[([^\n\r\\]]+)\\]\\]");
	private String tag;
	private String href;
	WikiPage parentPage;
	private static final Pattern URL_SUFFIX_PARSER = Pattern.compile("([^\\?\\#]*)((\\?.+)?(\\#.+)?)?");

	public AliasLinkWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		parentPage = getWikiPage().getParent();
		Matcher match = pattern.matcher(text);
		match.find();
		tag = match.group(1);
		href = match.group(2).trim();
		addChildWidgets(tag);
	}

	public String render() throws Exception
	{
		String expandedHref = expandVariables(href);
		Matcher suffixMatcher = URL_SUFFIX_PARSER.matcher(expandedHref);
		suffixMatcher.find();
		String url = suffixMatcher.group(1);
		String urlSuffix = suffixMatcher.group(2);

		return makeLinkTag(url, urlSuffix);

	}

	private String makeLinkTag(String url, String urlSuffix) throws Exception
	{
		if(WikiWordWidget.isWikiWord(url))
			return makeLinkTagForWikiWord(url, urlSuffix);
		else
		{
			url = LinkWidget.makeUrlUsable(url);
			return String.format("<a href=\"%s\">%s</a>", url+urlSuffix, childHtml());
		}
	}

	private String makeLinkTagForWikiWord(String url, String urlSuffix) throws Exception
	{
		WikiWordWidget www = new WikiWordWidget(new BlankParentWidget(this, ""), url);
		String theWord = www.getWikiWord();
		WikiPagePath wikiWordPath = PathParser.parse(theWord);
		WikiPagePath fullPathOfWikiWord = parentPage.getPageCrawler().getFullPathOfChild(parentPage, wikiWordPath);
		String qualifiedName = PathParser.render(fullPathOfWikiWord);
		if(parentPage.getPageCrawler().pageExists(parentPage, PathParser.parse(theWord)))
			return ("<a href=\"" + qualifiedName + urlSuffix + "\">" + childHtml() + "</a>"); //[acd] Alias V/E: use it
		else if(getWikiPage() instanceof ProxyPage)
			return makeAliasLinkToNonExistentRemotePage(theWord);
		else
			return (childHtml() + "<a href=\"" + qualifiedName + "?edit\">?</a>");
	}

	private String makeAliasLinkToNonExistentRemotePage(String theWord) throws Exception
	{
		ProxyPage proxy = (ProxyPage) getWikiPage();
		String remoteURLOfPage = proxy.getThisPageUrl();
		String nameOfThisPage = proxy.getName();
		int startOfThisPageName = remoteURLOfPage.lastIndexOf(nameOfThisPage);
		String remoteURLOfParent = remoteURLOfPage.substring(0, startOfThisPageName);
		return childHtml() + "<a href=\"" + remoteURLOfParent + theWord + "?edit\""
			+ " target=\"" + theWord + "\""
			+ ">?</a>";
	}

	public String asWikiText() throws Exception
	{
		return "[[" + childWikiText() + "][" + href + "]]";
	}

	public void acceptVisitor(WidgetVisitor visitor) throws Exception
	{
		visitor.visit(this);
	}

	public void renamePageIfReferenced(WikiPage pageToRename, String newName) throws Exception
	{
		if(WikiWordWidget.isWikiWord(href))
		{
			WikiWordWidget www = new WikiWordWidget(new BlankParentWidget(this, ""), href);
			www.renamePageIfReferenced(pageToRename, newName);
			href = www.getText();
		}
	}
}
