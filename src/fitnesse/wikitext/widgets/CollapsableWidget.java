// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.RawHtml;
import fitnesse.responders.WikiImportProperty;
import fitnesse.wiki.PageData;

public class CollapsableWidget extends ParentWidget {
  private static final String ENDL = LINE_BREAK_PATTERN;
  public static final String REGEXP = "!\\*+[<>]? .*?" + ENDL + ".*?" + ENDL + "\\*+!" + ENDL + "?";
  private static final Pattern pattern = Pattern.compile("!\\*+([<>])? (.*?)" + ENDL + "(.*?)" + ENDL + "\\*+!", Pattern.MULTILINE + Pattern.DOTALL);
  //invisible: N.B., add to fitnesse_base.css:
  //div.invisible  { line-height: 0px; visibility:hidden; }

  private static Random random = new Random();

  private String cssClass = "collapse_rim";
  private ParentWidget titleWidget;
  public boolean expanded = true;
  public boolean invisible = false;
  // hack. We only set the following flag to false in the 2-arg. constructor shown below. This constructor
  // is only used in WidgetBuilder and only when "!* .... *!" wiki text is encountered. For those collapsable sections,
  // we DON'T want an edit link!
  private boolean showEditCommand = true;

  private static final String collapsableOpenCss = "collapsable";
  private static final String collapsableInvisibleCss = "invisible";
  //invisible: Add to fitnesse_base.css: .invisible  { line-height: 0px;  visibility:hidden; }
  private static final String collapsableClosedCss = "hidden";
  private static final String collapsableOpenImg = "/files/images/collapsableOpen.gif";
  private static final String collapsableClosedImg = "/files/images/collapsableClosed.gif";
  private static final String collapseAllLink = "<a href=\"javascript:collapseAll();\">Collapse All</a>";
  private static final String expandAllLink = "<a href=\"javascript:expandAll();\">Expand All</a>";

  public CollapsableWidget(ParentWidget parent) {
    super(parent);
  }

  public CollapsableWidget(ParentWidget parent, String text) throws Exception {
    this(parent);
    showEditCommand = false;  // hack. only set false here.
    Matcher match = pattern.matcher(text);
    match.find();
    String tailChar = match.group(1);
    expanded = tailChar == null;
    invisible = expanded ? false : "<".equals(tailChar);
    String title = match.group(2);
    String body = match.group(3);
    init(title, body, this);
  }

  //!include: Refactored to use dual-scope constructor
  public CollapsableWidget(ParentWidget parent, String title, String body, String cssClass, boolean collapsed) throws Exception {
    this(parent);
    init(title, body, this); //!include: dual-scope
    this.cssClass = cssClass;
    this.expanded = !collapsed;
  }

  //!include: New constructor with dual scope: title & body.
  public CollapsableWidget(ParentWidget parent, ParentWidget includeParent,
                           String title, String body, String cssClass, boolean collapsed) throws Exception {
    this(parent);
    init(title, body, includeParent);
    this.cssClass = cssClass;
    this.expanded = !collapsed;
  }

  //!include: Refactored for 3rd arg
  private void init(String title, String body, ParentWidget parent) throws Exception {
    titleWidget = new BlankParentWidget(parent, "!meta " + title + " " + makeEditLinks(title));
    addChildWidgets(body);
  }

  private String makeEditLinks(String title) {
    if (showEditCommand == false)
      return "";
    try {
      PageData pageData = getWikiPage().getData();
      if (WikiImportProperty.isImported(pageData)) {
        return makeImportedEditLinks(pageData, title);
      }
    } catch (Exception e) {
      ;
    }
    return "[[(edit)][" + makeLocalEditLinks(title) + "]]";
  }

  private String makeLocalEditLinks(String title) {
    String[] splitTitle = title.split("\\s+");
    return splitTitle[splitTitle.length - 1] + "?edit&amp;redirectToReferer=true&amp;redirectAction=";
  }

  private String makeImportedEditLinks(PageData pageData, String title) {
    return "(edit: [[locally][" + makeLocalEditLinks(title) + "]]" +
      " | [[remotely][" + makeRemoteLink(pageData, title) + "]])";
  }

  private String makeRemoteLink(PageData pageData, String title) {
    String remoteInputParameters = WikiImportProperty.makeRemoteEditQueryParameters();
    WikiImportProperty importProperty;
    try {
      importProperty = WikiImportProperty.createFrom(pageData.getProperties());
      return importProperty.getSourceUrl() + "?" + remoteInputParameters;
    } catch (Exception e) {
      e.printStackTrace();
    }
    return "";
  }

  public String render() throws Exception {
    HtmlElement titleElement = new RawHtml("&nbsp;" + titleWidget.childHtml());
    HtmlElement bodyElement = new RawHtml(childHtml());
    HtmlElement html = makeCollapsableSection(titleElement, bodyElement);
    return html.html();
  }

  public HtmlTag makeCollapsableSection(HtmlElement title, HtmlElement content) {
    String id = random.nextLong() + "";
    //invisible: < recognition
    HtmlTag outerDiv;
    if (invisible) {
      outerDiv = HtmlUtil.makeDivTag(collapsableInvisibleCss);
      outerDiv.add(content);
      return outerDiv;
    }

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

  private HtmlTag makeCollapsableDiv() {
    if (!expanded)
      return HtmlUtil.makeDivTag(collapsableClosedCss);
    else
      return HtmlUtil.makeDivTag(collapsableOpenCss);
  }

  private String imageSrc() {
    if (expanded)
      return collapsableOpenImg;
    else
      return collapsableClosedImg;
  }

  public String asWikiText() throws Exception {
    return "";
  }
}
