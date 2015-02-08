// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.converters;

import fitnesse.slim.Converter;
import fitnesse.util.StringUtils;

public class CharConverter implements Converter<Character> {

  public String toString(Character o) {
    return o != null ? o.toString() : NULL_VALUE;
  }

  public Character fromString(String arg) {
    return !StringUtils.isBlank(arg) ? arg.charAt(0) : null;
  }
}
