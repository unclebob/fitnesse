// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import java.util.Arrays;

import fitnesse.slim.Converter;

public class StringArrayConverter implements Converter<String[]> {
  public String toString(String[] strings) {
    if (strings == null) return "null";
    return Arrays.asList(strings).toString();
  }

  public String[] fromString(String arg) {
    return ListConverter.fromStringToArrayOfStrings(arg);
  }
}
