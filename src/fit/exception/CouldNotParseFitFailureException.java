// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fit.exception;

public class CouldNotParseFitFailureException extends FitFailureException
{
  public CouldNotParseFitFailureException(String text, String type)
  {
    super("Could not parse: " + text + " expected type: " + type + ".");
  }
}
