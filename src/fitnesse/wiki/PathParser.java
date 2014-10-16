// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.Iterator;
import java.util.regex.Pattern;

public class PathParser {
  public static final String PATH_SEPARATOR = ".";

  private static final Pattern WIKI_WORD_PATTERN = Pattern.compile("\\w[\\w-]*");
  private static final Pattern WIKI_PATH_PATTERN = Pattern.compile("[<>^\\.]?\\w[\\w-]*(\\.\\w[\\w-]+)*");

  private WikiPagePath path;

  public static WikiPagePath parse(String pathName) {
    return new PathParser().makePath(pathName, new WikiPagePath());
  }

  private static WikiPagePath makePath(String pathName, WikiPagePath path) {
    if (pathName.equals("")) {
      return path;
    } else if (pathName.equals("root") || pathName.equals(PATH_SEPARATOR) || pathName.equals("/")) {
      path.makeAbsolute();
      return path;
    } else {
      return parsePathName(pathName, path);
    }
  }

  private static WikiPagePath parsePathName(String pathName, WikiPagePath path) {
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
      if (isWikiPath(pageName))
        path.addNameToEnd(pageName);
      else
        return null;
    }
    return path;
  }

  public static boolean isSingleWikiWord(String name) {
    return WIKI_WORD_PATTERN.matcher(name).matches()
            && !"files".equals(name)
            && !"root".equals(name);
  }

  public static boolean isWikiPath(String name) {
    return WIKI_PATH_PATTERN.matcher(name).matches();
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
