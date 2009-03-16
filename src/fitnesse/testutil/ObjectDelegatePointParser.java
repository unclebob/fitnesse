// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fit.decorator.util.ClassDelegatePointParser;

import java.awt.*;

public class ObjectDelegatePointParser {
  public Point parse(String s) {
    // format = (xxxx,yyyyy)
    return ClassDelegatePointParser.parse(s);
  }

}
