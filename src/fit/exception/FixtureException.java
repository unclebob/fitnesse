// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fit.exception;

import java.text.MessageFormat;

public abstract class FixtureException extends FitFailureException
{
  public final String fixtureName;

  public FixtureException(String messageFormat, String fixtureName)
  {
    super(formatMessage(messageFormat, fixtureName));
    this.fixtureName = fixtureName;
  }

  private static String formatMessage(String messageFormat, String fixtureName)
  {
    return new MessageFormat(messageFormat).format(new Object[]
    { fixtureName });
  }
}

