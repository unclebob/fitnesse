// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.util.List;

import fitnesse.slim.test.TestSlimInvocationAware;

import static org.junit.Assert.assertEquals;


public class SlimMethodInvocationViaAroundTest extends SlimMethodInvocationTest {
  @Override
  protected String getTestClassName() {
    return "fitnesse.slim.test.TestSlimInvocationAware";
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
    TestSlimInvocationAware.resetMethodsCalled();
  }

  @Override
  public void passAndReturnOneZorkWithPropertyEditor() throws Exception {
    super.passAndReturnOneZorkWithPropertyEditor();
    validateMethodIntercepted("oneZork");
  }

  @Override
  public void callNiladicFunction() throws Exception {
    super.callNiladicFunction();
    validateMethodIntercepted("nilad");
  }

  @Override
  public void throwMethodNotCalledErrorIfNoSuchMethodAndListAvailableMethodsSorted() throws Exception {
    super.throwMethodNotCalledErrorIfNoSuchMethodAndListAvailableMethodsSorted();
    validateNoMethodIntercepted();
  }


  @Override
  public void passManyArgs() throws Exception {
    super.passManyArgs();
    validateMethodIntercepted("manyArgs");
  }

  @Override
  public void convertArrayOfIntegersThrowsExceptionIfNotInteger() throws Exception {
    super.convertArrayOfIntegersThrowsExceptionIfNotInteger();
    validateNoMethodIntercepted();
  }

  @Override
  public void convertArraysOfBooleans() throws Exception {
    super.convertArraysOfBooleans();
    validateMethodIntercepted("setBooleanArray", "getBooleanArray");
  }

  @Override
  public void handleEchoNull() throws Exception {
    super.handleEchoNull();
    validateMethodIntercepted("echoString");
  }

  private void validateMethodIntercepted(String... expectedMethodNames) {
    List<String> methodsCalled = TestSlimInvocationAware.getMethodsCalled();
    assertEquals("Wrong number of request intercepted: " + methodsCalled, expectedMethodNames.length, methodsCalled.size());
    for (int i = 0; i < expectedMethodNames.length; i++) {
      String expectedMethodName = expectedMethodNames[i];
      String actualMethodName = methodsCalled.get(i);
      assertEquals("Unexpected request intercepted", expectedMethodName, actualMethodName);
    }
  }

  private void validateNoMethodIntercepted() {
    List<String> methodsCalled = TestSlimInvocationAware.getMethodsCalled();
    assertEquals("Requests intercepted: " + methodsCalled, 0, methodsCalled.size());
  }
}
