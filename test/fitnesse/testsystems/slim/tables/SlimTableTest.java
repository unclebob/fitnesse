// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import fitnesse.testrunner.WikiTestPage;
import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.wiki.WikiPageDummy;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

import static fitnesse.testsystems.slim.tables.Disgracer.disgraceClassName;
import static fitnesse.testsystems.slim.tables.Disgracer.disgraceMethodName;
import static org.junit.Assert.assertEquals;

public class SlimTableTest {
  @Test
  public void gracefulClassNames() throws Exception {
    assertDisgracedClassName("MyClass", "my class");
    assertDisgracedClassName("myclass", "myclass");
    assertDisgracedClassName("x.y", "x.y");
    assertDisgracedClassName("x_y", "x_y");
    assertDisgracedClassName("MeAndMrs_jones", "me and mrs_jones");
    assertDisgracedClassName("PageCreator", "Page creator.");
    assertDisgracedClassName("$symbol", "$symbol");
    assertDisgracedClassName("$MySymbol", "$MySymbol");
    assertDisgracedClassName("myEmbedded$Symbol", "myEmbedded$Symbol");
  }

  private void assertDisgracedClassName(String disgracedName, String sourceName) {
    assertEquals(disgracedName, disgraceClassName(sourceName));
  }

  @Test
  public void gracefulMethodNames() throws Exception {
    assertEquals("myMethodName", disgraceMethodName("my method name"));
    assertEquals("myMethodName", disgraceMethodName("myMethodName"));
    assertEquals("my_method_name", disgraceMethodName("my_method_name"));
    assertEquals("getStringArgs", disgraceMethodName("getStringArgs"));
    assertEquals("setMyVariableName", disgraceMethodName("set myVariableName"));
  }

  @Test
  public void replaceSymbolsShouldReplaceSimpleSymbol() throws Exception {
    SlimTable table = new MockTable();
    table.setSymbol("x", "a");
    assertEquals("this is a", table.replaceSymbols("this is $x"));
  }

  @Test
  public void replaceSymbolsShouldReplaceSecretSymbol() throws Exception {
    SlimTable table = new MockTable();
    table.setSymbol("SECRET_x", "a");
    assertEquals("this is *****", table.replaceSymbols("this is $SECRET_x"));
  }

  @Test
  public void replaceSymbolsShouldReplaceMoreThanOneSymbol() throws Exception {
    SlimTable table = new MockTable();
    table.setSymbol("x", "a");
    table.setSymbol("y", "b");
    assertEquals("this is a and b", table.replaceSymbols("this is $x and $y"));
  }

  @Test
  public void replaceSymbolsShouldConcatenate() throws Exception {
    SlimTable table = new MockTable();
    table.setSymbol("x", "a");
    table.setSymbol("y", "b");
    assertEquals("this is ab", table.replaceSymbols("this is $x$y"));
  }

  @Test
  public void replaceSymbolsShouldReplaceSameSymbolMoreThanOnce() throws Exception {
    SlimTable table = new MockTable();
    table.setSymbol("x", "a");
    assertEquals("this is a and a again", table.replaceSymbols("this is $x and $x again"));
  }

  @Test
  public void replaceSymbolsShouldMatchFullSymbolName() throws Exception {
    SlimTable table = new MockTable();
    table.setSymbol("V", "v");
    table.setSymbol("VX", "x");
    String actual = table.replaceSymbols("$V $VX");
    assertEquals("v x", actual);
  }

  @Test
  public void replaceSymbolsFullExpansion_ShouldReplaceSimpleSymbol() throws Exception {
    SlimTable table = new MockTable();
    table.setSymbol("x", "a");
    assertEquals("this is $x->[a]", table.replaceSymbolsWithFullExpansion("this is $x"));
  }

  @Test
  public void replaceSymbolsFullExpansion_ShouldReplaceSecretSymbol() throws Exception {
    SlimTable table = new MockTable();
    table.setSymbol("SECRET_x", "a");
    assertEquals("this is $SECRET_x->[*****]", table.replaceSymbolsWithFullExpansion("this is $SECRET_x"));
  }

  @Test
  public void replaceSymbolsFullExpansion_ShouldReplaceMoreThanOneSymbol() throws Exception {
    SlimTable table = new MockTable();
    table.setSymbol("x", "a");
    table.setSymbol("y", "b");
    assertEquals("this is $x->[a] and $y->[b]", table.replaceSymbolsWithFullExpansion("this is $x and $y"));
  }

  @Test
  public void replaceSymbolsFullExpansion_ShouldReplaceSameSymbolMoreThanOnce() throws Exception {
    SlimTable table = new MockTable();
    table.setSymbol("x", "a");
    assertEquals("this is $x->[a] and $x->[a] again", table.replaceSymbolsWithFullExpansion("this is $x and $x again"));
  }

  @Test
  public void replaceSymbolsFullExpansion_ShouldMatchFullSymbolName() throws Exception {
    SlimTable table = new MockTable();
    table.setSymbol("V", "v");
    table.setSymbol("VX", "x");
    String actual = table.replaceSymbolsWithFullExpansion("$V $VX");
    assertEquals("$V->[v] $VX->[x]", actual);
  }

  @Test
  public void replaceSymbols_ShouldReplaceConcatenatedSymbols() throws Exception {
    SlimTable table = new MockTable();
    table.setSymbol("x", "1");
    table.setSymbol("y", "a");
    assertEquals("this is $x->[1]1 and $y->[a]b", table.replaceSymbolsWithFullExpansion("this is $x1 and $yb"));
  }



  private static class MockTable extends SlimTable {
    public MockTable() {
      super(null, null, new SlimTestContextImpl(new WikiTestPage(new WikiPageDummy())));
    }

    @Override
    protected String getTableType() {
      return null;
    }

    @Override
    public List<SlimAssertion> getAssertions() {
      return Collections.emptyList();
    }
  }
}
