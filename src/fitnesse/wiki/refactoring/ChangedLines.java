// Copyright (C) 2026 by netcare GmbH. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import difflib.Delta;
import difflib.DiffUtils;
import fitnesse.wiki.PageData;

/**
 * Consecutive lines of a page before and after a change.
 */
public class ChangedLines {
  private final String before;
  private final String after;

  public ChangedLines(String before, String after) {
    this.before = before;
    this.after = after;
  }

  /**
   * @param oldContent page content before the change
   * @param newContent page content after the change
   * @return the changed parts of the content, empty if there are none
   */
  public static List<ChangedLines> between(String oldContent, String newContent) {
    List<ChangedLines> changes = new ArrayList<>();
    for (Delta<String> delta : DiffUtils.diff(lines(oldContent), lines(newContent)).getDeltas()) {
      changes.add(new ChangedLines(join(delta.getOriginal().getLines()), join(delta.getRevised().getLines())));
    }
    return changes;
  }

  private static List<String> lines(String content) {
    return Arrays.asList(content.split(PageData.PAGE_LINE_SEPARATOR));
  }

  private static String join(List<String> lines) {
    return String.join(PageData.PAGE_LINE_SEPARATOR, lines);
  }

  public String getBefore() {
    return before;
  }

  public String getAfter() {
    return after;
  }
}
