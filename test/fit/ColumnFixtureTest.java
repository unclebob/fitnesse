// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

public class ColumnFixtureTest {
  private TestFixture fixture;

  static class TestFixture extends ColumnFixture {
    public int method() {
      return 86;
    }

    public int field;
    public String stringField;

    public String stringMethod() {
      return null;
    }
  }

  @Before
  public void setUp() throws Exception {
    fixture = new TestFixture();
  }

  @Test
  public void testBindColumnToMethod() throws Exception {
    String[] methodSpecifiers = new String[]
      {"method()", "method?", "method!", "string method()", "string method?", "string method!"};
    String[] resultingMethodName = new String[]
      {"method", "method", "method", "stringMethod", "stringMethod", "stringMethod"};
    for (int i = 0; i < methodSpecifiers.length; i++) {
      Parse table = new Parse("<table><tr><td>" + methodSpecifiers[i] + "</td></tr></table>");
      Parse tableHead = table.parts.parts;
      fixture.bind(tableHead);
      assertNotNull(methodSpecifiers[i] + " no binding found.", fixture.columnBindings[0]);
      Method method = fixture.columnBindings[0].adapter.method;
      assertNotNull(methodSpecifiers[i] + "no method found.", method);
      assertEquals(resultingMethodName[i], method.getName());
    }
  }

  @Test
  public void testBindColumnToField() throws Exception {
    Parse table = new Parse("<table><tr><td>field</td></tr></table>");
    Parse tableHead = table.parts.parts;
    fixture.bind(tableHead);
    assertNotNull(fixture.columnBindings[0]);
    Field field = fixture.columnBindings[0].adapter.field;
    assertNotNull(field);
    assertEquals("field", field.getName());
  }

  @Test
  public void testGracefulColumnNames() throws Exception {
    Parse table = new Parse("<table><tr><td>string field</td></tr></table>");
    Parse tableHead = table.parts.parts;
    fixture.bind(tableHead);
    assertNotNull(fixture.columnBindings[0]);
    Field field = fixture.columnBindings[0].adapter.field;
    assertNotNull(field);
    assertEquals("stringField", field.getName());
  }

  @Test
  public void testBindColumnToFieldSymbol() throws Exception {
    Fixture.setSymbol("Symbol", "42");
    Parse table = new Parse("<table><tr><td>field=</td></tr><tr><td>Symbol</td></tr></table>");
    Parse rows = table.parts;
    fixture.doRows(rows);
    Binding binding = fixture.columnBindings[0];
    assertNotNull(binding);
    assertEquals(Binding.RecallBinding.class, binding.getClass());
    Field field = binding.adapter.field;
    assertNotNull(field);
    assertEquals("field", field.getName());
    assertEquals(42, fixture.field);
  }

  @Test
  public void testBindColumnToMethodSymbol() throws Exception {
    Parse table = new Parse("<table><tr><td>=method?</td></tr><tr><td>MethodSymbol</td></tr></table>");
    Parse rows = table.parts;
    fixture.doRows(rows);
    Binding binding = fixture.columnBindings[0];
    assertNotNull(binding);
    assertEquals(Binding.SaveBinding.class, binding.getClass());
    Method method = binding.adapter.method;
    assertEquals("method", method.getName());
    assertEquals("86", Fixture.getSymbol("MethodSymbol"));
  }
}
