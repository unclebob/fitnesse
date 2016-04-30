// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

public class SummaryFixture extends Fixture {
  public static final String countsKey = "counts";

  @Override
  public void doTable(Parse table) {
    summary.put(countsKey, counts());
    SortedSet<String> keys = new TreeSet<>(summary.keySet());
    table.parts.more = rows(keys.iterator());
  }

  protected Parse rows(Iterator<String> keys) {
    if (keys.hasNext()) {
      String key = keys.next();
      Parse result =
        tr(
          td(key,
            td(summary.get(key).toString(),
              null)),
          rows(keys));
      if (key.equals(countsKey)) {
        mark(result);
      }
      return result;
    } else {
      return null;
    }
  }

  protected Parse tr(Parse parts, Parse more) {
    return new Parse("tr", null, parts, more);
  }

  protected Parse td(String body, Parse more) {
    return new Parse("td", gray(body), null, more);
  }

  protected void mark(Parse row) {
    // mark summary good/bad without counting beyond here
    Counts official = counts;
    counts = new Counts();
    Parse cell = row.parts.more;
    if (official.wrong + official.exceptions > 0) {
      wrong(cell);
    } else {
      right(cell);
    }
    counts = official;
  }

}
