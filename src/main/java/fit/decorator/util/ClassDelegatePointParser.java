// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fit.decorator.util;

import java.awt.Point;

public class ClassDelegatePointParser {
  public static Point parse(String s) {
    // format = (xxxx,yyyyy)
    validateFormat(s);
    int indexOfOpenParanthesis = 0;
    int indexOfComma = s.indexOf(",");
    int indexOfClosingParanthesis = s.length() - 1;
    int x = Integer.parseInt(substring(s, indexOfOpenParanthesis, indexOfComma));
    int y = Integer.parseInt(substring(s, indexOfComma, indexOfClosingParanthesis));
    return new Point(x, y);
  }

  private static void validateFormat(String s) {
    if ("(x,y)".length() > s.length() || !s.contains("(") || !s.contains(",") || !s.contains(")")) {
      throw new IllegalArgumentException(s + " is not a valid format. (x,y) is the correct format");
    }
  }

  private static String substring(String s, int startingIndex, int endingIndex) {
    return s.substring(startingIndex + 1, endingIndex);
  }

}
