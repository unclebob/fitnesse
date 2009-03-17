// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.WikiWidget;

public class HruleWidget extends WikiWidget {
  public static final String REGEXP = "-{4,}";
  private static final Pattern pattern = Pattern.compile("-{4}(-*)");

  private int extraDashes = 0;

  public HruleWidget(ParentWidget parent, String text) {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find())
      extraDashes = match.group(1).length();
  }

  public int getExtraDashes() {
    return extraDashes;
  }

  public String render() throws Exception {
    HtmlTag hr = new HtmlTag("hr");
    if (extraDashes > 0)
      hr.addAttribute("size", hrSize(extraDashes));
    return hr.html();
  }

  private String hrSize(int height) {
    int hrSize = height + 1;
    return String.format("%d", hrSize);
  }
}

