// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.package fit;

package fit;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import fit.exception.CouldNotParseFitFailureException;
import fit.exception.FitFailureException;
import fit.exception.FitMatcherException;

public class Fixture {
  public Map<String, Object> summary = new HashMap<String, Object>();
  public Counts counts = new Counts();

  protected String[] args;

  private static final Map<String, Object> symbols = new HashMap<String, Object>();

  
  protected Class<?> getTargetClass() {
    return getClass();
  }

  public static void ClearSymbols() {
    symbols.clear();
  }

  public static Fixture loadFixture(String fixtureName) throws Throwable {
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

  public Object parse(String s, Class<?> type) throws Exception {
    if (type.equals(String.class)) {
      if (s.toLowerCase().equals("null"))
        return null;
      else if (s.toLowerCase().equals("blank"))
        return "";
      else
        return s;
    } else if (type.equals(Date.class)) {
      return DateFormat.getDateInstance(DateFormat.SHORT).parse(s);
    } else if (hasParseMethod(type)) {
      return callParseMethod(type, s);
    } else {
      throw new CouldNotParseFitFailureException(s, type.getName());
    }
  }

  public void check(Parse cell, TypeAdapter a) {
    String text = cell.text();
    if (text.equals(""))
      handleBlankCell(cell, a);
    else if (a == null)
      ignore(cell);
    else if (text.equals("error"))
      handleErrorInCell(a, cell);
    else
      compareCellToResult(a, cell);
  }

  private void compareCellToResult(TypeAdapter a, Parse cell) {
    new CellComparator().compareCellToResult(a, cell);
  }

  public void handleBlankCell(Parse cell, TypeAdapter a) {
    try {
      cell.addToBody(gray(a.toString(a.get())));
    } catch (Exception e) {
      cell.addToBody(gray("error"));
    }
  }

  private void handleErrorInCell(TypeAdapter a, Parse cell) {
    try {
      Object result = a.invoke();
      wrong(cell, a.toString(result));
    } catch (IllegalAccessException e) {
      exception(cell, e);
    } catch (Exception e) {
      right(cell);
    }
  }

  public String[] getArgs() {
    return Arrays.copyOf(args, args.length);
  }

  public static void setSymbol(String name, Object value) {
    symbols.put(name, (value == null) ? "null" : value);
  }

  public static Object getSymbol(String name) {
    return symbols.get(name);
  }

  public static boolean hasSymbol(String name) {
    return symbols.containsKey(name);
  }

  public static boolean hasParseMethod(Class<?> type) {
    try {
      type.getMethod("parse", new Class<?>[]
        {String.class});
      return true;
    } catch (NoSuchMethodException e) {
      return false;
    }
  }

  public static Object callParseMethod(Class<?> type, String s) throws Exception {
    Method parseMethod = type.getMethod("parse", new Class<?>[]
      {String.class});
    Object o = parseMethod.invoke(null, new Object[]
      {s});
    return o;
  }

  // TODO-RcM I might be moving out of here. Can you help me find a home of my
  // own?
  private class CellComparator {
    private Object result = null;

    private Object expected = null;

    private TypeAdapter typeAdapter;

    private Parse cell;

    private void compareCellToResult(TypeAdapter a, Parse theCell) {
      typeAdapter = a;
      cell = theCell;

      try {
        result = typeAdapter.get();
        expected = parseCell();
        if (expected instanceof Unparseable)
          tryRelationalMatch();
        else
          compare();
      } catch (Exception e) {
        exception(cell, e);
      }
    }

    private void compare() {
      if (typeAdapter.equals(expected, result)) {
        right(cell);
      } else {
        wrong(cell, typeAdapter.toString(result));
      }
    }

    private Object parseCell() {
      try {
        return typeAdapter.isRegex ? cell.text() : typeAdapter.parse(cell.text());
      }
      // Ignore parse exceptions, print non-parse exceptions,
      // return null so that compareCellToResult tries relational matching.
      catch (NumberFormatException e) {
      } catch (ParseException e) {
      } catch (Exception e) {
        e.printStackTrace();
      }
      return new Unparseable();
    }

    private void tryRelationalMatch() {
      Class<?> adapterType = typeAdapter.type;
      FitFailureException cantParseException = new CouldNotParseFitFailureException(cell.text(), adapterType
        .getName());
      if (result != null) {
        FitMatcher matcher = new FitMatcher(cell.text(), result);
        try {
          if (matcher.matches())
            right(cell);
          else
            wrong(cell);
          cell.body = matcher.message();
        } catch (FitMatcherException fme) {
          exception(cell, cantParseException);
        } catch (Exception e) {
          exception(cell, e);
        }
      } else {
        // TODO-RcM Is this always accurate?
        exception(cell, cantParseException);
      }
    }
  }

  private class Unparseable {
  }
}