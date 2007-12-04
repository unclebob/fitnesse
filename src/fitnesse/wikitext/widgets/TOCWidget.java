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
   // [-R[0-9]] [-g]
   public static final String REGEXP = "(?:^!contents([ \t]+-R[0-9]*)?([ \t]+-[fhgp])*?[ \\t]*$)";
   
   public static final String REGRACE_TOC 			= "REGRACE_TOC";
   public static final String FILTER_TOC 				= "FILTER_TOC";
   public static final String PROPERTY_TOC 			= "PROPERTY_TOC";
   public static final String PROPERTY_CHARACTERS 	= "PROPERTY_CHARACTERS";
   public static final String HELP_TOC					= "HELP_TOC";
   public static final String HELP_PREFIX_TOC		= "HELP_PREFIX_TOC";
   
   public static final String PROP_CHAR_DEFAULT 	= "*+@>";
   public static final String HELP_PREFIX_DEFAULT 	= "...";
   
   public String   propertyCharacters = PROP_CHAR_DEFAULT;
   public String   helpTextPrefix     = HELP_PREFIX_DEFAULT;
   
	private boolean recursive, 
						 isGraceful, 	isVarGraceful, 
						 isPropertied,	isVarPropertied, 
						 isFiltered, 	isVarFiltered,
						 isHelpShown,  isVarHelpShown;
   private int     depthLimit;  // 0 = unlimited depth recursion

	public TOCWidget(ParentWidget parent, String text)
	{
		super(parent);
		setRecursive(text);
      setGraceful(text);
      setPropertied(text);
      setFiltered(text);
      setHelpShown(text);
	}

	private void setRecursive(String text)
	{
		recursive = (text.indexOf("-R") > -1);
      
      if (recursive) // -R[0-9]...
      {  Pattern pat = Pattern.compile("-R([0-9])");
         Matcher mat = pat.matcher(text);
         depthLimit = mat.find()?  Integer.valueOf(mat.group(1)) : 0;
      }
	}

   private void setGraceful(String text)
   {
      isGraceful  = (text.indexOf("-g") > -1);
   }
   
   private void setPropertied(String text)
   {
      isPropertied  = (text.indexOf("-p") > -1);
   }
   
   private void setFiltered(String text)
   {
      isFiltered  = (text.indexOf("-f") > -1);
   }
   
   private void setHelpShown (String text)
   {
      isHelpShown  = (text.indexOf("-h") > -1);
   }
   
	public String render() throws Exception
	{
		WikiPage page = getWikiPage();
		setVarFlags          (page);
		setPropertyCharacters(page);
		setHelpTextPrefix    (page);		
		return buildContentsDiv(page, 1).html();
	}

	private void setVarFlags (WikiPage page) throws Exception
	{
     isVarGraceful   = "true".equals(parent.getVariable(REGRACE_TOC) ); 
     isVarPropertied = "true".equals(parent.getVariable(PROPERTY_TOC));
     isVarFiltered   = "true".equals(parent.getVariable(FILTER_TOC)  );
     isVarHelpShown  = "true".equals(parent.getVariable(HELP_TOC)    );
	}
	
	public boolean isRegracing ()        { return isVarGraceful   || isGraceful;   }
   public boolean isPropertyAppended () { return isVarPropertied || isPropertied; }
   public boolean isFiltersAppended ()  { return isVarFiltered   || isFiltered;   }
   public boolean isHelpAppended ()     { return isVarHelpShown  || isHelpShown;  }

   private void setPropertyCharacters (WikiPage page)
   {
      StringBuffer propChars = new StringBuffer();
   	try
   	{	String propsFromEnv = parent.getVariable(PROPERTY_CHARACTERS);
   		if (propsFromEnv != null)  propChars.append(propsFromEnv);
   	} catch (Exception e) { }
   	
   	int newLength = propChars.length();
   	
   	if (newLength < PROP_CHAR_DEFAULT.length())
   		propChars.append(PROP_CHAR_DEFAULT.substring(newLength));
   	
   	propertyCharacters = propChars.toString();
   }

   private void setHelpTextPrefix (WikiPage page) throws Exception
   {
   	String helpPrefixEnv = parent.getVariable(HELP_PREFIX_TOC);
   	helpTextPrefix = (helpPrefixEnv != null)? helpPrefixEnv : HELP_PREFIX_DEFAULT; 
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

   private boolean isDepthExceeded (int currentDepth)  // -R[0-9] limiter
   { return (depthLimit > 0) && (currentDepth > depthLimit);
   }
   
	private HtmlTag buildListItem(WikiPage wikiPage, int currentDepth) throws Exception
	{
		HtmlTag listItem = new HtmlTag("li");
		HtmlTag link = HtmlUtil.makeLink(getHref(wikiPage), getLinkText(wikiPage));
		addHelpText(link, wikiPage);
		listItem.add(link);
      
		if(isRecursive() && buildListOfChildPages(wikiPage).size() > 0)
		{
         // -R[0-9] limit & show "..."
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

	private void addHelpText (HtmlTag link, WikiPage wikiPage) throws Exception
	{
			String helpText = wikiPage.getHelpText();
			if (helpText != null)
			{
				if (isHelpAppended())
					link.tail = HtmlUtil.makeSpanTag("pageHelp", "..." + helpText).html();
				else
					link.addAttribute("title", helpText);
			}
	}

	private HtmlElement getLinkText(WikiPage wikiPage) throws Exception
	{
      String name    = regrace(wikiPage.getName()),
             props   = getProperties(wikiPage),
             filters = getFilters(wikiPage);

		if(wikiPage instanceof ProxyPage)
			return new HtmlTag("i", name + props + filters);
		else
			return new RawHtml(name + props + filters);
	}

	private String getProperties (WikiPage wikiPage) throws Exception
	{
		StringBuffer propText = new StringBuffer();
		if (isPropertyAppended())
		{
			PageData data = wikiPage.getData();
			WikiPageProperties props = data.getProperties();
			
			if (props.has("Suite"))        propText.append(propertyCharacters.charAt(0));
			if (props.has("Test"))         propText.append(propertyCharacters.charAt(1));
			if (props.has("WikiImport"))   propText.append(propertyCharacters.charAt(2));
			if (isSymbolic(wikiPage))      propText.append(propertyCharacters.charAt(3));
		}
		
		return (propText.length() > 0)? " " + propText.toString() : ""; 
	}

	private boolean isSymbolic (WikiPage page) throws Exception
	{
		boolean isSym = false;
		WikiPageProperties props = page.getParent().getData().getProperties();
		
		if (props.has("SymbolicLinks"))
		{
			WikiPageProperty syms = props.getProperty("SymbolicLinks");
			isSym = syms == null? false : syms.has(page.getName());
		}
		
		return isSym;
	}

	private String getFilters (WikiPage wikiPage) throws Exception
	{
		String filters = "";
		
		if (isFiltersAppended())
		{
			PageData data = wikiPage.getData();
			WikiPageProperties props = data.getProperties();
			
			String filterText = (props.has(PageData.PropertySUITES))? filterText = props.get(PageData.PropertySUITES) : "";
			filters = (filterText != null)? filterText.trim() : ""; 
		}
		
		return (filters.length() > 0)? " (" + filters + ")": ""; 
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
