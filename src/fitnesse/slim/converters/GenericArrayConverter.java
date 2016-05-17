package fitnesse.slim.converters;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import fitnesse.slim.Converter;
import fitnesse.util.StringUtils;

public class GenericArrayConverter<T> implements Converter<Object> {
  private final Class<T> componentClass;
  private final Converter<T> componentConverter;

  public GenericArrayConverter(Class<T> componentClass, Converter<T> componentConverter) {
    this.componentClass = componentClass;
    this.componentConverter = componentConverter;
  }

  @Override
  @SuppressWarnings("unchecked")
  public String toString(Object array) {
    if (array == null)
      return NULL_VALUE;

    int size = Array.getLength(array);
    List<String> ret = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      ret.add(getElementString(array, i));
    }

    return ret.toString();
  }

  private String getElementString(Object array, int i) {
    T element = (T) Array.get(array, i);
    String result;
    if (element == null) {
      result = componentConverter.toString(element);
    } else {
      result = ElementConverterHelper.elementToString(element);
    }
    return result;
  }

  @Override
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
