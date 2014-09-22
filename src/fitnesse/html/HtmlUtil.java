// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

public class HtmlUtil {
  public static final HtmlElement BR = new RawHtml("<br/>");
  public static final HtmlElement HR = new RawHtml("<hr/>");
  public static final HtmlElement NBSP = new RawHtml("&nbsp;");

  private static final String[] specialHtmlChars = new String[]{"&", "<", ">"};
  private static final String[] specialHtmlEscapes = new String[]{"&amp;", "&lt;", "&gt;"};
  private static final String[] specialWikiChars = new String[]{"!", "|", "$"};
  private static final String[] specialWikiEscapes = new String[]{"&bang;", "&bar;", "&dollar;"};

  public static HtmlTag makeDivTag(String divClass) {
    HtmlTag div = new HtmlTag("div");
    div.addAttribute("class", divClass);
    div.add("");
    return div;
  }

  public static HtmlTag makeBold(String content) {
    HtmlTag bold = new HtmlTag("b");
    bold.add(content);
    return bold;
  }

  public static HtmlTag makeSpanTag(String spanClass, String content) {
    HtmlTag span = new HtmlTag("span");
    span.addAttribute("class", spanClass);
    span.add(content);
    return span;
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
  
  public static HtmlTag makeToggleClassScript(String idElement, String classToToggle) {
    HtmlTag scriptTag = new HtmlTag("script");
    scriptTag.add("$(\"#" + idElement + "\").toggleClass(\"" + classToToggle + "\");");
    return scriptTag;
  }
  
  public static HtmlTag makeInitErrorMetadataScript() {
    HtmlTag scriptTag = new HtmlTag("script");
    scriptTag.add("initErrorMetadata();");
    return scriptTag;
  }
  
  public static HtmlTag makeSilentLink(String href, HtmlElement content) {
    HtmlTag link = new HtmlTag("a");
    link.addAttribute("href", "#");
    link.addAttribute("onclick", "doSilentRequest('" + href + "')");
    link.add(content);
    return link;
  }

  public static String escapeHTML(String value) {
      return replaceStrings(value, specialHtmlChars, specialHtmlEscapes);
  }

  private static String replaceStrings(String value, String[] originalStrings, String[] replacementStrings) {
        String result = value;
        for (int i = 0; i < originalStrings.length; i++)
            if (result.contains(originalStrings[i]))
                result = result.replace(originalStrings[i], replacementStrings[i]);
        return result;
    }

  public static String unescapeHTML(String value) {
        return replaceStrings(value, specialHtmlEscapes, specialHtmlChars);
    }

  public static String unescapeWiki(String value) {
      return replaceStrings(value, specialWikiEscapes, specialWikiChars);
  }

  public static String escapeWiki(String value) {
      return replaceStrings(value, specialWikiChars, specialWikiEscapes);
  }
}
