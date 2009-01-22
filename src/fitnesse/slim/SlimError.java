// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

public class SlimError extends Error {
  private static final long serialVersionUID = 1L;

  public SlimError(String s) {
    super(s);
  }

  public SlimError(String s, Throwable throwable) {
    super(s, throwable);
  }

  public SlimError(Throwable e) {
    this(e.getClass().getName() + " " + e.getMessage());
  }
}
