// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import java.util.Arrays;

import fitnesse.slim.Converter;

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
