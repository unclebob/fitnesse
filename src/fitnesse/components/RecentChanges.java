// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import util.Clock;

import fitnesse.FitNesseContext;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class RecentChanges {
  private static final String RECENT_CHANGES = "RecentChanges";

  private static SimpleDateFormat makeDateFormat() {
    //SimpleDateFormat is not thread safe, so we need to create each instance independently.
    return new SimpleDateFormat(FitNesseContext.recentChangesDateFormat);
  }

  public static void updateRecentChanges(PageData pageData) {
    createRecentChangesIfNecessary(pageData);
    addCurrentPageToRecentChanges(pageData);
  }

  public static List<String> getRecentChangesLines(PageData recentChangesdata) {
    String content = recentChangesdata.getContent();
    BufferedReader reader = new BufferedReader(new StringReader(content));
    List<String> lines = new ArrayList<String>();
    String line = null;
    try {
      while ((line = reader.readLine()) != null)
        lines.add(line);
    } catch (IOException e) {
      // TODO: -AJM- It's only the recent changes file. Should we throw an error or just log to the console?
      throw new RuntimeException("Unable to read recent changes", e);
    }
    return lines;
  }

  private static void addCurrentPageToRecentChanges(PageData data) {
    WikiPage recentChanges = data.getWikiPage().getPageCrawler().getRoot(data.getWikiPage()).getChildPage(RECENT_CHANGES);
    String resource = resource(data);
    PageData recentChangesdata = recentChanges.getData();
    List<String> lines = getRecentChangesLines(recentChangesdata);
    removeDuplicate(lines, resource);
    lines.add(0, makeRecentChangesLine(data));
    trimExtraLines(lines);
    String content = convertLinesToWikiText(lines);
    recentChangesdata.setContent(content);
    recentChanges.commit(recentChangesdata);
  }

  private static String resource(PageData data) {
    WikiPagePath fullPath = data.getWikiPage().getPageCrawler().getFullPath(data.getWikiPage());
    String resource = PathParser.render(fullPath);
    return resource;
  }

  private static void createRecentChangesIfNecessary(PageData data) {
    PageCrawler crawler = data.getWikiPage().getPageCrawler();
    WikiPage root = crawler.getRoot(data.getWikiPage());
    if (!root.hasChildPage(RECENT_CHANGES))
      crawler.addPage(root, PathParser.parse(RECENT_CHANGES), "");
  }

  private static String makeRecentChangesLine(PageData data) {
    String user = data.getAttribute(PageData.LAST_MODIFYING_USER);
    if (user == null)
      user = "";
    return "|" + resource(data) + "|" + user + "|" + makeDateFormat().format(Clock.currentDate()) + "|";
  }

  private static void removeDuplicate(List<String> lines, String resource) {
    for (ListIterator<String> iterator = lines.listIterator(); iterator.hasNext();) {
      String s = iterator.next();
      if (s.startsWith("|" + resource + "|"))
        iterator.remove();
    }
  }

  private static String convertLinesToWikiText(List<String> lines) {
    StringBuffer buffer = new StringBuffer();
    for (Iterator<String> iterator = lines.iterator(); iterator.hasNext();) {
      String s = iterator.next();
      buffer.append(s).append("\n");
    }
    return buffer.toString();
  }

  private static void trimExtraLines(List<String> lines) {
    while (lines.size() > 100)
      lines.remove(100);
  }
}
