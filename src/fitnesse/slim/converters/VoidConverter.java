package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class VoidConverter implements Converter {
  public static final String voidTag = "/__VOID__/";

  public String toString(Object o) {
    return voidTag;
  }

  public Object fromString(String arg) {
    return null;
  }
}
