// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.io.File;
import java.io.FilenameFilter;

public class Wildcard implements FilenameFilter {
  private String pattern;
  private String prefix;
  private String suffix;
  private int length;

  public Wildcard(String pattern) {
    int starIndex = pattern.indexOf("*");
    if (starIndex > -1) {
      prefix = pattern.substring(0, starIndex);
      suffix = pattern.substring(starIndex + 1);
      length = prefix.length() + suffix.length();
    } else {
      this.pattern = pattern;
    }
  }

  public boolean accept(File dir, String name) {
    if (pattern != null)
      return pattern.equals(name);

    boolean goodLength = name.length() >= length;
    boolean goodPrefix = name.startsWith(prefix);
    boolean goodSufix = name.endsWith(suffix);

    return goodLength && goodPrefix && goodSufix;
  }
}
