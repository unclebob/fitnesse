// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import util.GracefulNamer;
import fit.exception.FitFailureException;
import fit.exception.NoSuchFieldFitFailureException;
import fit.exception.NoSuchMethodFitFailureException;

public abstract class Binding {
  private static Pattern regexMethodPattern = Pattern.compile("(.+)(?:\\?\\?|!!)");
  private static Pattern methodPattern = Pattern.compile("(.+)(?:\\(\\)|\\?|!)");
  private static Pattern fieldPattern = Pattern.compile("=?([^=]+)=?");

  public TypeAdapter adapter;

  public static Binding create(Fixture fixture, String name) throws Throwable {
    Binding binding = null;

    if (name.startsWith("="))
      binding = new SaveBinding();
    else if (name.endsWith("="))
      binding = new RecallBinding();
    else if (regexMethodPattern.matcher(name).matches())
      binding = new RegexQueryBinding();
    else if (methodPattern.matcher(name).matches())
      binding = new QueryBinding();
    else if (fieldPattern.matcher(name).matches())
      binding = new SetBinding();

    if (binding == null)
      binding = new NullBinding();
    else
      binding.adapter = makeAdapter(fixture, name);

    return binding;
  }

  private static TypeAdapter makeAdapter(Fixture fixture, String name) throws Throwable {
    Matcher regexMatcher = regexMethodPattern.matcher(name);
    if (regexMatcher.find())
      return makeAdapterForRegexMethod(name, fixture, regexMatcher);
    else {
      Matcher methodMatcher = methodPattern.matcher(name);
      if (methodMatcher.find())
        return makeAdapterForMethod(name, fixture, methodMatcher);
      else
        return makeAdapterForField(name, fixture);
    }
  }

  private static TypeAdapter makeAdapterForField(String name, Fixture fixture) {
    Field field = null;
    if (GracefulNamer.isGracefulName(name)) {
      String simpleName = GracefulNamer.disgrace(name).toLowerCase();
      field = findField(fixture, simpleName);
    } else {
      try {
        Matcher matcher = fieldPattern.matcher(name);
        matcher.find();
        String fieldName = matcher.group(1);
        field = fixture.getTargetClass().getField(fieldName);
      }
      catch (NoSuchFieldException e) {
      }
    }

    if (field == null)
      throw new NoSuchFieldFitFailureException(name);
    return TypeAdapter.on(fixture, field);
  }

  private static TypeAdapter makeAdapterForMethod(String name, Fixture fixture, Matcher matcher) {
    Method method = getMethod(name, fixture, matcher);

    if (method == null)
      throw new NoSuchMethodFitFailureException(name);
    return TypeAdapter.on(fixture, method, false);
  }

  private static TypeAdapter makeAdapterForRegexMethod(String name, Fixture fixture, Matcher matcher) {
    Method method = getMethod(name, fixture, matcher);

    if (method == null)
      throw new NoSuchMethodFitFailureException(name);
    return TypeAdapter.on(fixture, method, true);
  }

  private static Method getMethod(String name, Fixture fixture, Matcher matcher) {
    Method method = null;
    if (GracefulNamer.isGracefulName(name)) {
      String simpleName = GracefulNamer.disgrace(name).toLowerCase();
      method = findMethod(fixture, simpleName);
    } else {
      try {
        String methodName = matcher.group(1);
        method = fixture.getTargetClass().getMethod(methodName, new Class[]{});
      }
      catch (NoSuchMethodException e) {
      }
    }

    return method;
  }

  private static Field findField(Fixture fixture, String simpleName) {
    Field[] fields = fixture.getTargetClass().getFields();
    Field field = null;
    for (int i = 0; i < fields.length; i++) {
      Field possibleField = fields[i];
      if (simpleName.equals(possibleField.getName().toLowerCase())) {
        field = possibleField;
        break;
      }
    }
    return field;
  }

  private static Method findMethod(Fixture fixture, String simpleName) {
    Method[] methods = fixture.getTargetClass().getMethods();
    Method method = null;
    for (int i = 0; i < methods.length; i++) {
      Method possibleMethod = methods[i];
      if (simpleName.equals(possibleMethod.getName().toLowerCase())) {
        method = possibleMethod;
        break;
      }
    }
    return method;
  }

  public abstract void doCell(Fixture fixture, Parse cell) throws Throwable;

  public static class SaveBinding extends Binding {
    public void doCell(Fixture fixture, Parse cell) {
      try {
        //TODO-MdM hmm... somehow this needs to regulated by the fixture.
        if (fixture instanceof ColumnFixture)
          ((ColumnFixture) fixture).executeIfNeeded();

        Object valueObj = adapter.get(); //...might be validly null
        String symbolValue = valueObj == null ? "null" : valueObj.toString();
        String symbolName = cell.text();
        Fixture.setSymbol(symbolName, symbolValue);
        cell.addToBody(Fixture.gray(" = " + symbolValue));
      }
      catch (Exception e) {
        fixture.exception(cell, e);
      }
    }
  }

  public static class RecallBinding extends Binding {
    public void doCell(Fixture fixture, Parse cell) throws Exception {
      String symbolName = cell.text();
      if (!Fixture.hasSymbol(symbolName))
        fixture.exception(cell, new FitFailureException("No such symbol: " + symbolName));
      else {
        String value = (String) Fixture.getSymbol(symbolName);
        if (adapter.field != null) {
          adapter.set(adapter.parse(value));
          cell.addToBody(Fixture.gray(" = " + value));
        }
        if (adapter.method != null) {
          cell.body = value;
          fixture.check(cell, adapter);
        }
      }
    }
  }

  public static class SetBinding extends Binding {
    public void doCell(Fixture fixture, Parse cell) throws Throwable {
      if ("".equals(cell.text()))
        fixture.handleBlankCell(cell, adapter);
      adapter.set(adapter.parse(cell.text()));
    }
  }

  public static class QueryBinding extends Binding {
    public void doCell(Fixture fixture, Parse cell) {
      fixture.check(cell, adapter);
    }
  }

  public static class RegexQueryBinding extends Binding {
    public void doCell(Fixture fixture, Parse cell) {
      fixture.check(cell, adapter);
    }
  }

  public static class NullBinding extends Binding {
    public void doCell(Fixture fixture, Parse cell) {
      fixture.ignore(cell);
    }
  }
}

