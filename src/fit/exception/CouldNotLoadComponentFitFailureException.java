// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit.exception;

public class CouldNotLoadComponentFitFailureException extends FitFailureException {
  private static final long serialVersionUID = 1L;

  public CouldNotLoadComponentFitFailureException(String component, String fixtureName) {
    super("Could not load " + component + " which is a component of " + fixtureName);
  }
}
