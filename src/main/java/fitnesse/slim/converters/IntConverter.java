// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class IntConverter implements Converter<Integer> {
  public String toString(Integer o) {
    return o.toString();
  }

  public Integer fromString(String arg) {
    return Integer.parseInt(arg);
  }
}
