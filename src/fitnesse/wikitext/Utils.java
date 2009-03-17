// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Utils {

  private static final String[] specialHtmlChars = new String[]{"&", "<", ">"};
  private static final String[] specialHtmlEscapes = new String[]{"&amp;", "&lt;", "&gt;"};
  private static final String[] specialWikiChars = new String[]{"!", "|", "$"};
  private static final String[] specialWikiEscapes = new String[]{"&bang;", "&bar;", "&dollar;"};

  public static String escapeHTML(String value) {
    for (int i = 0; i < specialHtmlChars.length; i++)
      value = value.replaceAll(specialHtmlChars[i], specialHtmlEscapes[i]);
    return value;
  }

  public static String unescapeHTML(String value) {
    for (int i = 0; i < specialHtmlChars.length; i++)
      value = value.replaceAll(specialHtmlEscapes[i], specialHtmlChars[i]);
    return value;
  }

  public static String unescapeWiki(String value) {
    for (int i = 0; i < specialWikiChars.length; i++)
      value = value.replace(specialWikiEscapes[i], specialWikiChars[i]);
    return value;
  }

  public static String escapeWiki(String value) {
    for (int i = 0; i < specialWikiChars.length; i++)
      value = value.replace(specialWikiChars[i], specialWikiEscapes[i]);
    return value;
  }

  public static String getStackTrace(Throwable e) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter pw = new PrintWriter(stringWriter);
    e.printStackTrace(pw);
    return stringWriter.toString();
  }
}
