// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or
// later.
package fit;

import junit.framework.TestCase;

public class FixtureLoaderTest extends TestCase {
  private FixtureLoader fixtureLoader;

  protected void setUp() throws Exception {
    fixtureLoader = new FixtureLoader();
  }

  public void testLoadFixturesFromPreviouslyRememberedPackages()
    throws Throwable {
    Fixture f1 = fixtureLoader.disgraceThenLoad("fit.FixtureOne");
    assertEquals("fit.FixtureOne", f1.getClass().getName());
    Fixture f2 = fixtureLoader.disgraceThenLoad("FixtureTwo");
    assertEquals("fit.FixtureTwo", f2.getClass().getName());
  }

  public void testLoadFixturesWithGracefulName() throws Throwable {
    fixtureLoader.disgraceThenLoad("fit.FixtureOne");
    Fixture f2 = fixtureLoader.disgraceThenLoad("fixture two");
    assertEquals("fit.FixtureTwo", f2.getClass().getName());
  }

  public void testLoadFixturesWithFixtureImplied() throws Throwable {
    fixtureLoader.disgraceThenLoad("fit.TheThirdFixture");
    Fixture fixture = fixtureLoader.disgraceThenLoad("the third");
    assertEquals("fit.TheThirdFixture", fixture.getClass().getName());
  }

  public static class CustomFixtureLoader implements FixtureLoaderInterface {
    @Override
    public Fixture disgraceThenLoad(String tableName) throws Throwable {
      return null;
    }

    @Override
    public void addPackageToPath(String name) {
    }
  }

  public void testCustomFixtureLoader() throws Throwable {
/*
    // Due to some error, couldn't manage to execute System.setProperty for unit tests

    String oldFixtureLoader = System.getProperty(FixtureLoader.class.getName());

    FixtureLoader.setInstance(null);
    System.setProperty(FixtureLoader.class.getName(), null);
    assertEquals(FixtureLoader.instance().getClass(), FixtureLoader.class);

    FixtureLoader.setInstance(null);
    System.setProperty(FixtureLoader.class.getName(), "");
    assertEquals(FixtureLoader.instance().getClass(), FixtureLoader.class);

    FixtureLoader.setInstance(null);
    System.setProperty(FixtureLoader.class.getName(), "1_am_sure_there_is_no_class_with_this_name");
    assertEquals(FixtureLoader.instance().getClass(), FixtureLoader.class);

    FixtureLoader.setInstance(null);
    System.setProperty(FixtureLoader.class.getName(), FixtureLoader.class.getName());
    assertEquals(FixtureLoader.instance().getClass(), FixtureLoader.class);
 
    FixtureLoader.setInstance(null);
    System.setProperty(FixtureLoader.class.getName(), CustomFixtureLoader.class.getName());
    assertEquals(FixtureLoader.instance().getClass(), CustomFixtureLoader.class);

    System.setProperty(FixtureLoader.class.getName(), oldFixtureLoader);
*/
    FixtureLoader.setInstance(null);
    assertEquals(FixtureLoader.instanceForClassName(null).getClass(), FixtureLoader.class);
    FixtureLoader.setInstance(null);
    assertEquals(FixtureLoader.instanceForClassName("").getClass(), FixtureLoader.class);
    FixtureLoader.setInstance(null);
    assertEquals(FixtureLoader.instanceForClassName("1_am_sure_there_is_no_class_with_this_name").getClass(), FixtureLoader.class);
    FixtureLoader.setInstance(null);
    assertEquals(FixtureLoader.instanceForClassName(FixtureLoader.class.getName()).getClass(), FixtureLoader.class);
    FixtureLoader.setInstance(null);
    assertEquals(FixtureLoader.instanceForClassName(CustomFixtureLoader.class.getName()).getClass(), CustomFixtureLoader.class);
  }

}
