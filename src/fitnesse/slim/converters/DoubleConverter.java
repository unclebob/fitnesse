// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import fitnesse.slim.SlimError;
import fitnesse.util.StringUtils;

import fitnesse.slim.Converter;

public class DoubleConverter implements Converter<Double> {
  @Override
  public String toString(Double o) {
    return o != null ? o.toString() : NULL_VALUE;
  }

  @Override
  public Double fromString(String arg) {
    try {
      return !StringUtils.isBlank(arg) ? Double.valueOf(arg) : null;
    } catch (NumberFormatException e) {
      throw new SlimError(String.format("message:<<Can't convert %s to double.>>", arg), e);
    }
  }
}
