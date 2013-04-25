// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import java.util.Arrays;

import fitnesse.slim.Converter;
import fitnesse.slim.SlimError;

@SuppressWarnings("rawtypes")
public class BooleanArrayConverter implements Converter {
  private static final BooleanConverter booleanConverter = new BooleanConverter();

  public String toString(Object o) {
    if (o == null) return "null";
    Boolean[] booleans = (Boolean[]) o;
    return Arrays.asList(booleans).toString();
  }

  public Object fromString(String arg) {
    String[] strings = ListConverter.fromStringToArrayOfStrings(arg);
    Boolean[] booleans = new Boolean[strings.length];
    for (int i = 0; i < strings.length; i++) {
      try {
        booleans[i] = booleanConverter.fromString(strings[i]);
      } catch (NumberFormatException e) {
        throw new SlimError("message:<<CANT_CONVERT_TO_BOOLEAN_LIST>>");
      }
    }
    return booleans;
  }
}
