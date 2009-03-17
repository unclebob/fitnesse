// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import java.util.Arrays;

import fitnesse.slim.Converter;
import fitnesse.slim.SlimError;

public class IntegerArrayConverter implements Converter {
  public String toString(Object o) {
    if (o == null) return "null";
    Integer[] integers = (Integer[]) o;
    return Arrays.asList(integers).toString();
  }

  public Object fromString(String arg) {
    String[] strings = ListConverter.fromStringToArrayOfStrings(arg);
    Integer[] integers = new Integer[strings.length];
    for (int i = 0; i < strings.length; i++) {
      try {
        integers[i] = Integer.parseInt(strings[i]);
      } catch (NumberFormatException e) {
        throw new SlimError("message:<<CANT_CONVERT_TO_INTEGER_LIST>>");
      }
    }
    return integers;
  }
}
