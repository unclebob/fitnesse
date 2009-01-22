// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class VoidConverter implements Converter {
  public static final String VOID_TAG = "/__VOID__/";

  public String toString(Object o) {
    return VOID_TAG;
  }

  public Object fromString(String arg) {
    return null;
  }
}
