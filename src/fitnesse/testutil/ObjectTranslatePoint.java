// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fit.TypeAdapter;

import java.awt.*;

public class ObjectTranslatePoint extends TranslatePoint {
  static {
    TypeAdapter.registerParseDelegate(Point.class, new ObjectDelegatePointParser());
  }
}
