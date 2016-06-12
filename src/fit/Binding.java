// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fit.exception.FitFailureException;
import fit.exception.NoSuchFieldFitFailureException;
import fit.exception.NoSuchMethodFitFailureException;
import util.GracefulNamer;

import static java.util.Arrays.asList;

public abstract class Binding {
  private static final Pattern regexMethodPattern = Pattern.compile("(.+)(?:\\?\\?|!!)");
  private static final Pattern methodPattern = Pattern.compile("(.+)(?:\\(\\)|\\?|!)");
  private static final Pattern fieldPattern = Pattern.compile("=?([^=]+)=?");

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
      Matcher matcher = fieldPattern.matcher(name);
      matcher.find();
      String fieldName = matcher.group(1);
      Class<?> clazz = fixture.getTargetClass();
      try {
        field = clazz.getField(fieldName);
      }
      catch (NoSuchFieldException e) {
        try {
          field = getField(clazz, fieldName);
        } catch (NoSuchFieldException e2) {
        }
      }
    }

    if (field == null)
      throw new NoSuchFieldFitFailureException(name);
    return TypeAdapter.on(fixture, field);
  }

  private static Field getField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
    try {
      return clazz.getDeclaredField(fieldName);
    } catch (NoSuchFieldException e) {
      Class<?> superClass = clazz.getSuperclass();
      if (superClass == null) {
        throw e;
      } else {
        return getField(superClass, fieldName);
      }
    }
  }

  private static TypeAdapter makeAdapterForMethod(String name, Fixture fixture, Matcher matcher) {
    return makeAdapterForMethod(name, fixture, matcher, false);
  }

  private static TypeAdapter makeAdapterForRegexMethod(String name, Fixture fixture, Matcher matcher) {
    return makeAdapterForMethod(name, fixture, matcher, true);
  }

  private static TypeAdapter makeAdapterForMethod(String name, Fixture fixture, Matcher matcher, boolean isRegex) {
    Method method = getMethod(name, fixture, matcher);

    if (method == null) {
      throw new NoSuchMethodFitFailureException(name);
    }
    return TypeAdapter.on(fixture, method, isRegex);
  }

  private static Method getMethod(String name, Fixture fixture, Matcher matcher) {
    Method method = null;
    if (GracefulNamer.isGracefulName(name)) {
      String simpleName = GracefulNamer.disgrace(name).toLowerCase();
      method = findMethod(fixture, simpleName);
    } else {
      try {
        String methodName = matcher.group(1);
        Class<?> targetClass = fixture.getTargetClass();
        method = targetClass.getMethod(methodName, new Class[]{});
      }
      catch (NoSuchMethodException e) {
      }
    }

    return method;
  }

  private static Field findField(Fixture fixture, String simpleName) {
    List<Field> fields = getAllDeclaredFields(fixture.getTargetClass());
    Field field = null;
    for (Field possibleField : fields) {
      if (simpleName.equalsIgnoreCase(possibleField.getName())) {
        field = possibleField;
        break;
      }
    }
    return field;
  }

  private static List<Field> getAllDeclaredFields(Class<?> clazz){
    if (clazz.getSuperclass() != null) {
      List<Field> fields = getAllDeclaredFields(clazz.getSuperclass());
      fields.addAll(asList(clazz.getDeclaredFields()));
      return fields;
    } else {
      return new ArrayList<>(asList(clazz.getDeclaredFields()));
    }
  }

  private static Method findMethod(Fixture fixture, String simpleName) {
    Method[] methods = fixture.getTargetClass().getMethods();
    Method method = null;
    for (Method possibleMethod : methods) {
      if (simpleName.equalsIgnoreCase(possibleMethod.getName())) {
        method = possibleMethod;
        break;
      }
    }
    return method;
  }

  public abstract void doCell(Fixture fixture, Parse cell) throws Throwable;

  public static class SaveBinding extends Binding {
    @Override
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
    @Override
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
    @Override
    public void doCell(Fixture fixture, Parse cell) throws Throwable {
      if ("".equals(cell.text()))
        fixture.handleBlankCell(cell, adapter);
      adapter.set(adapter.parse(cell.text()));
    }
  }

  public static class QueryBinding extends Binding {
    @Override
    public void doCell(Fixture fixture, Parse cell) {
      fixture.check(cell, adapter);
    }
  }

  public static class RegexQueryBinding extends Binding {
    @Override
    public void doCell(Fixture fixture, Parse cell) {
      fixture.check(cell, adapter);
    }
  }

  public static class NullBinding extends Binding {
    @Override
    public void doCell(Fixture fixture, Parse cell) {
      fixture.ignore(cell);
    }
  }
}

