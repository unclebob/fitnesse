package fitnesse.slim.converters;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import fitnesse.util.StringUtils;

import fitnesse.slim.Converter;

public class GenericArrayConverter<T> implements Converter<Object> {
  private final Class<T> componentClass;
  private final Converter<T> componentConverter;

  public GenericArrayConverter(Class<T> componentClass, Converter<T> componentConverter) {
    this.componentClass = componentClass;
    this.componentConverter = componentConverter;
  }

  @SuppressWarnings("unchecked")
  public String toString(Object array) {
    if (array == null)
      return NULL_VALUE;

    int size = Array.getLength(array);
    List<String> ret = new ArrayList<String>(size);
    for (int i = 0; i < size; i++) {
      ret.add(componentConverter.toString((T) Array.get(array, i)));
    }

    return ret.toString();
  }

  public Object fromString(String arg) {
    if (StringUtils.isBlank(arg))
      return null;

    String[] strings = ListConverterHelper.fromStringToArrayOfStrings(arg);
    Object array = Array.newInstance(componentClass, strings.length);
    for (int i = 0; i < strings.length; i++) {
      Array.set(array, i, componentConverter.fromString(strings[i]));
    }
    return array;
  }

}
