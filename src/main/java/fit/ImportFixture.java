// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

public class ImportFixture extends Fixture {
  public void doRow(Parse row) {
    String packageName = row.parts.text();
    FixtureLoader.instance().addPackageToPath(packageName);
  }
}
