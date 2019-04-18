// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import fitnesse.slim.SlimError;

public class IntConverter extends ConverterBase<Integer> {

  @Override
  protected Integer getObject(String arg) {
    try {
      return Integer.valueOf(arg);
    } catch (NumberFormatException e) {
      throw new SlimError(String.format("message:<<Can't convert %s to integer.>>", arg), e);
    }
  }
}
