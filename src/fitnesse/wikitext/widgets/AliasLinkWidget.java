// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.PathParser;
import fitnesse.wiki.ProxyPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wikitext.WidgetBuilder;
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

	public AliasLinkWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		parentPage = getWikiPage().getParent();
		Matcher match = pattern.matcher(text);
		if(match.find())
		{
			tag = match.group(1);
			href = match.group(2);
			addChildWidgets(tag);
		}
	}

	//[acd] Alias Vars/Evals: Class to expand variables
	public static class VariableExpandingWidgetRoot extends ParentWidget
	{
		public VariableExpandingWidgetRoot(ParentWidget parent, String content) throws Exception
		{
			super(parent);
			if(content != null) addChildWidgets(content);
		}

		public WidgetBuilder getBuilder()
		{
			return WidgetBuilder.literalAndVariableWidgetBuilder;
		}

		public boolean doEscaping()
		{
			return false;
		}

		public String render() throws Exception
		{
			return "";
		}

		protected void addToParent()
		{
		}
	}
	//[acd] Alias Vars/Evals: end of variable expander

	public String render() throws Exception
	{
		//[acd] Alias Vars/Evals: Expand and allow (?) and (#) in alias link
		//
		String expandedHref = (new VariableExpandingWidgetRoot(this, href)).childHtml();
		Pattern urlPat = Pattern.compile("([^\\?\\#]*)((\\?.+)?(\\#.+)?)?");
		Matcher urlMat = urlPat.matcher(expandedHref);
		String urlSuffix = "";

		if(urlMat.matches())
		{
			expandedHref = urlMat.group(1);
			urlSuffix = urlMat.group(2);
		}

		if(WikiWordWidget.isWikiWord(expandedHref))
		{
			WikiWordWidget www = new WikiWordWidget(new BlankParentWidget(this, ""), expandedHref);
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
		else
		{
			String url = LinkWidget.makeUrlUsable(href);
			return String.format("<a href=\"%s\">%s</a>", url, tag);
		}

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
