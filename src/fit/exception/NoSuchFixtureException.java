// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or
// later.
package fit.exception;

import fit.exception.FitFailureException;

public class NoSuchFixtureException extends FixtureException
{
  public NoSuchFixtureException(String fixtureName)
  {
    super("Could not find fixture: {0}.", fixtureName);
  }
}
