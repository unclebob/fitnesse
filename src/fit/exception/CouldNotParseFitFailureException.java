// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit.exception;

public class CouldNotParseFitFailureException extends FitFailureException
{
  public CouldNotParseFitFailureException(String text, String type)
  {
    super("Could not parse: " + text + " expected type: " + type + ".");
  }
}
