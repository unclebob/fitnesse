// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit.exception;

import java.text.ParseException;

public class FitParseException extends ParseException {
  private static final long serialVersionUID = 1L;

  public FitParseException(String s, int i) {
    super(s, i);
  }
}
