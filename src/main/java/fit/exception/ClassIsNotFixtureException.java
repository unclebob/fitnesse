// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or
// later.
package fit.exception;

public class ClassIsNotFixtureException extends FixtureException {
  private static final long serialVersionUID = 1L;

  public ClassIsNotFixtureException(String fixtureName) {
    super("Class {0} is not a fixture.", fixtureName);
  }
}