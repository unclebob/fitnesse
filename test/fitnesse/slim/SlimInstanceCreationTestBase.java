// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

// Extracted Test class to be implemented by all Java based Slim ports
// The tests for PhpSlim and JsSlim implement this class

public abstract class SlimInstanceCreationTestBase {
  protected StatementExecutorInterface caller;
  protected String testClass = "TestSlim";

  @Before
  public abstract void setUp() throws Exception;

  protected abstract void assertInstanceOfTestSlim(Object x);

  protected abstract String getTestClassPath();

  protected String getTestClassName() {
    return getTestClassPath() + "." + testClass;
  }

  @Test
  public void canCreateInstance() throws Exception {
    caller.create("x", getTestClassName(), new Object[0]);
    Object x = caller.getInstance("x");
    assertInstanceOfTestSlim(x);
  }

  @Test
  public void canCreateInstanceWhenSpecifiedBySymbol() throws Exception {
    caller.assign("X", getTestClassName());
    caller.create("x", "$X", new Object[0]);
    Object x = caller.getInstance("x");
    assertInstanceOfTestSlim(x);
  }

  @Test
  public void canSetActorFromInstanceStoredInSymbol() throws Exception {
    caller.create("x", getTestClassName(), new Object[0]);
    Object x = caller.getInstance("x");
    caller.assign("X", x);
    caller.create("y", "$X", new Object[0]);
    Object y = caller.getInstance("y");
    assertEquals(x, y);
  }

  @Test
  public void canCreateInstanceWithArguments() throws Exception {
    caller.create("x", getTestClassName(), new Object[]{"3"});
    Object x = caller.getInstance("x");
    assertInstanceOfTestSlim(x);
  }

  @Test(expected = SlimException.class)
  public void cantCreateInstanceIfConstructorArgumentBad() throws Exception {
    caller.create("x", getTestClassName(), new Object[]{"notInt"});
  }

  @Test(expected = SlimException.class)
  public void cantCreateInstanceIfConstructorArgumentCountIncorrect() throws Exception {
    caller.create("x", getTestClassName(), new Object[]{"3", "4"});
  }


  @Test(expected = SlimException.class)
  public void throwsInstanceNotCreatedErrorIfNoSuchClass() throws Exception {
    caller.create("x", getTestClassPath() + ".NoSuchClass", new Object[0]);
  }

  @Test(expected = SlimException.class)
  public void throwsInstanceNotCreatedErrorIfNoPublicDefaultConstructor() throws Exception {
    caller.create("x", getTestClassPath() + ".ClassWithNoPublicDefaultConstructor", new Object[0]);
  }

  @Test
  public void canAddPath() throws Exception {
    caller.addPath(getTestClassPath());
    caller.create("x", testClass, new Object[0]);
    Object x = caller.getInstance("x");
    assertInstanceOfTestSlim(x);
  }

  @Test(expected = SlimException.class)
  public void cantCreateInstanceWithoutPath() throws Exception {
    caller.create("x", testClass, new Object[0]);
  }
}
