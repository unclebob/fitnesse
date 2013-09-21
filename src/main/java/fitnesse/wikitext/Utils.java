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

  public static String getStackTrace(Throwable e) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter pw = new PrintWriter(stringWriter);
    e.printStackTrace(pw);
    return stringWriter.toString();
  }
}
