// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fit.exception;

public class NoDefaultConstructorFixtureException extends FixtureException
{
  public NoDefaultConstructorFixtureException(String fixtureName)
  {
    super("Class {0} has no default constructor.", fixtureName);
  }
}