// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.Iterator;
import java.util.regex.Pattern;

import fitnesse.wikitext.widgets.WikiWordWidget;

public class PathParser {
  public static final String PATH_SEPARATOR = ".";

  public static final String PATH_PREFIX_CHARS = ".<>^"; //..."^" is deprecated
  private static final Pattern wikiWordPattern = Pattern.compile(WikiWordWidget.REGEXP);
  private WikiPagePath path;

  public static WikiPagePath parse(String pathName) {
    return new PathParser().makePath(pathName);
  }

  private WikiPagePath makePath(String pathName) {
    path = new WikiPagePath();
    if (pathName.equals("")) {
      return path;
    } else if (pathName.equals("root") || pathName.equals(PATH_SEPARATOR)) {
      path.makeAbsolute();
      return path;
    } else {
      return parsePathName(pathName);
    }
  }

  private WikiPagePath parsePathName(String pathName) {
    if (pathName.startsWith(PATH_SEPARATOR)) {
      path.makeAbsolute();
      pathName = pathName.substring(1);
    } else if (pathName.startsWith(">") || pathName.startsWith("^")) {
      path.setPathMode(WikiPagePath.Mode.SUB_PAGE);
      pathName = pathName.substring(1);
    } else if (pathName.startsWith("<")) {
      path.setPathMode(WikiPagePath.Mode.BACKWARD_SEARCH);
      pathName = pathName.substring(1);
    }
    String[] names = pathName.split("\\" + PATH_SEPARATOR);
    for (int i = 0; i < names.length; i++) {
      String pageName = names[i];
      if (nameIsValid(pageName))
        path.addNameToEnd(pageName);
      else
        return null;
    }
    return path;
  }

  public static boolean isPathPrefix(Character c) {
    return PATH_PREFIX_CHARS.indexOf(c) >= 0;
  }

  private static boolean nameIsValid(String name) {
    return wikiWordPattern.matcher(name).matches();
  }

  public static String render(WikiPagePath path) {
    StringBuffer renderedPath = new StringBuffer();
    if (path.isSubPagePath())
      renderedPath.append(">");
    else if (path.isBackwardSearchPath())
      renderedPath.append("<");
    else if (path.isAbsolute()) {
      renderedPath.append(".");
    }

    Iterator<?> i = path.getNames().iterator();
    if (i.hasNext()) {
      String name = (String) i.next();
      renderedPath.append(name);
    }
    while (i.hasNext()) {
      renderedPath.append(PATH_SEPARATOR).append(i.next());
    }
    return renderedPath.toString();
  }
}
