// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import java.util.regex.Pattern;

public class HtmlUtil {
  public static final HtmlElement BR = new RawHtml("<br/>");
  public static final HtmlElement HR = new RawHtml("<hr/>");
  public static final HtmlElement NBSP = new RawHtml("&nbsp;");

  private static final String[] specialHtmlChars = new String[]{"&", "<", ">"};
  private static final String[] specialHtmlEscapes = new String[]{"&amp;", "&lt;", "&gt;"};
  private static final String[] specialWikiChars = new String[]{"!", "|", "$"};
  private static final String[] specialWikiEscapes = new String[]{"&bang;", "&bar;", "&dollar;"};

  // Source: http://dev.w3.org/html5/markup/common-models.html
  public static final String HTML_CELL_CONTENT_PATTERN_TEXT = "<(p|hr|pre|ul|ol|dl|div|h[1-6]|hgroup|address|" +
              "blockquote|ins|del|object|map|video|audio|figure|table|fieldset|canvas|a|em|strong|small|mark|" +
              "abbr|dfn|i|b|s|u|code|var|samp|kbd|sup|sub|q|cite|span|br|ins|del|img|embed|object|video|audio|label|" +
              "output|datalist|progress|command|canvas|time|meter)([ >].*</\\1>|[^>]*/>)";
  private static final Pattern HTML_PATTERN = Pattern.compile("^" + HTML_CELL_CONTENT_PATTERN_TEXT + "$",
                                                        Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

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

  public static boolean isValidTableCellContent(String text) {
    // performance improvement: First check 1st character.
    return text.startsWith("<") && HTML_PATTERN.matcher(text).matches();
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
