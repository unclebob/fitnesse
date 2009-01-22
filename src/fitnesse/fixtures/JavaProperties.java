// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fitlibrary.DoFixture;

import java.util.regex.Pattern;

public class JavaProperties extends DoFixture {
  public boolean propertyShouldMatch(String property, String pattern) {
    String value = System.getProperty(property);
    return Pattern.matches(pattern, value);
  }


  public String property(String property) {
    return System.getProperty(property);
  }
}
