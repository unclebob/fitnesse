// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import fitnesse.components.ClassPathBuilder;
import fitnesse.testrunner.WikiPageDescriptor;
import fitnesse.testsystems.*;
import fitnesse.testsystems.slim.results.ExceptionResult;
import fitnesse.testsystems.slim.results.TestResult;
import fitnesse.testsystems.slim.tables.Assertion;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.mem.InMemoryPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import java.net.ServerSocket;
import java.net.SocketException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SlimTestSystemTest {

  @Test
  public void translateExceptionMessage() throws Exception {
    assertTranslatedException("Could not find constructor for SomeClass", "NO_CONSTRUCTOR SomeClass");
    assertTranslatedException("Could not invoke constructor for SomeClass", "COULD_NOT_INVOKE_CONSTRUCTOR SomeClass");
    assertTranslatedException("No converter for SomeClass", "NO_CONVERTER_FOR_ARGUMENT_NUMBER SomeClass");
    assertTranslatedException("Method someMethod not found in SomeClass", "NO_METHOD_IN_CLASS someMethod SomeClass");
    assertTranslatedException("The instance someInstance does not exist", "NO_INSTANCE someInstance");
    assertTranslatedException("Could not find class SomeClass", "NO_CLASS SomeClass");
    assertTranslatedException("The instruction [a, b, c] is malformed", "MALFORMED_INSTRUCTION [a, b, c]");
  }


  private void assertTranslatedException(String expected, String message) {
    assertEquals(expected, SlimTestSystem.translateExceptionMessage(message));
  }

}
