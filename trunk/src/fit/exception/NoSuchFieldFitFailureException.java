// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit.exception;

public class NoSuchFieldFitFailureException extends FitFailureException
{
  public NoSuchFieldFitFailureException(String name)
  {
    super("Could not find field: " + name + ".");
  }
}
