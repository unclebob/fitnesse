// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fit.exception;

public class CouldNotLoadComponentFitFailureException  extends FitFailureException
{
  public CouldNotLoadComponentFitFailureException(String component, String fixtureName)
  {
    super("Could not load " + component + " which is a component of " + fixtureName);
  }
}
