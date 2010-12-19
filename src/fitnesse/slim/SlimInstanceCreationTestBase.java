// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
    Object response = caller.create("x", getTestClassName(), new Object[0]);
    assertEquals("OK", response);
    Object x = caller.getInstance("x");
    assertInstanceOfTestSlim(x);
  }

  @Test
  public void canCreateInstanceWhenSpecifiedBySymbol() throws Exception {
    caller.setVariable("X", getTestClassName());
    Object response = caller.create("x", "$X", new Object[0]);
    assertEquals("OK", response);
    Object x = caller.getInstance("x");
    assertInstanceOfTestSlim(x);
  }

  @Test
  public void canSetActorFromInstanceStoredInSymbol() throws Exception {
    Object response = caller.create("x", getTestClassName(), new Object[0]);
    Object x = caller.getInstance("x");
    caller.setVariable("X", x);
    response = caller.create("y", "$X", new Object[0]);
    assertEquals("OK", response);
    Object y = caller.getInstance("y");
    assertEquals(x, y);
  }

  @Test
  public void canCreateInstanceWithArguments() throws Exception {
    Object response = caller.create("x", getTestClassName(), new Object[]{"3"});
    assertEquals("OK", response);
    Object x = caller.getInstance("x");
    assertInstanceOfTestSlim(x);
  }

  @Test
  public void cantCreateInstanceIfConstructorArgumentBad() throws Exception {
    String result = (String) caller.create("x", getTestClassName(), new Object[]{"notInt"});
    assertException("message:<<COULD_NOT_INVOKE_CONSTRUCTOR " + getTestClassName() + "[1]", result);
  }

  @Test
  public void cantCreateInstanceIfConstructorArgumentCountIncorrect() throws Exception {
    String result = (String) caller.create("x", getTestClassName(), new Object[]{"3", "4"});
    assertException("message:<<COULD_NOT_INVOKE_CONSTRUCTOR " + getTestClassName() + "[2]>>", result);
  }


  @Test
  public void throwsInstanceNotCreatedErrorIfNoSuchClass() throws Exception {
    String result = (String) caller.create("x", getTestClassPath() + ".NoSuchClass", new Object[0]);
    assertException("message:<<COULD_NOT_INVOKE_CONSTRUCTOR " + getTestClassPath() + ".NoSuchClass[0]>>", result);
  }

  @Test
  public void throwsInstanceNotCreatedErrorIfNoPublicDefaultConstructor() throws Exception {
    String result = (String) caller.create("x", getTestClassPath() + ".ClassWithNoPublicDefaultConstructor", new Object[0]);
    assertException("message:<<COULD_NOT_INVOKE_CONSTRUCTOR " + getTestClassPath() + ".ClassWithNoPublicDefaultConstructor[0]>>", result);
  }

  @Test
  public void canAddPath() {
    caller.addPath(getTestClassPath());
    Object response = caller.create("x", testClass, new Object[0]);
    assertEquals("OK", response);
    Object x = caller.getInstance("x");
    assertInstanceOfTestSlim(x);
  }

  @Test
  public void cantCreateInstanceWithoutPath() {
    String result = (String) caller.create("x", testClass, new Object[0]);
    assertException("message:<<COULD_NOT_INVOKE_CONSTRUCTOR " + testClass + "[0]>>", result);
  }

  protected void assertException(String message, String result) {
    assertTrue(result, result.indexOf(SlimServer.EXCEPTION_TAG) != -1 && result.indexOf(message) != -1);
  }
}
