// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.package fit;

package fit;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import fit.exception.CouldNotParseFitFailureException;
import fit.exception.FitFailureException;
import fit.exception.FitMatcherException;


public class Fixture extends BaseFixture {
  @Deprecated
  public FixtureListener listener = new NullFixtureListener();

  private static final Map<String, Object> symbols = new HashMap<String, Object>();


  @Deprecated
  public static void setForcedAbort(boolean state) {
    Dispatcher.setForcedAbort(state);
  }  //Semaphores

  protected Class<?> getTargetClass() {
    return getClass();
  }

  @Deprecated
  public class RunTime extends Dispatcher.RunTime { }

  // Traversal //////////////////////////
  @Deprecated
  public void doTables(Parse tables) {
    summary.put("run date", new Date());
    summary.put("run elapsed time", new RunTime());
    if (tables != null) {
      Parse heading = tables.at(0, 0, 0);
      if (heading != null) {
        try {
          Fixture fixture = getLinkedFixtureWithArgs(tables);
          fixture.listener = listener;
          fixture.interpretTables(tables);
        } catch (Throwable e) {
          exception(heading, e);
          interpretFollowingTables(tables);
        }
      }
    }
    listener.tablesFinished(counts);
    ClearSymbols();
    SemaphoreFixture.ClearSemaphores(); //Semaphores:  clear all at end
  }

  public static void ClearSymbols() {
    symbols.clear();
  }

  @Deprecated
  protected void interpretTables(Parse tables) {
    try { // Don't create the first fixture again, because creation may do something important.
      getArgsForTable(tables); // get them again for the new fixture object
      doTable(tables);
    } catch (Exception ex) {
      exception(tables.at(0, 0, 0), ex);
      listener.tableFinished(tables);
      return;
    }
    interpretFollowingTables(tables);
  }

  @Deprecated
  private void interpretFollowingTables(Parse tables) {
    listener.tableFinished(tables);
    tables = tables.more;
    while (tables != null) {
      Parse heading = tables.at(0, 0, 0);

      if (Dispatcher.aborting()) ignore(heading);  //Semaphores: ignore on failed lock
      else if (heading != null) {
        try {
          BaseFixture fixture = getLinkedFixtureWithArgs(tables);
          fixture.doTable(tables);
        } catch (Throwable e) {
          exception(heading, e);
        }
      }
      listener.tableFinished(tables);
      tables = tables.more;
    }
  }

  @Deprecated
  protected Fixture getLinkedFixtureWithArgs(Parse tables) throws Throwable {
    Parse header = tables.at(0, 0, 0);
    Fixture fixture = loadFixture(header.text());
    fixture.counts = counts;
    fixture.summary = summary;
    fixture.getArgsForTable(tables);
    return fixture;
  }

  public static Fixture loadFixture(String fixtureName) throws Throwable {
    return (Fixture) BaseFixture.loadFixture(fixtureName);
  }

  // Utility //////////////////////////////////

  @Deprecated
  public static String label(String string) {
    return BaseFixture.label(string);
  }

  @Deprecated
  public static String gray(String string) {
    return BaseFixture.gray(string);
  }

  @Deprecated
  public static String escape(String string) {
    return BaseFixture.escape(string);
  }

  @Deprecated
  public static String escape(String string, char from, String to) {
	return BaseFixture.escape(string, from, to);
  }

  @Deprecated
  public static String camel(String name) {
	return BaseFixture.camel(name);
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