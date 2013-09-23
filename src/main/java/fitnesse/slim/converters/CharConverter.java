// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import fitnesse.slim.Converter;

public class CharConverter implements Converter<Character> {
  public String toString(Character o) {
    return o.toString();
  }

  public Character fromString(String arg) {
    return arg.toCharArray()[0];
  }
}
