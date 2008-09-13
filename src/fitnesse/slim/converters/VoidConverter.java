package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class VoidConverter implements Converter {
  public String toString(Object o) {
    return "VOID";
  }

  public Object fromString(String arg) {
    return null;
  }
}
