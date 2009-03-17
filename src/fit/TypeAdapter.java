// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class TypeAdapter {
  public Object target;
  public Fixture fixture;
  public Field field;
  public Method method;
  public Class<?> type;
  public boolean isRegex;
  private static Map<Class<?>, TypeAdapter> PARSE_DELEGATES = new HashMap<Class<?>, TypeAdapter>();
  // Factory //////////////////////////////////

  public static TypeAdapter on(Fixture target, Class<?> type) {
    TypeAdapter a = adapterFor(type);
    a.init(target, type);
    return a;
  }

  public static TypeAdapter on(Fixture fixture, Field field) {
    TypeAdapter a = on(fixture, field.getType());
    a.target = fixture;
    a.field = field;
    return a;
  }

  public static TypeAdapter on(Fixture fixture, Method method) {
    return on(fixture, method, false);
  }

  public static TypeAdapter on(Fixture fixture, Method method, boolean isRegex) {
    TypeAdapter a = on(fixture, method.getReturnType());
    a.target = fixture;
    a.method = method;
    a.isRegex = isRegex;
    return a;
  }

  public static TypeAdapter adapterFor(Class<?> type) throws UnsupportedOperationException {
    if (type.isPrimitive()) {
      if (type.equals(byte.class)) return new ByteAdapter();
      if (type.equals(short.class)) return new ShortAdapter();
      if (type.equals(int.class)) return new IntAdapter();
      if (type.equals(long.class)) return new LongAdapter();
      if (type.equals(float.class)) return new FloatAdapter();
      if (type.equals(double.class)) return new DoubleAdapter();
      if (type.equals(char.class)) return new CharAdapter();
      if (type.equals(boolean.class)) return new BooleanAdapter();
      throw new UnsupportedOperationException("can't yet adapt " + type);
    } else {
      Object delegate = PARSE_DELEGATES.get(type);
      if (delegate instanceof DelegateClassAdapter)
        return (TypeAdapter) ((DelegateClassAdapter) delegate).clone();
      if (delegate instanceof DelegateObjectAdapter)
        return (TypeAdapter) ((DelegateObjectAdapter) delegate).clone();
      if (type.equals(Byte.class)) return new ClassByteAdapter();
      if (type.equals(Short.class)) return new ClassShortAdapter();
      if (type.equals(Integer.class)) return new ClassIntegerAdapter();
      if (type.equals(Long.class)) return new ClassLongAdapter();
      if (type.equals(Float.class)) return new ClassFloatAdapter();
      if (type.equals(Double.class)) return new ClassDoubleAdapter();
      if (type.equals(Character.class)) return new ClassCharacterAdapter();
      if (type.equals(Boolean.class)) return new ClassBooleanAdapter();
      if (type.isArray()) return new ArrayAdapter();
      return new TypeAdapter();
    }
  }

  // Accessors ////////////////////////////////

  public void init(Fixture fixture, Class<?> type) {
    this.fixture = fixture;
    this.type = type;
  }

  public Object get() throws IllegalAccessException, InvocationTargetException {
    if (field != null) {
      return field.get(target);
    }
    if (method != null) {
      return invoke();
    }
    return null;
  }

  public void set(Object value) throws Exception {
    field.set(target, value);
  }

  public Object invoke() throws IllegalAccessException, InvocationTargetException {
    Object params[] =
      {};
    return method.invoke(target, params);
  }

  public Object parse(String s) throws Exception {
    Object obj;
    obj = isRegex ? s : fixture.parse(s, type);
    return obj;
  }

  public boolean equals(Object a, Object b) {
    boolean isEqual = false;

    if (isRegex)
      isEqual = Pattern.matches(a.toString(), b.toString());
    else {
      if (a == null)
        isEqual = (b == null);
      else
        isEqual = a.equals(b);
    }
    return isEqual;
  }

  public String toString(Object o) {
    if (o == null) {
      return "null";
    } else if (o instanceof String && ((String) o).equals(""))
      return "blank";
    else
      return o.toString();
  }

  /**
   * Registers a delegate, a class that will handle parsing of other types of values.
   */
  public static void registerParseDelegate(Class<?> type, Class<?> parseDelegate) {
    try {
      PARSE_DELEGATES.put(type, new DelegateClassAdapter(parseDelegate));
    } catch (Exception ex) {
      throw new RuntimeException("Parse delegate class " + parseDelegate.getName()
        + " does not have a suitable static parse() method.");
    }
  }

  /**
   * Registers a delegate object that will handle parsing of other types of values.
   */
  public static void registerParseDelegate(Class<?> type, Object parseDelegate) {
    try {
      PARSE_DELEGATES.put(type, new DelegateObjectAdapter(parseDelegate));
    } catch (Exception ex) {
      throw new RuntimeException("Parse delegate object of class " + parseDelegate.getClass().getName()
        + " does not have a suitable parse() method.");
    }
  }

  public static void clearDelegatesForNextTest() {
    PARSE_DELEGATES.clear();
  }

  // Subclasses ///////////////////////////////

  static class ByteAdapter extends ClassByteAdapter {
    public void set(Object i) throws IllegalAccessException {
      field.setByte(target, ((Byte) i).byteValue());
    }
  }

  static class ClassByteAdapter extends TypeAdapter {
    public Object parse(String s) {
      return ("null".equals(s)) ? null : new Byte(Byte.parseByte(s));
    }
  }

  static class ShortAdapter extends ClassShortAdapter {
    public void set(Object i) throws IllegalAccessException {
      field.setShort(target, ((Short) i).shortValue());
    }
  }

  static class ClassShortAdapter extends TypeAdapter {
    public Object parse(String s) {
      return ("null".equals(s)) ? null : new Short(Short.parseShort(s));
    }
  }

  static class IntAdapter extends ClassIntegerAdapter {
    public void set(Object i) throws IllegalAccessException {
      field.setInt(target, ((Integer) i).intValue());
    }
  }

  static class ClassIntegerAdapter extends TypeAdapter {
    public Object parse(String s) {
      return ("null".equals(s)) ? null : new Integer(Integer.parseInt(s));
    }
  }

  static class LongAdapter extends ClassLongAdapter {
    public void set(Long i) throws IllegalAccessException {
      field.setLong(target, i.longValue());
    }
  }

  static class ClassLongAdapter extends TypeAdapter {
    public Object parse(String s) {
      return ("null".equals(s)) ? null : new Long(Long.parseLong(s));
    }
  }

  static class FloatAdapter extends ClassFloatAdapter {
    public void set(Object i) throws IllegalAccessException {
      field.setFloat(target, ((Number) i).floatValue());
    }

    public Object parse(String s) {
      return ("null".equals(s)) ? null : new Float(Float.parseFloat(s));
    }
  }

  static class ClassFloatAdapter extends TypeAdapter {
    public Object parse(String s) {
      return ("null".equals(s)) ? null : new Float(Float.parseFloat(s));
    }
  }

  static class DoubleAdapter extends ClassDoubleAdapter {
    public void set(Object i) throws IllegalAccessException {
      field.setDouble(target, ((Number) i).doubleValue());
    }

    public Object parse(String s) {
      return new Double(Double.parseDouble(s));
    }
  }

  static class ClassDoubleAdapter extends TypeAdapter {
    public Object parse(String s) {
      return ("null".equals(s)) ? null : new Double(Double.parseDouble(s));
    }
  }

  static class CharAdapter extends ClassCharacterAdapter {
    public void set(Object i) throws IllegalAccessException {
      field.setChar(target, ((Character) i).charValue());
    }
  }

  static class ClassCharacterAdapter extends TypeAdapter {
    public Object parse(String s) {
      return ("null".equals(s)) ? null : new Character(s.charAt(0));
    }
  }

  static class BooleanAdapter extends ClassBooleanAdapter {
    public void set(Object i) throws IllegalAccessException {
      field.setBoolean(target, ((Boolean) i).booleanValue());
    }
  }

  static class ClassBooleanAdapter extends TypeAdapter {
    public Object parse(String s) {
      if ("null".equals(s)) return null;
      String ls = s.toLowerCase();
      if (ls.equals("true"))
        return new Boolean(true);
      if (ls.equals("yes"))
        return new Boolean(true);
      if (ls.equals("1"))
        return new Boolean(true);
      if (ls.equals("y"))
        return new Boolean(true);
      if (ls.equals("+"))
        return new Boolean(true);
      return new Boolean(false);
    }
  }

  static class ArrayAdapter extends TypeAdapter {
    Class<?> componentType;
    TypeAdapter componentAdapter;

    public void init(Fixture target, Class<?> type) {
      super.init(target, type);
      componentType = type.getComponentType();
      componentAdapter = on(target, componentType);
    }

    public Object parse(String s) throws Exception {
      StringTokenizer t = new StringTokenizer(s, ",");
      Object array = Array.newInstance(componentType, t.countTokens());
      for (int i = 0; t.hasMoreTokens(); i++) {
        Array.set(array, i, componentAdapter.parse(t.nextToken().trim()));
      }
      return array;
    }

    public String toString(Object o) {
      if (o == null)
        return "";
      int length = Array.getLength(o);
      StringBuffer b = new StringBuffer(5 * length);
      for (int i = 0; i < length; i++) {
        b.append(componentAdapter.toString(Array.get(o, i)));
        if (i < (length - 1)) {
          b.append(", ");
        }
      }
      return b.toString();
    }

    public boolean equals(Object a, Object b) {
      int length = Array.getLength(a);
      if (length != Array.getLength(b))
        return false;
      for (int i = 0; i < length; i++) {
        if (!componentAdapter.equals(Array.get(a, i), Array.get(b, i)))
          return false;
      }
      return true;
    }
  }

  static class DelegateClassAdapter extends TypeAdapter implements Cloneable {
    private Method parseMethod;

    public DelegateClassAdapter(Class<?> parseDelegate) throws SecurityException, NoSuchMethodException {
      this.parseMethod = parseDelegate.getMethod("parse", new Class[]{String.class});
      int modifiers = parseMethod.getModifiers();
      if (!Modifier.isStatic(modifiers) || !Modifier.isPublic(modifiers)
        || parseMethod.getReturnType() == Void.class)
        throw new NoSuchMethodException();
    }

    public Object parse(String s) throws Exception {
      return parseMethod.invoke(null, new Object[]
        {s});
    }

    protected Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        return null;
      }
    }
  }

  static class DelegateObjectAdapter extends TypeAdapter implements Cloneable {
    private Object delegate;
    private Method parseMethod;

    public DelegateObjectAdapter(Object delegate) throws SecurityException, NoSuchMethodException {
      this.delegate = delegate;
      this.parseMethod = delegate.getClass().getMethod("parse", new Class[]
        {String.class});
    }

    public Object parse(String s) throws Exception {
      return parseMethod.invoke(delegate, new Object[]
        {s});
    }

    protected Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        return null;
      }
    }
  }
}
