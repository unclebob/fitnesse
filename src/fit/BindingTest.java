// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import util.RegexTestCase;
import fit.exception.NoSuchFieldFitFailureException;
import fit.exception.NoSuchMethodFitFailureException;

public class BindingTest extends RegexTestCase {
  private TestFixture fixture;
  private Parse cell1;
  private Parse cell2;
  private Parse cell3;
  private Parse cell4;

  protected void setUp() throws Exception {
    fixture = new TestFixture();
    Parse table = new Parse("<table><tr><td>123</td><td>321</td><td>abc</td><td></td></tr></table>");
    cell1 = table.parts.parts;
    cell2 = table.parts.parts.more;
    cell3 = table.parts.parts.more.more;
    cell4 = table.parts.parts.more.more.more;
  }

  public void testConstruction() throws Throwable {
    assertEquals(Binding.QueryBinding.class, Binding.create(fixture, "intMethod()").getClass());
    assertEquals(Binding.QueryBinding.class, Binding.create(fixture, "intMethod?").getClass());
    assertEquals(Binding.QueryBinding.class, Binding.create(fixture, "intMethod!").getClass());
    assertEquals(Binding.SetBinding.class, Binding.create(fixture, "intField").getClass());
    assertEquals(Binding.RecallBinding.class, Binding.create(fixture, "intField=").getClass());
    assertEquals(Binding.SaveBinding.class, Binding.create(fixture, "=intMethod()").getClass());
    assertEquals(Binding.SaveBinding.class, Binding.create(fixture, "=intField").getClass());
  }

  public static class TestFixture extends Fixture {
    public int intField = 0;

    public int intMethod() {
      return intField;
    }

    public Integer integerField = new Integer(42);

    public Integer integerMethodIsNull() {
      return integerField;
    }
  }

  public void testQueryBinding() throws Throwable {
    Binding binding = Binding.create(fixture, "intMethod()");
    binding.doCell(fixture, cell1);
    assertEquals(1, fixture.counts.wrong);

    fixture.intField = 321;
    binding.doCell(fixture, cell2);
    assertEquals(1, fixture.counts.right);
  }

  public void testSetBinding() throws Throwable {
    Binding binding = Binding.create(fixture, "intField");
    binding.doCell(fixture, cell1);
    assertEquals(123, fixture.intField);

    binding.doCell(fixture, cell2);
    assertEquals(321, fixture.intField);
  }

  public void testQueryBindingWithBlankCell() throws Throwable {
    Binding binding = Binding.create(fixture, "intField");
    binding.doCell(fixture, cell4);
    assertSubString("0", cell4.text());
  }

  public void testSaveBinding() throws Throwable {
    Binding binding = Binding.create(fixture, "=intMethod()");
    binding.doCell(fixture, cell1);
    assertEquals("0", Fixture.getSymbol("123"));
    assertSubString("123  = 0", cell1.text());

    fixture.intField = 999;
    binding.doCell(fixture, cell2);
    assertEquals("999", Fixture.getSymbol("321"));
  }

  public void testSaveBindingWithNull() throws Throwable {
    Binding binding = Binding.create(fixture, "=integerMethodIsNull()");
    fixture.integerField = null;
    binding.doCell(fixture, cell1);
    assertEquals("null", Fixture.getSymbol("123"));
    assertSubString("123  = null", cell1.text());

    binding.doCell(fixture, cell2);
    assertEquals("null", Fixture.getSymbol("321"));
  }

  public void testRecallBinding() throws Throwable {
    Binding binding = Binding.create(fixture, "intField=");
    Fixture.setSymbol("123", "999");
    binding.doCell(fixture, cell1);
    assertEquals(999, fixture.intField);

    binding.doCell(fixture, cell3);
    assertSubString("No such symbol: abc", cell3.text());
  }

  // -AcD- Found this while testing with nulls
  public void testRecallBindingWithNull() throws Throwable {
    Binding binding = Binding.create(fixture, "integerField=");
    Fixture.setSymbol("123", null);
    binding.doCell(fixture, cell1);
    assertEquals(null, fixture.integerField);
  }

  public void testRecallBindingSymbolTableText() throws Throwable {
    Binding binding = Binding.create(fixture, "intField=");
    Fixture.setSymbol("123", "999");
    binding.doCell(fixture, cell1);
    assertEquals("123  = 999", cell1.text());
  }

  public void testUseOfGracefulNamingForMethods() throws Throwable {
    checkForMethodBinding("intMethod()", true);
    checkForMethodBinding("int Method?", true);
    checkForMethodBinding("int method?", true);
    checkForMethodBinding("intmethod?", false);
    checkForMethodBinding("Intmethod?", false);
    checkForMethodBinding("IntMethod?", false);
  }

  public void testUseOfGracefulNamingForFields() throws Throwable {
    checkForFieldBinding("intField", true);
    checkForFieldBinding("int Field", true);
    checkForFieldBinding("int field", true);
    checkForFieldBinding("intfield", false);
    checkForFieldBinding("Intfield", false);
    checkForFieldBinding("IntField", false);
  }

  private void checkForMethodBinding(String name, boolean expected) throws Throwable {
    Binding binding = null;
    try {
      binding = Binding.create(fixture, name);
    }
    catch (NoSuchMethodFitFailureException e) {
      assertFalse("method not found", expected);
      return;
    }
    assertTrue("method was found", expected);
    assertTrue(binding instanceof Binding.QueryBinding);
    assertEquals("intMethod", binding.adapter.method.getName());
  }

  private void checkForFieldBinding(String name, boolean expected) throws Throwable {
    Binding binding = null;
    try {
      binding = Binding.create(fixture, name);
    }
    catch (NoSuchFieldFitFailureException e) {
      assertFalse("field not found", expected);
      return;
    }
    assertTrue("field was found", expected);
    assertTrue(binding instanceof Binding.SetBinding);
    assertEquals("intField", binding.adapter.field.getName());
  }
}
