// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import fit.exception.FitParseException;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class ColumnFixtureTest extends ColumnFixture {
  private ColumnFixtureTest selfShuntFixture;
  public int field;
  public String stringField;
  private boolean resetCalledBeforeRow = false;
  private boolean resetCalled = false;
  private int executeCalled = 0;
  private int counter = 0;
  private boolean executeCalledBeforeMethod = false;
  private boolean executeWillThrow = false;

  public int method() {
    return 86;
  }

  public int method2() {
    return 88;
  }

  public int count() {
    counter++;
    return counter;
  }

  @Override
  public void execute() throws Exception {
    if (executeWillThrow)
      throw new Exception("Execute threw.");
    executeCalled++;
  }

  @Override
  public void reset() throws Exception {
    resetCalled = true;
  }

  public String resetCheck() {
    resetCalledBeforeRow = resetCalled;
    return "";
  }

  public boolean checkExecute() {
    executeCalledBeforeMethod = (executeCalled != 0);
    return executeCalledBeforeMethod;
  }

  public String stringMethod() {
    return null;
  }

  @Before
  public void setUp() throws Exception {
    selfShuntFixture = this;
  }

  private String getStringFor(Parse table) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter out = new PrintWriter(stringWriter);
    table.print(out);
    out.flush();
    out.close();
    return stringWriter.toString();
  }

  private Parse tableOf(String rows) throws FitParseException {
    return new Parse("<table>" + rows + "</table>");
  }

  private String row(String... cells) {
    String row = "<tr>";
    for (String cell : cells)
      row += "<td>" + cell + "</td>";
    row += "</tr>";
    return row;
  }

  private Parse doTableOf(String rows) throws FitParseException {
    Parse table = tableOf(rows);
    selfShuntFixture.doRows(table.parts);
    return table;
  }

  private Parse bindTableOf(String rows) throws FitParseException {
    Parse table = tableOf(rows);
    selfShuntFixture.bindColumnHeadersToMethodsAndFields(table.parts.parts);
    return table;
  }

  @Test
  public void testBindColumnToMethod() throws Exception {
    String[] methodSpecifiers = new String[]
      {"method()", "method?", "method!", "string method()", "string method?", "string method!"};
    String[] resultingMethodName = new String[]
      {"method", "method", "method", "stringMethod", "stringMethod", "stringMethod"};
    for (int i = 0; i < methodSpecifiers.length; i++) {
      bindTableOf(row(methodSpecifiers[i]));
      assertNotNull(methodSpecifiers[i] + " no binding found.", selfShuntFixture.columnBindings[0]);
      Method method = selfShuntFixture.columnBindings[0].adapter.method;
      assertNotNull(methodSpecifiers[i] + "no method found.", method);
      assertEquals(resultingMethodName[i], method.getName());
    }
  }

  @Test
  public void testBindColumnToField() throws Exception {
    bindTableOf(row("field"));
    assertNotNull(selfShuntFixture.columnBindings[0]);
    Field field = selfShuntFixture.columnBindings[0].adapter.field;
    assertNotNull(field);
    assertEquals("field", field.getName());
  }

  @Test
  public void testGracefulColumnNames() throws Exception {
    bindTableOf(row("string field"));
    assertNotNull(selfShuntFixture.columnBindings[0]);
    Field field = selfShuntFixture.columnBindings[0].adapter.field;
    assertNotNull(field);
    assertEquals("stringField", field.getName());
  }

  @Test
  public void testBindColumnToFieldSymbol() throws Exception {
    Fixture.setSymbol("Symbol", "42");
    doTableOf(row("field=") + row("Symbol"));
    Binding binding = selfShuntFixture.columnBindings[0];
    assertNotNull(binding);
    assertEquals(Binding.RecallBinding.class, binding.getClass());
    Field field = binding.adapter.field;
    assertNotNull(field);
    assertEquals("field", field.getName());
    assertEquals(42, selfShuntFixture.field);
  }

  @Test
  public void testBindColumnToMethodSymbol() throws Exception {
    doTableOf(row("=method?") + row("MethodSymbol"));
    Binding binding = selfShuntFixture.columnBindings[0];
    assertNotNull(binding);
    assertEquals(Binding.SaveBinding.class, binding.getClass());
    Method method = binding.adapter.method;
    assertEquals("method", method.getName());
    assertEquals("86", Fixture.getSymbol("MethodSymbol"));
  }

  @Test
  public void ensureResetCalled() throws Exception {
    doTableOf(row("resetCheck?") + row("whatever"));
    assertTrue(resetCalled);
  }

  @Test
  public void ensureResetCalledBeforeRows() throws Exception {
    doTableOf(row("resetCheck?") + row("whatever"));
    assertTrue(resetCalledBeforeRow);
  }

  @Test
  public void ensureExecuteIsCalledForRowWithMethod() throws Exception {
    doTableOf(row("resetCheck?") + row("whatever"));
    assertEquals(1, executeCalled);
  }

  @Test
  public void ensureExecuteIsCalledForRowWithOnlyVariables() throws Exception {
    doTableOf(row("stringField") + row("whatever"));
    assertEquals(1, executeCalled);
  }

  @Test
  public void ensureExecuteIsCalledOnlyOnceIfOneRow() throws Exception {
    doTableOf(row("count?", "count?") + row("1", "2"));
    assertEquals(2, counter);
    assertEquals(1, executeCalled);
  }

  @Test
  public void ensureExecuteIsCalledOncePerRow() throws Exception {
    doTableOf(row("count?", "count?") + row("1", "2") + row("3","4"));
    assertEquals(4, counter);
    assertEquals(2, executeCalled);
  }

  @Test
  public void ensureExecuteIsCalledBeforeAnyMethods() throws Exception {
    doTableOf(row("field", "checkExecute?") + row("1", "true"));
    assertTrue(executeCalledBeforeMethod);
  }

  @Test
  public void exceptionForBadMethod() throws Exception {
    Parse table = doTableOf(row("noSuchMethod?") + row("1"));
    String s = getStringFor(table);
    assertTrue(s.contains("Could not find method: noSuchMethod?."));
  }

  @Test
  public void exceptionForBadOutputType() throws Exception {
    Parse table = doTableOf(row("method?") + row("NotAnInt"));
    String s = getStringFor(table);
    assertTrue(s.contains("Could not parse: NotAnInt, expected type: int"));
  }

  @Test
  public void exceptionForBadInputType() throws Exception {
    Parse table = doTableOf(row("field") + row("NotAnInt"));
    String s = getStringFor(table);
    assertTrue(s.contains("java.lang.NumberFormatException: For input string: \"NotAnInt\""));
  }

  @Test
  public void messageIfExecuteThrowsBeforeFirstMethodAndThenContinues() throws Exception {
    executeWillThrow = true;
    Parse table = doTableOf(row("field", "count?") + row("1", "true"));
    String s = getStringFor(table);
    assertTrue(s.contains("Execute threw."));
    assertEquals(1, counter)  ;
  }

  @Test
  public void messageIfExecuteThrowsWhenNoMethods() throws Exception {
    executeWillThrow = true;
    Parse table = doTableOf(row("field", "field") + row("1", "1"));
    String s = getStringFor(table);
    System.out.println("s = " + s);
    assertTrue(s.contains("Execute threw."));
  }
}
