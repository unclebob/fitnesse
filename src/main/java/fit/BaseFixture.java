// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.package fit;

package fit;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import fit.exception.FitFailureException;


public class BaseFixture {
  public Map<String, Object> summary = new HashMap<String, Object>();

  public Counts counts = new Counts();

  protected String[] args;


  protected Class<?> getTargetClass() {
    return getClass();
  }

  public static BaseFixture loadFixture(String fixtureName) throws Throwable {
    return FixtureLoader.instance().disgraceThenLoad(fixtureName);
  }

  public void getArgsForTable(Parse table) {
    List<String> argumentList = new ArrayList<String>();
    Parse parameters = table.parts.parts.more;
    for (; parameters != null; parameters = parameters.more)
      argumentList.add(parameters.text());

    args = argumentList.toArray(new String[argumentList.size()]);
  }

  public void doTable(Parse table) {
    doRows(table.parts.more);
  }

  public void doRows(Parse rows) {
    while (rows != null) {
      Parse more = rows.more;
      doRow(rows);
      rows = more;
    }
  }

  public void doRow(Parse row) {
    doCells(row.parts);
  }

  public void doCells(Parse cells) {
    for (int i = 0; cells != null; i++) {
      try {
        doCell(cells, i);
      } catch (Exception e) {
        exception(cells, e);
      }
      cells = cells.more;
    }
  }

  public void doCell(Parse cell, int columnNumber) {
    ignore(cell);
  }

  // Annotation ///////////////////////////////

  public void right(Parse cell) {
    cell.addToTag(" class=\"pass\"");
    counts.right++;
  }

  public void wrong(Parse cell) {
    cell.addToTag(" class=\"fail\"");
    counts.wrong++;
  }

  public void wrong(Parse cell, String actual) {
    wrong(cell);
    cell.addToBody(label("expected") + "<hr>" + escape(actual) + label("actual"));
  }

  public void ignore(Parse cell) {
    cell.addToTag(" class=\"ignore\"");
    counts.ignores++;
  }

  public void exception(Parse cell, Throwable exception) {
    while (exception.getClass().equals(InvocationTargetException.class)) {
      exception = ((InvocationTargetException) exception).getTargetException();
    }
    if (isFriendlyException(exception)) {
      cell.addToBody("<hr/>" + label(exception.getMessage()));
    } else {
      final StringWriter buf = new StringWriter();
      exception.printStackTrace(new PrintWriter(buf));
      cell.addToBody("<hr><pre><div class=\"fit_stacktrace\">" + (buf.toString()) + "</div></pre>");
    }
    cell.addToTag(" class=\"error\"");
    counts.exceptions++;
  }

  public boolean isFriendlyException(Throwable exception) {
    return exception instanceof FitFailureException;
  }

  // Utility //////////////////////////////////

  public String counts() {
    return counts.toString();
  }

  public static String label(String string) {
    return " <span class=\"fit_label\">" + string + "</span>";
  }

  public static String gray(String string) {
    return " <span class=\"fit_grey\">" + string + "</span>";
  }

  public static String escape(String string) {
    return escape(escape(string, '&', "&amp;"), '<', "&lt;");
  }

  public static String escape(String string, char from, String to) {
    int i = -1;
    while ((i = string.indexOf(from, i + 1)) >= 0) {
      if (i == 0) {
        string = to + string.substring(1);
      } else if (i == string.length()) {
        string = string.substring(0, i) + to;
      } else {
        string = string.substring(0, i) + to + string.substring(i + 1);
      }
    }
    return string;
  }

  public static String camel(String name) {
    StringBuffer b = new StringBuffer(name.length());
    StringTokenizer t = new StringTokenizer(name);
    b.append(t.nextToken());
    while (t.hasMoreTokens()) {
      String token = t.nextToken();
      b.append(token.substring(0, 1).toUpperCase()); // replace spaces with camelCase
      b.append(token.substring(1));
    }
    return b.toString();
  }

  public String[] getArgs() {
    return Arrays.copyOf(args, args.length);
  }
}