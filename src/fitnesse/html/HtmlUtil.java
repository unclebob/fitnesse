// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPageAction;

import java.util.List;

public class HtmlUtil {
  public static final String BRtag = "<br/>";
  public static final String HRtag = "<hr/>";
  public static HtmlElement BR = new RawHtml(BRtag);
  public static HtmlElement HR = new RawHtml(HRtag);
  public static HtmlElement NBSP = new RawHtml("&nbsp;");
  public static HtmlElement P = new RawHtml("<p>");
  public static final boolean NO_NEW_WINDOW = false;
  public static final String ENDL = System.getProperty("line.separator");

  public static HtmlTag makeDivTag(String divClass) {
    HtmlTag div = new HtmlTag("div");
    div.addAttribute("class", divClass);
    div.add("");
    return div;
  }

  public static void addTitles(HtmlPage page, String title) {
    page.title.use(title);
    HtmlTag span = new HtmlTag("span");
    span.addAttribute("class", "page_title");
    span.add(title);
    page.header.use(span);
  }

  public static HtmlTag makeBold(String content) {
    HtmlTag bold = new HtmlTag("b");
    bold.add(content);
    return bold;
  }

  public static HtmlTag makeItalic(String content) {
    HtmlTag italic = new HtmlTag("i");
    italic.add(content);
    return italic;
  }

  public static HtmlTag makeSpanTag(String spanClass, String content) {
    HtmlTag span = new HtmlTag("span");
    span.addAttribute("class", spanClass);
    span.add(content);
    return span;
  }

  public static HtmlTag makeFormTag(String method, String action) {
    return makeFormTag(method, action, null);
  }

  public static HtmlTag makeFormTag(String method, String action, String name) {
    HtmlTag formTag = new HtmlTag("form");
    formTag.addAttribute("method", method);
    formTag.addAttribute("action", action);
    if (name != null)
      formTag.addAttribute("name", name);
    return formTag;
  }

  public static HtmlTag makeAnchorTag(String name) {
    HtmlTag anchorTag = new HtmlTag("a", " ");
    anchorTag.addAttribute("name", name);
    return anchorTag;
  }

  public static HtmlTag makeInputTag(String type, String name, String value) {
    HtmlTag input = makeInputTag(type, name);
    input.addAttribute("value", value);
    return input;
  }

  public static HtmlTag makeInputTag(String type, String name) {
    HtmlTag input = new HtmlTag("input");
    input.addAttribute("type", type);
    input.addAttribute("name", name);
    return input;
  }

  public static HtmlTag makeOptionTag(String value, String text) {
    HtmlTag option = new HtmlTag("option");
    option.addAttribute("value", value);
    option.add(text);

    return option;
  }

  public static HtmlTag makeLink(String href, String text) {
    return makeLink(href, new RawHtml(text));
  }

  public static HtmlTag makeLink(String href, HtmlElement content) {
    HtmlTag link = new HtmlTag("a");
    link.addAttribute("href", href);
    link.add(content);
    return link;
  }

  public static TagGroup makeBreadCrumbsWithCurrentPageLinked(String path)
    throws Exception {
    return makeBreadCrumbsWithCurrentPageLinked(path, ".");
  }

  public static HtmlTag makeBreadCrumbsWithCurrentPageNotLinked(String trail)
    throws Exception {
    return makeBreadCrumbsWithCurrentPageNotLinked(trail, ".");
  }

  public static TagGroup makeBreadCrumbsWithCurrentPageLinked(
    String path,
    String separator
  ) throws Exception {
    TagGroup tagGroup = new TagGroup();
    String[] crumbs = path.split("[" + separator + "]");
    String trail = makeAllButLastCrumb(crumbs, separator, tagGroup);
    tagGroup.add(getLastCrumbAsLink(crumbs, trail));
    return tagGroup;
  }

  public static HtmlTag makeBreadCrumbsWithCurrentPageNotLinked(
    String path,
    String separator
  ) throws Exception {
    TagGroup tagGroup = new TagGroup();
    String[] crumbs = path.split("[" + separator + "]");
    makeAllButLastCrumb(crumbs, separator, tagGroup);
    tagGroup.add(getLastCrumbAsText(crumbs));
    return tagGroup;
  }

  private static HtmlTag getLastCrumbAsLink(String[] crumbs, String trail)
    throws Exception {
    String crumb = getLastCrumb(crumbs);
    HtmlTag link = makeLink("/" + trail + crumb, crumb);
    link.head = HtmlUtil.BR.html();
    link.addAttribute("class", "page_title");
    return link;
  }

  private static String getLastCrumb(String[] crumbs) {
    String crumb = "";
    if (crumbs.length > 0)
      crumb = crumbs[crumbs.length - 1];
    return crumb;
  }

  private static HtmlTag getLastCrumbAsText(String[] crumbs) throws Exception {
    String crumb = getLastCrumb(crumbs);
    HtmlTag thisPage = new HtmlTag("span", crumb);
    thisPage.addAttribute("class", "page_title");
    thisPage.head = HtmlUtil.BR.html();
    return thisPage;
  }

  public static HtmlTag makeBreadCrumbsWithPageType(String trail, String type) throws Exception {
    return makeBreadCrumbsWithPageType(trail, ".", type);
  }

  public static HtmlTag makeBreadCrumbsWithPageType(String trail, String separator, String type) throws Exception {
    TagGroup group = makeBreadCrumbsWithCurrentPageLinked(trail, separator);
    group.add(HtmlUtil.BR);
    group.add(HtmlUtil.makeSpanTag("page_type", type));
    return group;
  }

  private static String makeAllButLastCrumb(String[] crumbs, String separator, TagGroup group) {
    String trail = "";
    for (int i = 0; i < crumbs.length - 1; i++) {
      String crumb = crumbs[i];
      HtmlTag link = makeLink("/" + trail + crumb, crumb);
      link.tail = separator;
      trail = trail + crumb + separator;
      group.add(link);
    }
    return trail;
  }

  public static HtmlTag makeActions(List<WikiPageAction> actions) throws Exception {
    TagGroup actionsGroup = new TagGroup();

    for (WikiPageAction action : actions) {
      if (action.getPageName() == null)
        addBreakToActions(actionsGroup, action);
      else
        addLinkToActions(actionsGroup, action);
    }
    return actionsGroup;
  }
  
  private static void addBreakToActions(TagGroup actions, WikiPageAction action) {
    final HtmlTag tag = new HtmlTag("div");
    tag.addAttribute("class", "main");
    tag.add(action.getLinkName());
    actions.add(tag);
    }

  private static void addLinkToActions(TagGroup actions, WikiPageAction action) {
    actions.add(makeAction(action));
    actions.add(makeNavBreak());
  }

  public static HtmlTag makeAction(WikiPageAction action) {
    String href = action.getPageName();
    if (action.getQuery() != null && action.getQuery().length() > 0)
      href = href + "?" + action.getQuery();

    HtmlTag linkTag = new HtmlTag("a");
    linkTag.addAttribute("href", href);
    if (action.isNewWindow())
      linkTag.addAttribute("target", "newWindow");
    linkTag.addAttribute("accesskey", action.getShortcutKey());
    linkTag.add(action.getLinkName());

    TagGroup group = new TagGroup();
    group.add(new HtmlComment(action.getLinkName() + " button"));
    group.add(linkTag);
    return group;
  }

  public static HtmlTag makeNavBreak() {
    HtmlTag navBreak = new HtmlTag("div");
    navBreak.addAttribute("class", "nav_break");
    navBreak.add("&nbsp;");
    return navBreak;
  }

  public static String makeNormalWikiPageContent(PageData pageData) throws Exception {
    SetupTeardownIncluder.includeInto(pageData);
    return makePageHtmlWithHeaderAndFooter(pageData);
  }

  public static String makePageHtmlWithHeaderAndFooter(PageData pageData) throws Exception {
    StringBuffer buffer = new StringBuffer();
    buffer.append(pageData.getHeaderPageHtml());
    buffer.append(pageData.getHtml());
    buffer.append("<br/><div class=\"footer\">\n");
    buffer.append(pageData.getFooterPageHtml());
    buffer.append("</div>\n");
    return buffer.toString();
  }

  public static String metaText(String text) {
    return "<span class=\"meta\">" + text + "</span>";
  }

  public static HtmlTag makeJavascriptLink(String jsFile) {
    HtmlTag scriptTag = new HtmlTag("script");
    scriptTag.addAttribute("src", jsFile);
    scriptTag.addAttribute("type", "text/javascript");
    scriptTag.use("");
    return scriptTag;
  }
  
  public static String escapeHtmlForJavaScript(String html) {
    html = html.replaceAll("\"", "\\\\\"");
    html = html.replaceAll("\t", "\\\\t");
    html = html.replaceAll("\n", "\\\\n");
    html = html.replaceAll("\r", "\\\\r");
    html = html.replaceAll(HtmlElement.endl, "\\\\n");
    return html;
  }
  
  public static HtmlTag makeAppendElementScript(String idElementToAppend, String htmlToAppend) {
    HtmlTag scriptTag = new HtmlTag("script");
    String getElement = "document.getElementById(\"" + idElementToAppend + "\")";
    String escapedHtml = escapeHtmlForJavaScript(htmlToAppend);
    
    StringBuffer script = new StringBuffer();
    script.append("var existingContent = ").append(getElement).append(".innerHTML;");
    script.append(HtmlTag.endl);
    script.append(getElement).append(".innerHTML = existingContent + \"").append(escapedHtml).append("\";");
    script.append(HtmlTag.endl);
    scriptTag.add(script.toString());
    
    return scriptTag;
  }
  
  public static HtmlTag makeReplaceElementScript(String idElement, String newHtmlForElement) {
    HtmlTag scriptTag = new HtmlTag("script");
    String escapedHtml = escapeHtmlForJavaScript(newHtmlForElement);
    scriptTag.add("document.getElementById(\"" + idElement + "\").innerHTML = \"" + escapedHtml + "\";");
    return scriptTag;
  }
  
  
  public static HtmlTag makeSilentLink(String href, HtmlElement content) {
    HtmlTag link = new HtmlTag("a");
    link.addAttribute("href", "#");
    link.addAttribute("onclick", "doSilentRequest('" + href + "')");
    link.add(content);
    return link;
  }

}
