// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

public class ImportFixture extends Fixture
{
  public void doRow(Parse row)
  {
    String packageName = row.parts.text();
    FixtureLoader.instance().addPackageToPath(packageName);
  }
}
