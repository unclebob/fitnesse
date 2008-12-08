package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class StringConverter implements Converter {
  public String toString(Object o) {
    return ((String) o);
  }

  public Object fromString(String arg) {
    return arg;
  }
}
