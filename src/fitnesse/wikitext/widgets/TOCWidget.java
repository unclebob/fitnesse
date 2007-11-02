// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.html.*;
import fitnesse.wiki.*;
import fitnesse.wikitext.WikiWidget;

import java.util.*;
import java.util.regex.*;

public class TOCWidget extends WikiWidget
{
   //[acd] !contents: [-R[0-9]] [-g]
   public static final String REGEXP = "(?:^!contents([ \t]+-R[0-9]*)?([ \t]+-g)?[ \\t]*$)";
   public static final String REGRACE_TOC = "REGRACE_TOC";

	private boolean recursive, isGraceful;
   private int     depthLimit;  //[acd] !contents: 0 = unlimited depth recursion

	public TOCWidget(ParentWidget parent, String text)
	{
		super(parent);
		setRecursive(text);
      setGraceful(text);
	}

	private void setRecursive(String text)
	{
		recursive = (text.indexOf("-R") > -1);
      
      if (recursive) //[acd] !contents: -R[0-9]...
      {  Pattern pat = Pattern.compile("-R([0-9])");
         Matcher mat = pat.matcher(text);
         depthLimit = mat.find()?  Integer.valueOf(mat.group(1)) : 0;
      }
	}

   private void setGraceful(String text) //[acd] Regracing
   {
      isGraceful  = (text.indexOf("-g") > -1);
   }
   
	public String render() throws Exception
	{
		return buildContentsDiv(getWikiPage(), 1).html();
	}

	private HtmlTag buildContentsDiv(WikiPage wikiPage, int currentDepth)
		throws Exception
	{
		HtmlTag div = makeDivTag(currentDepth);
      div.add(buildList(wikiPage, currentDepth));
		return div;
	}

	private HtmlTag buildList(WikiPage wikiPage, int currentDepth)
		throws Exception
	{
		HtmlTag list = new HtmlTag("ul");
		for(Iterator iterator = buildListOfChildPages(wikiPage).iterator(); iterator.hasNext();)
		{
			list.add(buildListItem((WikiPage) iterator.next(), currentDepth));
		}
		return list;
	}

   private boolean isDepthExceeded (int currentDepth)  //[acd] !contents: -R[0-9] limiter
   { return (depthLimit > 0) && (currentDepth > depthLimit);
   }
   
	private HtmlTag buildListItem(WikiPage wikiPage, int currentDepth) throws Exception
	{
		HtmlTag listItem = new HtmlTag("li");
		listItem.add(HtmlUtil.makeLink(getHref(wikiPage), getLinkText(wikiPage)));
      
		if(isRecursive() && buildListOfChildPages(wikiPage).size() > 0)
		{
         //[acd] !contents: -R[0-9] limit & show "..."
         if (isDepthExceeded(currentDepth + 1))
            listItem.add("...");
         else
            listItem.add(buildContentsDiv(wikiPage, currentDepth + 1));
		}
		return listItem;
	}

	private String getHref(WikiPage wikiPage) throws Exception
	{
		String href = null;
		WikiPagePath wikiPagePath = wikiPage.getPageCrawler().getFullPath(wikiPage);
		href = PathParser.render(wikiPagePath);
		return href;
	}

   //[acd] Regracing
   public boolean isRegracing ()
   {  boolean isDoingIt = false;
      try { isDoingIt = "true".equals(parent.getVariable(REGRACE_TOC)); }
      catch (Exception e) { isDoingIt = false; }
      return isDoingIt || isGraceful;
   }

	private HtmlElement getLinkText(WikiPage wikiPage) throws Exception
	{
      String name = regrace(wikiPage.getName());  //[acd] regrace names
		if(wikiPage instanceof ProxyPage)
			return new HtmlTag("i", name);
		else
			return new RawHtml(name);
	}

	private List buildListOfChildPages(WikiPage wikiPage) throws Exception
	{
		List childPageList = new ArrayList(wikiPage.getChildren());
		if(wikiPage.hasExtension(VirtualCouplingExtension.NAME))
		{
			VirtualCouplingExtension extension = (VirtualCouplingExtension) wikiPage.getExtension(VirtualCouplingExtension.NAME);
			WikiPage virtualCoupling = extension.getVirtualCoupling();
			childPageList.addAll(virtualCoupling.getChildren());
		}
		Collections.sort(childPageList);
		return childPageList;
	}

	private HtmlTag makeDivTag(int currentDepth)
	{
		return HtmlUtil.makeDivTag("toc" + currentDepth);
	}

	public boolean isRecursive()
	{
		return recursive;
	}
}
