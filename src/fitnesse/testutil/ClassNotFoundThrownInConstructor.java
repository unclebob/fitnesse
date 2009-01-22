// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fit.Fixture;

public class ClassNotFoundThrownInConstructor extends Fixture {
  public ClassNotFoundThrownInConstructor() throws ClassNotFoundException {
    Class.forName("NoSuchClass");
  }
}
