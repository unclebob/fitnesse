// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class BooleanConverter implements Converter<Boolean> {
  public static final String TRUE = "true";
  public static final String FALSE = "false";

  public Boolean fromString(String arg) {
    return (
      arg.equalsIgnoreCase("true") ||
        arg.equalsIgnoreCase("yes") ||
        arg.equals(TRUE)
    );
  }

  public String toString(Boolean o) {
    return o ? TRUE : FALSE;
  }
}
