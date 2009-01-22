// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class DoubleConverter implements Converter {
  public String toString(Object o) {
    return o.toString();
  }

  public Object fromString(String arg) {
    return Double.parseDouble(arg);
  }
}  
