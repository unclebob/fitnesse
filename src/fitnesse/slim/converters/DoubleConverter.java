// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import fitnesse.slim.SlimError;

public class DoubleConverter extends ConverterBase<Double> {

  @Override
  public Double getObject(String arg) {
    try {
      return Double.valueOf(arg);
    } catch (NumberFormatException e) {
      throw new SlimError(String.format("message:<<Can't convert %s to double.>>", arg), e);
    }
  }
}
