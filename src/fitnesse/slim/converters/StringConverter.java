// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class StringConverter implements Converter {
  public String toString(Object o) {
    return ((String) o);
  }

  public Object fromString(String arg) {
    return arg;
  }
}
