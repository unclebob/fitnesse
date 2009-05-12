// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class FitNesseContextTest {
//  public static ClassCreatedFromRuby rubyResult;

  @Test
  public void shouldReportPortOfMinusOneIfNotInitialized() {
    FitNesseContext.globalContext = null;
    assertEquals(-1, FitNesseContext.getPort());
  }

  @Test
  public void shouldHavePortSetAfterFitNesseObjectConstructed() throws Exception {
    FitNesseContext context = new FitNesseContext();
    context.port = 9988;
    new FitNesse(context, false);
    assertEquals(9988, FitNesseContext.getPort());
  }

//  @Test
//  public void jrubyCanBeCalled() throws Exception {
//    FitNesseContext context = new FitNesseContext();
//    Ruby ruby = context.getRubyRuntime();
//    rubyResult = null;
//    ruby.executeScript("Java::fitnesse.FitNesseContextTest.rubyResult = Java::fitnesse.FitNesseContextTest::ClassCreatedFromRuby.new", "junk");
//    assertNotNull(rubyResult);
//    assertTrue(rubyResult instanceof ClassCreatedFromRuby);
//    IRubyObject createdClass = ruby.executeScript("Java::fitnesse.FitNesseContextTest::ClassCreatedFromRuby.new", "x");
//    assertNotNull(createdClass);
//    assertEquals(ClassCreatedFromRuby.class, createdClass.getJavaClass());
//    assertEquals("zap", createdClass.callMethod(ruby.getCurrentContext(), "zork").toString());
//  }
//
//  public static class ClassCreatedFromRuby {
//    public String zork() {
//      return "zap";
//    }
//
//  }
}
