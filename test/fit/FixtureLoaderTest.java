// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or
// later.
package fit;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class FixtureLoaderTest {
  private FixtureLoader fixtureLoader;

  @Before
  public void setUp() throws Exception {
    fixtureLoader = new FixtureLoader();
  }

  @Test
  public void testLoadFixturesFromPreviouslyRememberedPackages()
    throws Throwable {
    Fixture f1 = fixtureLoader.disgraceThenLoad("fit.FixtureOne");
    assertEquals("fit.FixtureOne", f1.getClass().getName());
    Fixture f2 = fixtureLoader.disgraceThenLoad("FixtureTwo");
    assertEquals("fit.FixtureTwo", f2.getClass().getName());
  }

  @Test
  public void testLoadFixturesWithGracefulName() throws Throwable {
    fixtureLoader.disgraceThenLoad("fit.FixtureOne");
    Fixture f2 = fixtureLoader.disgraceThenLoad("fixture two");
    assertEquals("fit.FixtureTwo", f2.getClass().getName());
  }

  @Test
  public void testLoadFixturesWithFixtureImplied() throws Throwable {
    fixtureLoader.disgraceThenLoad("fit.TheThirdFixture");
    Fixture fixture = fixtureLoader.disgraceThenLoad("the third");
    assertEquals("fit.TheThirdFixture", fixture.getClass().getName());
  }
}