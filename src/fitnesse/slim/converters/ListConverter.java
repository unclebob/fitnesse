package fitnesse.slim.converters;

import fitnesse.slim.Converter;

import java.util.Arrays;

public class ListConverter implements Converter {
  public String toString(Object o) {
    if (o == null) return "null";
    return o.toString();
  }

  public Object fromString(String arg) {
    String[] strings = fromStringToArrayOfStrings(arg);
    return Arrays.asList(strings);
  }

  static String[] fromStringToArrayOfStrings(String arg) {
    if (arg.startsWith("["))
      arg = arg.substring(1);
    if (arg.endsWith("]"))
      arg = arg.substring(0, arg.length() - 1);
    String[] strings = arg.split(",");
    for (int i = 0; i < strings.length; i++)
      strings[i] = strings[i].trim();
    return strings;
  }
}
