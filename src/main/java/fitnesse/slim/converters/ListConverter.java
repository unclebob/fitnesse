// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import java.util.Arrays;
import java.util.List;

import fitnesse.slim.Converter;

@SuppressWarnings("rawtypes")
public class ListConverter implements Converter<List> {
  public String toString(List o) {
    if (o == null) return "null";
    return o.toString();
  }

  public List fromString(String arg) {
    String[] strings = fromStringToArrayOfStrings(arg);
    return Arrays.asList(strings);
  }

  static String[] fromStringToArrayOfStrings(String arg) {
    if (arg.startsWith("["))
      arg = arg.substring(1);
    if (arg.endsWith("]"))
      arg = arg.substring(0, arg.length() - 1);
    String[] strings;
    if ("".equals(arg.trim())) {
	  strings = new String[]{};
    } else {
      strings = arg.split(",");
    }  
    for (int i = 0; i < strings.length; i++)
      strings[i] = strings[i].trim();
    return strings;
  }
}
