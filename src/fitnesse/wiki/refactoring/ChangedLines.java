// Copyright (C) 2026 by netcare GmbH. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import difflib.Chunk;
import difflib.Delta;
import difflib.DiffUtils;
import fitnesse.html.HtmlUtil;
import fitnesse.wiki.PageData;

/**
 * Consecutive lines of a page before and after a change.
 */
public class ChangedLines {
  // words, and every other character on its own
  private static final Pattern TOKEN = Pattern.compile("\\w+|\\W", Pattern.UNICODE_CHARACTER_CLASS);

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

  /**
   * @return the lines before the change as HTML, with the words that change marked
   */
  public String getBeforeHtml() {
    List<String> beforeTokens = tokens(before);
    List<Chunk<String>> changed = new ArrayList<>();
    for (Delta<String> delta : DiffUtils.diff(beforeTokens, tokens(after)).getDeltas()) {
      changed.add(delta.getOriginal());
    }
    return markChanges(beforeTokens, changed);
  }

  /**
   * @return the lines after the change as HTML, with the changed words marked
   */
  public String getAfterHtml() {
    List<String> afterTokens = tokens(after);
    List<Chunk<String>> changed = new ArrayList<>();
    for (Delta<String> delta : DiffUtils.diff(tokens(before), afterTokens).getDeltas()) {
      changed.add(delta.getRevised());
    }
    return markChanges(afterTokens, changed);
  }

  private static List<String> tokens(String text) {
    List<String> tokens = new ArrayList<>();
    Matcher matcher = TOKEN.matcher(text);
    while (matcher.find()) {
      tokens.add(matcher.group());
    }
    return tokens;
  }

  private static String markChanges(List<String> tokens, List<Chunk<String>> changedChunks) {
    StringBuilder html = new StringBuilder();
    int position = 0;
    for (Chunk<String> chunk : changedChunks) {
      appendEscaped(html, tokens.subList(position, chunk.getPosition()));
      if (chunk.size() > 0) {
        html.append("<mark>");
        appendEscaped(html, chunk.getLines());
        html.append("</mark>");
      }
      position = chunk.getPosition() + chunk.size();
    }
    appendEscaped(html, tokens.subList(position, tokens.size()));
    return html.toString();
  }

  private static void appendEscaped(StringBuilder html, List<String> tokens) {
    html.append(HtmlUtil.escapeHTML(String.join("", tokens)));
  }
}
