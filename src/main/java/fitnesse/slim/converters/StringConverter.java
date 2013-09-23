// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class StringConverter implements Converter<String> {
  public String toString(String o) {
    return ((String) o);
  }

  public String fromString(String arg) {
    return arg;
  }
}
