// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or
// later.
package fit.exception;

public class ClassIsNotFixtureException extends FixtureException
{
  public ClassIsNotFixtureException(String fixtureName)
  {
    super("Class {0} is not a fixture.", fixtureName);
  }
}