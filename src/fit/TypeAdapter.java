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
  private static final Map<Class<?>, TypeAdapter> PARSE_DELEGATES = new HashMap<>();
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
    a.field.setAccessible(true);
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
  
  // Primitive byte adapter
  public static TypeAdapter adapterFor(Class<byte> type) {
    return new ByteAdapter();
  }
  
  // Class Byte adapter
  public static TypeAdapter adapterFor(Class<Byte> type) {
      Object retval = delegateAdapter(type);
      if (retval != null){
        return retval; 
      }
        return new ClassByteAdapter(); 
  }
  
  // Primitive short adapter
  public static TypeAdapter adapterFor(Class<short> type) {
    return new ShortAdapter();
  }
  
  // Class Short adapter
  public static TypeAdapter adapterFor(Class<Short> type) {
      Object retval = delegateAdapter(type);
      if (retval != null){
        return retval; 
      }
        return new ClassShortAdapter(); 
  }
  
  // Primitive int adapter
  public static TypeAdapter adapterFor(Class<int> type) {
    return new IntAdapter();
  }
  
  // Class Integer adapter
  public static TypeAdapter adapterFor(Class<Integer> type) {
      Object retval = delegateAdapter(type);
      if (retval != null){
        return retval; 
      }
        return new ClassIntegerAdapter(); 
  }
  
  // Primitive long adapter
  public static TypeAdapter adapterFor(Class<long> type) {
    return new LongAdapter();
  }
  
  // Class Long adapter
  public static TypeAdapter adapterFor(Class<Long> type) {
      Object retval = delegateAdapter(type);
      if (retval != null){
        return retval; 
      }
        return new ClassLongAdapter(); 
  }
  
  // Primitive float adapter
  public static TypeAdapter adapterFor(Class<float> type) {
    return new FloatAdapter();
  }
  
  // Class Float adapter
  public static TypeAdapter adapterFor(Class<Float> type) {
      Object retval = delegateAdapter(type);
      if (retval != null){
        return retval; 
      }
        return new ClassFloatAdapter(); 
  }
  
  // Primitive double adapter
  public static TypeAdapter adapterFor(Class<double> type) {
    return new DoubleAdapter();
  }
  
  // Class Double adapter
  public static TypeAdapter adapterFor(Class<Double> type) {
      Object retval = delegateAdapter(type);
      if (retval != null){
        return retval; 
      }
        return new ClassDoubleAdapter(); 
  }
  
  // Primitive char adapter
  public static TypeAdapter adapterFor(Class<char> type) {
    return new CharAdapter();
  }
  
  // Class Character adapter
  public static TypeAdapter adapterFor(Class<Character> type) {
      Object retval = delegateAdapter(type);
      if (retval != null){
        return retval; 
      }
        return new ClassCharacterAdapter(); 
  }
  
  // Primitive boolean adapter
  public static TypeAdapter adapterFor(Class<boolean> type) {
    return new BooleanAdapter();
  }
  
  // Class Boolean adapter
  public static TypeAdapter adapterFor(Class<Boolean> type) {
      Object retval = delegateAdapter(type);
      if (retval != null){
        return retval; 
      }
        return new ClassBooleanAdapter(); 
  }
  
  // Helper method for handeling delegate adapters. removed repetition of code.
  private TypeAdapter delegateAdapter(Class<?> type){
      Object delegate = PARSE_DELEGATES.get(type);
      if (delegate instanceof DelegateClassAdapter)
        return (TypeAdapter) ((DelegateClassAdapter) delegate).clone();
      if (delegate instanceof DelegateObjectAdapter)
        return (TypeAdapter) ((DelegateObjectAdapter) delegate).clone();
      else{
        return null;
      }
  }
  
  // Handle any other types that may come in
  public static TypeAdapter adapterFor(Class<?> type) throws UnsupportedOperationException {
    if (type.isPrimitive()) {
      throw new UnsupportedOperationException("can't yet adapt " + type);
    } else {
      Object retval = delegateAdapter(type);
      if (retval != null){
        return retval; 
      }
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
    Object[] params = {};
    return method.invoke(target, params);
  }

  public Object parse(String s) throws Exception {
    Object obj;
    obj = isRegex ? s : fixture.parse(s, type);
    return obj;
  }

  public boolean equals(Object a, Object b) {
    boolean isEqual = false;

    if (isRegex) {
      if (b != null)
        isEqual = Pattern.matches(a.toString(), b.toString());
    } else {
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

  /*
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

  /*
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
    @Override
    public void set(Object i) throws IllegalAccessException {
      field.setByte(target, ((Byte) i).byteValue());
    }
  }

  static class ClassByteAdapter extends TypeAdapter {
    @Override
    public Object parse(String s) {
      return ("null".equals(s)) ? null : new Byte(Byte.parseByte(s));
    }
  }

  static class ShortAdapter extends ClassShortAdapter {
    @Override
    public void set(Object i) throws IllegalAccessException {
      field.setShort(target, ((Short) i).shortValue());
    }
  }

  static class ClassShortAdapter extends TypeAdapter {
    @Override
    public Object parse(String s) {
      return ("null".equals(s)) ? null : new Short(Short.parseShort(s));
    }
  }

  static class IntAdapter extends ClassIntegerAdapter {
    @Override
    public void set(Object i) throws IllegalAccessException {
      field.setInt(target, ((Integer) i).intValue());
    }
  }

  static class ClassIntegerAdapter extends TypeAdapter {
    @Override
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
    @Override
    public Object parse(String s) {
      return ("null".equals(s)) ? null : new Long(Long.parseLong(s));
    }
  }

  static class FloatAdapter extends ClassFloatAdapter {
    @Override
    public void set(Object i) throws IllegalAccessException {
      field.setFloat(target, ((Number) i).floatValue());
    }

    @Override
    public Object parse(String s) {
      return ("null".equals(s)) ? null : new Float(Float.parseFloat(s));
    }
  }

  static class ClassFloatAdapter extends TypeAdapter {
    @Override
    public Object parse(String s) {
      return ("null".equals(s)) ? null : new Float(Float.parseFloat(s));
    }
  }

  static class DoubleAdapter extends ClassDoubleAdapter {
    @Override
    public void set(Object i) throws IllegalAccessException {
      field.setDouble(target, ((Number) i).doubleValue());
    }

    @Override
    public Object parse(String s) {
      return new Double(Double.parseDouble(s));
    }
  }

  static class ClassDoubleAdapter extends TypeAdapter {
    @Override
    public Object parse(String s) {
      return ("null".equals(s)) ? null : new Double(Double.parseDouble(s));
    }
  }

  static class CharAdapter extends ClassCharacterAdapter {
    @Override
    public void set(Object i) throws IllegalAccessException {
      field.setChar(target, ((Character) i).charValue());
    }
  }

  static class ClassCharacterAdapter extends TypeAdapter {
    @Override
    public Object parse(String s) {
      return ("null".equals(s)) ? null : new Character(s.charAt(0));
    }
  }

  static class BooleanAdapter extends ClassBooleanAdapter {
    @Override
    public void set(Object i) throws IllegalAccessException {
      field.setBoolean(target, ((Boolean) i).booleanValue());
    }
  }

  static class ClassBooleanAdapter extends TypeAdapter {
    @Override
    public Object parse(String s) {
      if ("null".equals(s)) return null;
      String ls = s.toLowerCase();
      if (ls.equals("true"))
        return Boolean.TRUE;
      if (ls.equals("yes"))
        return Boolean.TRUE;
      if (ls.equals("1"))
        return Boolean.TRUE;
      if (ls.equals("y"))
        return Boolean.TRUE;
      if (ls.equals("+"))
        return Boolean.TRUE;
      return Boolean.FALSE;
    }
  }

  static class ArrayAdapter extends TypeAdapter {
    Class<?> componentType;
    TypeAdapter componentAdapter;

    @Override
    public void init(Fixture target, Class<?> type) {
      super.init(target, type);
      componentType = type.getComponentType();
      componentAdapter = on(target, componentType);
    }

    @Override
    public Object parse(String s) throws Exception {
      StringTokenizer t = new StringTokenizer(s, ",");
      Object array = Array.newInstance(componentType, t.countTokens());
      for (int i = 0; t.hasMoreTokens(); i++) {
        Array.set(array, i, componentAdapter.parse(t.nextToken().trim()));
      }
      return array;
    }

    @Override
    public String toString(Object o) {
      if (o == null)
        return "";
      int length = Array.getLength(o);
      StringBuilder b = new StringBuilder(5 * length);
      for (int i = 0; i < length; i++) {
        b.append(componentAdapter.toString(Array.get(o, i)));
        if (i < (length - 1)) {
          b.append(", ");
        }
      }
      return b.toString();
    }

    @Override
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

    @Override
    public Object parse(String s) throws Exception {
      return parseMethod.invoke(null, new Object[]
        {s});
    }

    @Override
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

    @Override
    public Object parse(String s) throws Exception {
      return parseMethod.invoke(delegate, new Object[]
        {s});
    }

    @Override
    protected Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        return null;
      }
    }
  }
}
