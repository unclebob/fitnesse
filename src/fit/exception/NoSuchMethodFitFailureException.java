// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fit.exception;

import fit.exception.FitFailureException;

public class NoSuchMethodFitFailureException extends FitFailureException
{
  public NoSuchMethodFitFailureException(String name)
  {
    super("Could not find method: " + name + ".");
  }
}
