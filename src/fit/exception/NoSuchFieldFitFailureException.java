// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fit.exception;

public class NoSuchFieldFitFailureException extends FitFailureException
{
  public NoSuchFieldFitFailureException(String name)
  {
    super("Could not find field: " + name + ".");
  }
}
