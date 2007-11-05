// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.html.*;

import java.util.Random;
import java.util.regex.*;

public class CollapsableWidget extends ParentWidget
{
	private static final String ENDL = LineBreakWidget.REGEXP;
   //[acd] invisible: Modify REGEXP to allow < to make invisible
	public static final String REGEXP = "!\\*+[<>]? .*?" + ENDL + ".*?" + ENDL + "\\*+!" + ENDL + "?";
	private static final Pattern pattern = Pattern.compile("!\\*+([<>])? (.*?)" + ENDL + "(.*?)" + ENDL + "\\*+!", Pattern.MULTILINE + Pattern.DOTALL);
   //[acd] invisible: N.B., add to fitnesse.css:
   //div.invisible  { line-height: 0px; margin: 0px 0px 0px 0px; display:none; visibility:hidden; }
   //[acd] invisible: end of < patch to REGEXP

	private static Random random = new Random();

	private String cssClass = "collapse_rim";
	private ParentWidget titleWidget;
	public boolean expanded = true;
   public boolean invisible = false;

	private static final String collapsableOpenCss = "collapsable";
   private static final String collapsableInvisibleCss = "invisible";  //[acd] invisible: class in fitnesse[_base].css
   //[acd] invisible: Add to fitnesse_base.css: .invisible  { line-height: 0px;  visibility:hidden; }
	private static final String collapsableClosedCss = "hidden";
	private static final String collapsableOpenImg = "/files/images/collapsableOpen.gif";
	private static final String collapsableClosedImg = "/files/images/collapsableClosed.gif";
	private static final String collapseAllLink = "<a href=\"javascript:collapseAll();\">Collapse All</a>";
	private static final String expandAllLink = "<a href=\"javascript:expandAll();\">Expand All</a>";

	public CollapsableWidget(ParentWidget parent)
	{
		super(parent);
	}

	public CollapsableWidget(ParentWidget parent, String text) throws Exception
	{
		this(parent);
		Matcher match = pattern.matcher(text);
		match.find();
      String tailChar = match.group(1);
		expanded  = tailChar == null;
      invisible = expanded? false : "<".equals(tailChar); 
		String title = match.group(2);
		String body = match.group(3);
		init(title, body, this);
	}

   //[acd] !include: Refactored to use dual-scope constructor
	public CollapsableWidget(ParentWidget parent, String title, String body, String cssClass, boolean collapsed) throws Exception
	{
		this(parent);
		init(title, body, this); //[acd] !include: dual-scope
		this.cssClass = cssClass;
		this.expanded = !collapsed;
	}

   //[acd] !include: New constructor with dual scope: title & body.
   public CollapsableWidget(ParentWidget parent, ParentWidget includeParent, 
                            String title, String body, String cssClass, boolean collapsed) throws Exception
	{
      this(parent);
      init(title, body, includeParent);
      this.cssClass = cssClass;
      this.expanded = !collapsed;
   }

   //[acd] !include: Refactored for 3rd arg
	private void init(String title, String body, ParentWidget parent) throws Exception
	{
		titleWidget = new BlankParentWidget(parent, "!meta " + title); //[acd] !include: patch
		addChildWidgets(body);
	}

	public String render() throws Exception
	{
		HtmlElement titleElement = new RawHtml("&nbsp;" + titleWidget.childHtml());
		HtmlElement bodyElement = new RawHtml(childHtml());
		HtmlElement html = makeCollapsableSection(titleElement, bodyElement);
		return html.html();
	}

	public HtmlTag makeCollapsableSection(HtmlElement title, HtmlElement content)
	{
		String id = random.nextLong() + "";
      //[acd] invisible: < recognition
      HtmlTag outerDiv;
      if (invisible)
      {
         outerDiv = HtmlUtil.makeDivTag(collapsableInvisibleCss);
         outerDiv.add(content);
         return outerDiv;
      }
      //[acd] invisible: end of < recognition

      outerDiv = HtmlUtil.makeDivTag(cssClass);
      
		HtmlTag image = new HtmlTag("img");
		image.addAttribute("src", imageSrc());
		image.addAttribute("class", "left");
		image.addAttribute("id", "img" + id);

		HtmlTag anchor = new HtmlTag("a", image);
		anchor.addAttribute("href", "javascript:toggleCollapsable('" + id + "');");

		HtmlTag links = new HtmlTag("div");
		links.addAttribute("style", "float: right;");
		links.addAttribute("class", "meta");
		links.add(expandAllLink + " | " + collapseAllLink);

		outerDiv.add(links);
		outerDiv.add(anchor);
		outerDiv.add(title);

		HtmlTag collapsablediv = makeCollapsableDiv();
		collapsablediv.addAttribute("id", id);
		collapsablediv.add(content);
		outerDiv.add(collapsablediv);

		return outerDiv;
	}

	private HtmlTag makeCollapsableDiv()
	{
		if(!expanded)
			return HtmlUtil.makeDivTag(collapsableClosedCss);
		else
			return HtmlUtil.makeDivTag(collapsableOpenCss);
	}

	private String imageSrc()
	{
		if(expanded)
			return collapsableOpenImg;
		else
			return collapsableClosedImg;
	}
}
