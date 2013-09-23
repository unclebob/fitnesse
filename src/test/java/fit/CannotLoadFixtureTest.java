// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import fit.exception.ClassIsNotFixtureException;
import fit.exception.FixtureException;
import fit.exception.NoDefaultConstructorFixtureException;
import fit.exception.NoSuchFixtureException;
import fit.testFxtr.NoDefaultConstructorFixture;
import fit.testFxtr.WouldBeFixture;
import org.junit.Before;
import org.junit.Test;

public class CannotLoadFixtureTest {
  private FixtureLoader fixtureLoader;

  @Before
  public void setUp() throws Exception {
    fixtureLoader = new FixtureLoader();
  }

  @Test
  public void testFixtureClassDoesNotExtendFixture() throws Throwable {
    assertCannotLoadFixture(
      "Successfully loaded a fixture that does not extend Fixture!",
      WouldBeFixture.class.getName(), ClassIsNotFixtureException.class);
  }

  @Test
  public void testFixtureClassNotEndingInFixtureDoesNotExtendFixture() throws Throwable {
    assertCannotLoadFixtureAfterChoppingOffFixture(
      "Successfully loaded a fixture that does not extend Fixture!",
      WouldBeFixture.class, ClassIsNotFixtureException.class);
  }

  @Test
  public void testFixtureHasNoDefaultConstructor() throws Throwable {
    assertCannotLoadFixture(
      "Successfully loaded a fixture with no default constructor!",
      NoDefaultConstructorFixture.class.getName(),
      NoDefaultConstructorFixtureException.class);
  }

  @Test
  public void testFixtureClassNotEndingInFixtureHasNoDefaultConstructor()
    throws Throwable {
    assertCannotLoadFixtureAfterChoppingOffFixture(
      "Successfully loaded a fixture with no default constructor!",
      NoDefaultConstructorFixture.class,
      NoDefaultConstructorFixtureException.class);
  }

  @Test
  public void testFixtureNameNotFound() throws Throwable {
    assertCannotLoadFixture("Successfully loaded a nonexistent fixture!",
      "BlahBlahBlah", NoSuchFixtureException.class);
  }

  @Test
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

