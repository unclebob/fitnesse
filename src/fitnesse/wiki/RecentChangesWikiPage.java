// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import fitnesse.util.Clock;

import fitnesse.FitNesseContext;

public class RecentChangesWikiPage implements RecentChanges {
  private static final Logger LOG = Logger.getLogger(RecentChangesWikiPage.class.getName());

  private static SimpleDateFormat makeDateFormat() {
    //SimpleDateFormat is not thread safe, so we need to create each instance independently.
    return new SimpleDateFormat(FitNesseContext.recentChangesDateFormat);
  }

  @Override
  public void updateRecentChanges(WikiPage page) {
    createRecentChangesIfNecessary(page);
    addCurrentPageToRecentChanges(page);
  }

  @Override
  public WikiPage toWikiPage(WikiPage root) {
    return root.getPageCrawler().getRoot().getChildPage(RECENT_CHANGES);
  }

  public List<String> getRecentChangesLines(PageData recentChangesdata) {
    String content = recentChangesdata.getContent();
    BufferedReader reader = null;
    List<String> lines = new ArrayList<>();
    try {
      reader = new BufferedReader(new StringReader(content));
      String line = null;
      while ((line = reader.readLine()) != null)
        lines.add(line);
    } catch (IOException e) {
      LOG.log(Level.WARNING, "Unable to read recent changes", e);
    } finally {
      try {
        if (reader != null) {
          reader.close();
        }
      } catch (IOException e) {
        LOG.log(Level.FINE, "Unable to close recent changes file for reading", e);
      }
    }
    return lines;
  }

  private void addCurrentPageToRecentChanges(WikiPage page) {
    WikiPage recentChanges = page.getPageCrawler().getRoot().getChildPage(RECENT_CHANGES);
    String resource = resource(page);
    PageData recentChangesData = recentChanges.getData();
    List<String> lines = getRecentChangesLines(recentChangesData);
    removeDuplicate(lines, resource);
    lines.add(0, makeRecentChangesLine(page));
    trimExtraLines(lines);
    String content = convertLinesToWikiText(lines);
    recentChangesData.setContent(content);
    recentChanges.commit(recentChangesData);

  }

  private String resource(WikiPage page) {
    WikiPagePath fullPath = page.getPageCrawler().getFullPath();
    String resource = PathParser.render(fullPath);
    return resource;
  }

  private void createRecentChangesIfNecessary(WikiPage page) {
    PageCrawler crawler = page.getPageCrawler();
    WikiPage root = crawler.getRoot();
    if (!root.hasChildPage(RECENT_CHANGES))
      WikiPageUtil.addPage(root, PathParser.parse(RECENT_CHANGES), "");
  }

  private String makeRecentChangesLine(WikiPage page) {
    String user = page.getData().getAttribute(PageData.LAST_MODIFYING_USER);
    if (user == null)
      user = "";
    return "|" + resource(page) + "|" + user + "|" + makeDateFormat().format(Clock.currentDate()) + "|";
  }

  private void removeDuplicate(List<String> lines, String resource) {
    for (ListIterator<String> iterator = lines.listIterator(); iterator.hasNext();) {
      String s = iterator.next();
      if (s.startsWith("|" + resource + "|"))
        iterator.remove();
    }
  }

  private String convertLinesToWikiText(List<String> lines) {
    StringBuilder buffer = new StringBuilder();
    for (String line : lines) {
      buffer.append(line).append("\n");
    }
    return buffer.toString();
  }

  private void trimExtraLines(List<String> lines) {
    while (lines.size() > 100)
      lines.remove(100);
  }
}
