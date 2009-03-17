// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.wikitext.WikiWidget;

public class ImageWidget extends WikiWidget {
  public static final String REGEXP = "(?:!img(?:-[lr])? \\S+)|(?:" + LinkWidget.REGEXP + ".(?:gif|jpg|GIF|JPG))";
  private static final Pattern pattern = Pattern.compile("(!img(-[lr])? )?(\\S*)");

  private String picturePath;
  private String alignment;
  private boolean usesBangImg;

  public ImageWidget(ParentWidget parent, String text) {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      picturePath = LinkWidget.makeUrlUsable(match.group(3));
      usesBangImg = match.group(1) != null;
      if (usesBangImg)
        alignment = match.group(2);
    } else
      System.err.println("ImagesWidget parse error: " + text);
  }

  public String render() throws Exception {
    StringBuffer html = new StringBuffer("<img src=\"");
    html.append(picturePath).append("\"");
    if (alignment != null) {
      html.append(" class=\"");
      if ("-l".equals(alignment))
        html.append("left");
      else
        html.append("right");
      html.append("\"");
    }
    html.append("/>");

    return html.toString();
  }

  public String asWikiText() throws Exception {
    String pathString = picturePath;
    if (pathString.startsWith("/files"))
      pathString = "http:/" + pathString;

    if (usesBangImg) {
      final String alignmentString = (alignment == null ? "" : alignment);
      return "!img" + alignmentString + " " + pathString;
    } else
      return pathString;
  }

}


