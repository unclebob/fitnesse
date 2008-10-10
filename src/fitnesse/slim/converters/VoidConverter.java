package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class VoidConverter implements Converter {
  public static final String VOID_TAG = "/__VOID__/";

  public String toString(Object o) {
    return VOID_TAG;
  }

  public Object fromString(String arg) {
    return null;
  }
}
