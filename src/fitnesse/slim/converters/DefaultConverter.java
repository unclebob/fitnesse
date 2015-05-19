package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class DefaultConverter implements Converter<Object> {
  @Override
  public String toString(Object o) {
    return o == null ? NULL_VALUE : o.toString();
  }

  @Override
  public Object fromString(String arg) {
    return arg;
  }
}
