package fitnesse.slim.converters;

import fitnesse.slim.Converter;

import java.util.Arrays;

public class StringArrayConverter implements Converter {
  public String toString(Object o) {
    if (o == null) return "null";
    String[] strings = (String[]) o;
    return Arrays.asList(strings).toString();
  }

  public Object fromString(String arg) {
    return ListConverter.fromStringToArrayOfStrings(arg);
  }
}
