// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import junit.framework.TestCase;
import fit.exception.ClassIsNotFixtureException;
import fit.exception.FixtureException;
import fit.exception.NoDefaultConstructorFixtureException;
import fit.exception.NoSuchFixtureException;
import fit.testFxtr.NoDefaultConstructorFixture;
import fit.testFxtr.WouldBeFixture;

public class CannotLoadFixtureTest extends TestCase {
  private FixtureLoader fixtureLoader;

  protected void setUp() throws Exception {
    fixtureLoader = new FixtureLoader();
  }

  public void testFixtureClassDoesNotExtendFixture() throws Throwable {
    assertCannotLoadFixture(
      "Successfully loaded a fixture that does not extend Fixture!",
      WouldBeFixture.class.getName(), ClassIsNotFixtureException.class);
  }

  public void testFixtureClassNotEndingInFixtureDoesNotExtendFixture() throws Throwable {
    assertCannotLoadFixtureAfterChoppingOffFixture(
      "Successfully loaded a fixture that does not extend Fixture!",
      WouldBeFixture.class, ClassIsNotFixtureException.class);
  }

  public void testFixtureHasNoDefaultConstructor() throws Throwable {
    assertCannotLoadFixture(
      "Successfully loaded a fixture with no default constructor!",
      NoDefaultConstructorFixture.class.getName(),
      NoDefaultConstructorFixtureException.class);
  }

  public void testFixtureClassNotEndingInFixtureHasNoDefaultConstructor()
    throws Throwable {
    assertCannotLoadFixtureAfterChoppingOffFixture(
      "Successfully loaded a fixture with no default constructor!",
      NoDefaultConstructorFixture.class,
      NoDefaultConstructorFixtureException.class);
  }

  public void testFixtureNameNotFound() throws Throwable {
    assertCannotLoadFixture("Successfully loaded a nonexistent fixture!",
      "BlahBlahBlah", NoSuchFixtureException.class);
  }

  public void testFixtureNameNotFoundEvenAfterAddingOnFixture()
    throws Throwable {
    try {
      fixtureLoader.disgraceThenLoad("BlahBlahBlah");
      fail("Successfully loaded a nonexistent fixture!");
    }
    catch (FixtureException expected) {
      assertEquals(NoSuchFixtureException.class, expected.getClass());
      assertEquals("BlahBlahBlah", expected.fixtureName);
    }
  }

  private String chopOffFixture(Class<?> fixtureClass) {
    return fixtureClass.getName().replaceAll("Fixture", "");
  }

  private void assertCannotLoadFixture(String failureMessage,
                                       String fixtureName, Class<?> expectedExceptionType) throws Throwable {
    try {
      fixtureLoader.disgraceThenLoad(fixtureName);
      fail(failureMessage);
    }
    catch (FixtureException expected) {
      assertEquals(expectedExceptionType, expected.getClass());
      assertEquals(fixtureName, expected.fixtureName);
    }
  }

  private void assertCannotLoadFixtureAfterChoppingOffFixture(
    String failureMessage, Class<?> fixtureClass, Class<?> expectedExceptionType)
    throws Throwable {
    try {
      fixtureLoader.disgraceThenLoad(chopOffFixture(fixtureClass));
      fail(failureMessage);
    }
    catch (FixtureException expected) {
      assertEquals("Got exception: " + expected, expectedExceptionType,
        expected.getClass());
      assertEquals(fixtureClass.getName(), expected.fixtureName);
    }
  }
}

