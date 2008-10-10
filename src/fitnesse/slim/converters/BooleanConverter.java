package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class BooleanConverter implements Converter {
  public static final String TRUE = "true";
  public static final String FALSE = "false";

  public Object fromString(String arg) {
    return (
      arg.equalsIgnoreCase("true") ||
        arg.equalsIgnoreCase("yes") ||
        arg.equals(TRUE)
      );
  }

  public String toString(Object o) {
    return ((Boolean) o ) ? TRUE : FALSE;
  }
}
