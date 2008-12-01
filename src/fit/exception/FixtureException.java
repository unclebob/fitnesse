// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit.exception;

import java.text.MessageFormat;

public abstract class FixtureException extends FitFailureException {
  private static final long serialVersionUID = 1L;

  public final String fixtureName;

  public FixtureException(String messageFormat, String fixtureName) {
    super(formatMessage(messageFormat, fixtureName));
    this.fixtureName = fixtureName;
  }

  private static String formatMessage(String messageFormat, String fixtureName) {
    return new MessageFormat(messageFormat).format(new Object[]
      {fixtureName});
  }
}

