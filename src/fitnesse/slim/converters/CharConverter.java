package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class CharConverter implements Converter {
  public String toString(Object o) {
    return o.toString();
  }

  public Object fromString(String arg) {
    return arg.toCharArray()[0];
  }
}
