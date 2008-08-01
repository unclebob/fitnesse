// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import java.util.*;

public class FixtureName
{
  private final String nameAsString;

  public FixtureName(String tableName)
  {
    // REFACTOR Fold GracefulNamer into this class
    if (GracefulNamer.isGracefulName(tableName))
      this.nameAsString = GracefulNamer.disgrace(tableName);
    else
      this.nameAsString = tableName;
  }

  public String toString()
  {
    return nameAsString;
  }

  public boolean isFullyQualified()
  {
    return nameAsString.indexOf('.') != -1;
  }

  public static boolean fixtureNameHasPackageSpecified(final String fixtureName)
  {
    return new FixtureName(fixtureName).isFullyQualified();
  }

  public List getPotentialFixtureClassNames(Set fixturePathElements)
  {
    List candidateClassNames = new ArrayList();

    if (!isFullyQualified())
    {
      for (Iterator i = fixturePathElements.iterator(); i.hasNext();)
      {
        String packageName = (String) i.next();
        addBlahAndBlahFixture(packageName + ".", candidateClassNames);
      }
    }
    addBlahAndBlahFixture("", candidateClassNames);
    
    return candidateClassNames;
  }

  private void addBlahAndBlahFixture(String qualifiedBy, List candidateClassNames)
  {
    candidateClassNames.add(qualifiedBy + nameAsString);
    candidateClassNames.add(qualifiedBy + nameAsString + "Fixture");
  }
}