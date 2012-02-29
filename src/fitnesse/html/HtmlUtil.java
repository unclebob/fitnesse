// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import org.apache.velocity.VelocityContext;

import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.responders.templateUtilities.HtmlPage;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.ProxyPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageActions;
import fitnesse.wiki.WikiPagePath;

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
    page.setTitle(title);
    page.setPageTitle(new PageTitle(title));
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

  public static String makeNormalWikiPageContent(PageData pageData) {
    SetupTeardownAndLibraryIncluder.includeInto(pageData);
    return makePageHtmlWithHeaderAndFooter(pageData);
  }

  public static String makePageHtmlWithHeaderAndFooter(PageData pageData) {
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
