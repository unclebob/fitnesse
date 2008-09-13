package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class DoubleConverter implements Converter {
  public String toString(Object o) {
    return o.toString();
  }

  public Object fromString(String arg) {
    return Double.parseDouble(arg);
  }
}
