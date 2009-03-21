// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import fitnesse.wiki.PageCrawlerImpl;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.ProxyPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

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


  public static class ActionLink {
    public ActionLink(String pageName, String linkName) {
      this.pageName = pageName;
      this.linkName = linkName;
      this.query = linkName.toLowerCase();
      this.shortcutKey = query.substring(0, 1);
      this.newWindow = false;
    }

    private String pageName;
    private String linkName;
    private String query;
    private String shortcutKey;
    private boolean newWindow;

    public void setPageName(String pageName) {
      this.pageName = pageName;
    }

    public void setLinkName(String linkName) {
      this.linkName = linkName;
    }

    public void setQuery(String inputName) {
      this.query = inputName;
    }

    public void setShortcutKey(String shortcutKey) {
      this.shortcutKey = shortcutKey;
    }

    public void setNewWindow(boolean newWindow) {
      this.newWindow = newWindow;
    }

    public HtmlTag getHtml() {
      String name = linkName;
      String inputName = query;
      String href = pageName;
      if (inputName != null)
        href = href + "?" + inputName;

      HtmlTag linkTag = new HtmlTag("a");
      linkTag.addAttribute("href", href);
      if (newWindow)
        linkTag.addAttribute("target", "newWindow");
      linkTag.addAttribute("accesskey", shortcutKey);
      linkTag.add(name);
      TagGroup group = new TagGroup();
      group.add(new HtmlComment(name + " button"));
      group.add(linkTag);
      return group;
    }
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

  public static HtmlTag makeBreadCrumbsWithPageType(String trail, String type)
    throws Exception {
    return makeBreadCrumbsWithPageType(trail, ".", type);
  }

  public static HtmlTag makeBreadCrumbsWithPageType(
    String trail,
    String separator, String type
  ) throws Exception {
    TagGroup group = makeBreadCrumbsWithCurrentPageLinked(trail, separator);
    group.add(HtmlUtil.BR);
    group.add(HtmlUtil.makeSpanTag("page_type", type));
    return group;
  }

  private static String makeAllButLastCrumb(
    String[] crumbs,
    String separator, TagGroup group
  ) {
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

  public static HtmlTag makeActions(PageData pageData) throws Exception {
    WikiPage page = pageData.getWikiPage();

    WikiPagePath localPagePath = page.getPageCrawler().getFullPath(page);
    String localPageName = PathParser.render(localPagePath);
    String localOrRemotePageName = localPageName;
    boolean newWindowIfRemote = NO_NEW_WINDOW;
    if (page instanceof ProxyPage) {
      ProxyPage proxyPage = (ProxyPage) page;
      localOrRemotePageName = proxyPage.getThisPageUrl();
      newWindowIfRemote = true;
    }
    return makeActions(pageData, localPageName, localOrRemotePageName,
      newWindowIfRemote
    );
  }

  public static HtmlTag makeActions(
    PageData pageData, String localPageName,
    String localOrRemotePageName, boolean newWindowIfRemote
  )
    throws Exception {
    TagGroup actions = new TagGroup();
    if (isTestPage(pageData)) {
      ActionLink link = new ActionLink(localPageName, "Test");
      link.setQuery("test");
      addLinkToActions(actions, link);
    }
    if (isSuitePage(pageData)) {
      ActionLink link = new ActionLink(localPageName, "Suite");
      link.setShortcutKey("");
      addLinkToActions(actions, link);
    }
    if (pageData.hasAttribute("Edit")) {
      ActionLink link = new ActionLink(localOrRemotePageName, "Edit");
      link.setNewWindow(newWindowIfRemote);
      addLinkToActions(actions, link);
    }
    if (pageData.hasAttribute("Properties")) {
      ActionLink link = new ActionLink(localOrRemotePageName, "Properties");
      link.setNewWindow(newWindowIfRemote);
      addLinkToActions(actions, link);
    }
    if (pageData.hasAttribute("Refactor")) {
      ActionLink link = new ActionLink(localOrRemotePageName, "Refactor");
      link.setNewWindow(newWindowIfRemote);
      addLinkToActions(actions, link);
    }
    if (pageData.hasAttribute("WhereUsed")) {
      ActionLink link = new ActionLink(localOrRemotePageName, "Where Used");
      link.setNewWindow(newWindowIfRemote);
      link.setQuery("whereUsed");
      addLinkToActions(actions, link);
    }
    if (pageData.hasAttribute("Search")) {
      ActionLink link = new ActionLink("", "Search");
      link.setQuery("searchForm");
      addLinkToActions(actions, link);
    }
    if (pageData.hasAttribute("Files")) {
      ActionLink link = new ActionLink("/files", "Files");
      link.setQuery(null);
      addLinkToActions(actions, link);
    }
    if (pageData.hasAttribute("Versions")) {
      ActionLink link = new ActionLink(localOrRemotePageName, "Versions");
      link.setNewWindow(newWindowIfRemote);
      addLinkToActions(actions, link);
    }
    if (pageData.hasAttribute("RecentChanges")) {
      ActionLink link = new ActionLink("/RecentChanges", "Recent Changes");
      link.setQuery(null);
      link.setShortcutKey("");
      addLinkToActions(actions, link);
    }
    if (pageData.hasAttribute("StopAll")) {
      ActionLink link = new ActionLink("?stoptest", "Stop All Tests");
      link.setQuery(null);
      addLinkToActions(actions, link);
    }
    ActionLink userGuideLink = new ActionLink(".FitNesse.UserGuide", "User Guide");
    userGuideLink.setQuery(null);
    userGuideLink.setShortcutKey("");
    actions.add(userGuideLink.getHtml());
    return actions;
  }

  private static void addLinkToActions(TagGroup actions, ActionLink link) {
    actions.add(link.getHtml());
    actions.add(makeNavBreak());
  }

  private static boolean isSuitePage(PageData pageData) throws Exception {
    return pageData.hasAttribute("Suite");
  }

  public static HtmlTag makeNavBreak() {
    HtmlTag navBreak = new HtmlTag("div");
    navBreak.addAttribute("class", "nav_break");
    navBreak.add("&nbsp;");
    return navBreak;
  }

  public static String makeNormalWikiPageContent(PageData pageData)
    throws Exception {
    SetupTeardownIncluder.includeInto(pageData);
    String content = pageData.getHtml();
    return addHeaderAndFooter(pageData.getWikiPage(), content);
  }

  public static String addHeaderAndFooter(WikiPage page, String content)
    throws Exception {
    StringBuffer buffer = new StringBuffer();
    buffer.append(getHtmlOfInheritedPage("PageHeader", page));
    buffer.append(content);
    buffer.append("<br/><div class=\"footer\">\n");
    buffer.append(getHtmlOfInheritedPage("PageFooter", page));
    buffer.append("</div>\n");
    return buffer.toString();
  }

  private static boolean isTestPage(PageData pageData) throws Exception {
    return pageData.hasAttribute("Test");
  }

  public static String getHtmlOfInheritedPage(
    String pageName,
    WikiPage context
  ) throws Exception {
    return getLabeledHtmlOfInheritedPage(pageName, context, "");
  }

  public static String getLabeledHtmlOfInheritedPage(
    String pageName,
    WikiPage context, String label
  ) throws Exception {
    WikiPage inheritedPage = PageCrawlerImpl.getInheritedPage(pageName,
      context
    );
    if (inheritedPage != null) {
      PageData data = inheritedPage.getData();
      if (label != null && label.length() > 1) {
        WikiPagePath inheritedPagePath = context.getPageCrawler()
          .getFullPath(inheritedPage);
        String inheritedPagePathName = PathParser
          .render(inheritedPagePath);
        String fullLabel = "!meta " + label + ": ."
          + inheritedPagePathName + "\n";
        String newContent = fullLabel + data.getContent();
        data.setContent(newContent);
      }
      return data.getHtml(context);
    } else
      return "";
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
