// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import fitnesse.util.StringUtils;

import fitnesse.slim.Converter;

public class BooleanConverter implements Converter<Boolean> {
  public static final String TRUE = "true";
  public static final String FALSE = "false";
  public static final String YES = "yes";

  public String toString(Boolean o) {
    return o != null ? o.booleanValue() ? TRUE : FALSE : NULL_VALUE;
  }

  public Boolean fromString(String arg) {
    if (StringUtils.isBlank(arg))
      return null;

    return arg.equalsIgnoreCase(TRUE) || arg.equalsIgnoreCase(YES);
  }
}
