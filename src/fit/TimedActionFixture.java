// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

package fit;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimedActionFixture extends ActionFixture {
  private static SimpleDateFormat makeDateFormat() {
    //SimpleDateFormat is not thread safe, so we need to create each instance independently.    
    return new SimpleDateFormat("hh:mm:ss");
  }

  public void doTable(Parse table) {
    super.doTable(table);
    table.parts.parts.last().more = td("time");
    table.parts.parts.last().more = td("split");
  }

  public void doCells(Parse cells) {
    Date start = time();
    super.doCells(cells);
    long split = time().getTime() - start.getTime();
    cells.last().more = td(makeDateFormat().format(start));
    cells.last().more = td(split < 1000 ? "&nbsp;" : Double.toString((split) / 1000.0));
  }

  // Utility //////////////////////////////////

  public Date time() {
    return new Date();
  }

  public Parse td(String body) {
    return new Parse("td", gray(body), null, null);
  }

}
