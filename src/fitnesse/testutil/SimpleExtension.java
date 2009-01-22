// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fitnesse.wiki.Extension;

public class SimpleExtension implements Extension {
  private static final long serialVersionUID = 1L;

  public String getName() {
    return "SimpleExtension";
  }
}
