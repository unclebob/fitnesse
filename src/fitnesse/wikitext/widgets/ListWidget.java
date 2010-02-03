// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListWidget extends ParentWidget {
  public static final String REGEXP = "(?:^[ \\t]+[\\*\\d][^\n]*" + "\n" + "?)+";
  private static final Pattern pattern = Pattern.compile("([ \\t]+)([\\*\\d])([^\n]*)");

  private boolean ordered = false;
  private int level;

  public ListWidget(ParentWidget parent, String text) throws Exception {
    super(parent);
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      level = findLevel(match);
      ordered = !("*".equals(match.group(2)));
    }
    buildList(text);
  }

  private ListWidget(ParentWidget parent, Matcher match) {
    super(parent);
    level = findLevel(match);
    ordered = !("*".equals(match.group(2)));
  }

  private String buildList(String text) throws Exception {
    if (text == null)
      return null;
    Matcher match = pattern.matcher(text);
    if (match.find()) {
      int level = findLevel(match);
      if (level > this.level) {
        ListWidget childList = new ListWidget(this, match);
        String remainder = childList.buildList(text);
        return buildList(remainder);
      } else if (level < this.level)
        return text;
      else {
        String listItemContent = match.group(3).trim();
        // the trim is real important.  It removes the starting
        // spaces that could cause the item to be recognized
        // as another list.
        
        // Find next tree item on same layer to get the child items.
        Pattern nextPattern = Pattern.compile("([\\r\\n]+"+match.group(1)+"[\\*\\d])");
        
        Matcher nextMatch = nextPattern.matcher(text.substring(match.end()));
        
        String childItems;
        if(nextMatch.find())
        {
          // If there is a item on same layer, get text until this position -> child items.
          childItems = text.substring(match.end()).substring(0, nextMatch.start()).replaceAll("(^[\\r\\n]+)|([ \\r\\n]+$)", "");
        }else
        {
          // If there is no item on same layer, the text to the end are child items.
          childItems = text.substring(match.end()).replaceAll("(^[\\r\\n]+)|( [\\r\\n]+$)", "");
        }
        
        ListItemWidget item = new ListItemWidget(this, listItemContent, this.level + 1);
        
        // Add the child items as a new list to the list item.
        if(!childItems.isEmpty())
        {
          new ListWidget(item, childItems);
        }
        
        // Go on with building list after all child items.
        return buildList(text.substring(match.end()+childItems.length()));
        
      }
    } else
      return null;
  }

  public boolean isOrdered() {
    return ordered;
  }

  public int getLevel() {
    return level;
  }

  public String render() throws Exception {
    String tagValue = ordered ? "ol" : "ul";
    StringBuffer html = new StringBuffer();
    //appendTabs(html);
    html.append("<").append(tagValue).append(">").append("\n");
    html.append(childHtml());
    //appendTabs(html);
    html.append("</").append(tagValue).append(">").append("\n");

    return html.toString();
  }

  private void appendTabs(StringBuffer html) {
    for (int i = 0; i < level; i++)
      html.append("\t");
  }

  private int findLevel(Matcher match) {
    return match.group(1).length() - 1;
  }
}
