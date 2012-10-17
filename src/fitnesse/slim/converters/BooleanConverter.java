// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class BooleanConverter implements Converter {
  public static final String TRUE = "true";
  public static final String FALSE = "false";

  public Boolean fromString(String arg) {
    return (
      arg.equalsIgnoreCase(TRUE) ||
        arg.equalsIgnoreCase("yes")
    );
  }

  public String toString(Object o) {
    if (o instanceof Boolean) {
      return ((Boolean) o) ? TRUE : FALSE;
    } else {
      return o.toString();
    }
  }

}
