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

  public static String escapeHTML(String value) {
      return replaceStrings(value, specialHtmlChars, specialHtmlEscapes);
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

  private static String replaceStrings(String value, String[] originalStrings, String[] replacementStrings) {
    String result = value;
    for (int i = 0; i < originalStrings.length; i++)
      if (result.contains(originalStrings[i]))
        result = result.replace(originalStrings[i], replacementStrings[i]);
    return result;
  }
}
