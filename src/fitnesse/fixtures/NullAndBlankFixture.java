// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.ColumnFixture;

public class NullAndBlankFixture extends ColumnFixture
{
  public String nullString;
  public String blankString;
  public String nullString() {return null;}
  public String blankString() {return "";}
  public boolean isNull() {return nullString == null;}
  public boolean isBlank() {return blankString.length() == 0;}
}
