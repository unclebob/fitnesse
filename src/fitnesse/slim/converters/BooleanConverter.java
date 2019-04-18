// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

public class BooleanConverter extends ConverterBase<Boolean> {
  public static final String TRUE = "true";
  public static final String FALSE = "false";
  public static final String YES = "yes";

  @Override
  public String getString(Boolean o) {
    return o ? TRUE : FALSE;
  }

  @Override
  protected Boolean getObject(String arg) {
    return arg.equalsIgnoreCase(TRUE) || arg.equalsIgnoreCase(YES);
  }
}
