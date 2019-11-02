package fitnesse.slim.converters;

import fitnesse.slim.Converter;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class GenericArrayConverter<T> extends ConverterBase<Object> {
  private final Class<T> componentClass;
  private final Converter<T> componentConverter;

  public GenericArrayConverter(Class<T> componentClass, Converter<T> componentConverter) {
    this.componentClass = componentClass;
    this.componentConverter = componentConverter;
  }

  @Override
  @SuppressWarnings("unchecked")
  public String getString(Object array) {
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
  public Object getObject(String arg) {
    String[] strings = ListConverterHelper.fromStringToArrayOfStrings(arg);
    Object array = Array.newInstance(componentClass, strings.length);
    for (int i = 0; i < strings.length; i++) {
      Array.set(array, i, componentConverter.fromString(strings[i]));
    }
    return array;
  }

}
