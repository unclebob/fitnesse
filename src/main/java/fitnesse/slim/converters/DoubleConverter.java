// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class DoubleConverter implements Converter<Double> {
  public String toString(Double o) {
    return o.toString();
  }

  public Double fromString(String arg) {
    return Double.parseDouble(arg);
  }
}  
