package fitnesse.slim.converters;

import java.util.List;

import fitnesse.util.StringUtils;

final class ListConverterHelper {

  static String toString(List<?> list) {
    return list.toString();
  }

  static String[] fromStringToArrayOfStrings(String arg) {
    //Remove square brackets 
    if (arg.startsWith("["))
      arg = arg.substring(1);
    if (arg.endsWith("]"))
      arg = arg.substring(0, arg.length() - 1);

    //Split the arg
    String[] strings;
    if (StringUtils.isBlank(arg)) {
      strings = new String[] {};
    } else {
      strings = arg.split(",");
    }

    //Trim all item
    for (int i = 0; i < strings.length; i++)
      strings[i] = strings[i].trim();

    return strings;
  }
}
