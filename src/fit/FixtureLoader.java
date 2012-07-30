// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or
// later.
package fit;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import fit.exception.CouldNotLoadComponentFitFailureException;
import fit.exception.NoSuchFixtureException;

// REFACTOR The fixture path is really the only part of this

// class that needs to be globally accessible.
public class FixtureLoader implements FixtureLoaderInterface {
  private static FixtureLoaderInterface instance = null;

  public static FixtureLoaderInterface instance() {
    if (instance == null) {
      instance = instanceForClassName(System.getProperty(FixtureLoader.class.getName()));
    }
    return instance;
  }

  protected static FixtureLoaderInterface instanceForClassName(String fixtureLoaderClassName) {
    if(fixtureLoaderClassName!=null && !fixtureLoaderClassName.isEmpty()) {
      try {
        return (FixtureLoaderInterface)Class.forName(fixtureLoaderClassName).newInstance();
      } catch (Exception ex) {
        // Possible exceptions are:
        // - Class cannot be found 
        // - Class cannot be instantiated (i.e. doesn't have a no arg constructor)
        // - Class does not implement the FixtureLoaderInterface
        ex.printStackTrace(System.err); // Don't know what to do so just print it
      }
    }
    return new FixtureLoader();
  }

  public static void setInstance(FixtureLoaderInterface loader) {
    instance = loader;
  }

  public Set<String> fixturePathElements = new HashSet<String>() {
    private static final long serialVersionUID = 1L;

    {
      add("fit");
    }
  };

  @Override
  public Fixture disgraceThenLoad(String tableName) throws Throwable {
    FixtureName fixtureName = new FixtureName(tableName);
    Fixture fixture = instantiateFirstValidFixtureClass(fixtureName);
    addPackageToFixturePath(fixture);
    return fixture;
  }

  private void addPackageToFixturePath(Fixture fixture) {
    Package fixturePackage = fixture.getClass().getPackage();
    if (fixturePackage != null)
      addPackageToPath(fixturePackage.getName());
  }

  @Override
  public void addPackageToPath(String name) {
    fixturePathElements.add(name);
  }

  private Fixture instantiateFixture(String fixtureName) throws Throwable {
    Class<?> classForFixture = loadFixtureClass(fixtureName);
    FixtureClass fixtureClass = new FixtureClass(classForFixture);
    return fixtureClass.newInstance();
  }

  private Class<?> loadFixtureClass(String fixtureName) {
    try {
      return Class.forName(fixtureName);
    }
    catch (ClassNotFoundException deadEnd) {
      if (deadEnd.getMessage().equals(fixtureName))
        throw new NoSuchFixtureException(fixtureName);
      else
        throw new CouldNotLoadComponentFitFailureException(
          deadEnd.getMessage(), fixtureName);
    }
  }

  private Fixture instantiateFirstValidFixtureClass(FixtureName fixtureName)
    throws Throwable {
    for (Iterator<String> i = fixtureName.getPotentialFixtureClassNames(
      fixturePathElements).iterator(); i.hasNext();) {
      String each = (String) i.next();
      try {
        return instantiateFixture(each);
      }
      catch (NoSuchFixtureException ignoreAndTryTheNextCandidate) {
      }
    }

    throw new NoSuchFixtureException(fixtureName.toString());
  }
}
