// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import fit.exception.NoSuchFieldFitFailureException;
import fit.exception.NoSuchMethodFitFailureException;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static util.RegexTestCase.assertSubString;

public class BindingTest {
  private TestFixture fixture;
  private Parse cell1;
  private Parse cell2;
  private Parse cell3;
  private Parse cell4;

  @Before
  public void setUp() throws Exception {
    fixture = new TestFixture();
    Parse table = new Parse("<table><tr><td>123</td><td>321</td><td>abc</td><td></td></tr></table>");
    cell1 = table.parts.parts;
    cell2 = table.parts.parts.more;
    cell3 = table.parts.parts.more.more;
    cell4 = table.parts.parts.more.more.more;
  }

  @Test
  public void testConstruction() throws Throwable {
    assertEquals(Binding.QueryBinding.class, Binding.create(fixture, "intMethod()").getClass());
    assertEquals(Binding.QueryBinding.class, Binding.create(fixture, "intMethod?").getClass());
    assertEquals(Binding.QueryBinding.class, Binding.create(fixture, "intMethod!").getClass());
    assertEquals(Binding.SetBinding.class, Binding.create(fixture, "intField").getClass());
    assertEquals(Binding.RecallBinding.class, Binding.create(fixture, "intField=").getClass());
    assertEquals(Binding.SaveBinding.class, Binding.create(fixture, "=intMethod()").getClass());
    assertEquals(Binding.SaveBinding.class, Binding.create(fixture, "=intField").getClass());
    assertEquals(Binding.SetBinding.class, Binding.create(fixture, "privateIntField").getClass());
    assertEquals(Binding.RecallBinding.class, Binding.create(fixture, "privateIntField=").getClass());
    assertEquals(Binding.SaveBinding.class, Binding.create(fixture, "=privateIntField").getClass());
    assertEquals(Binding.RegexQueryBinding.class, Binding.create(fixture, "intMethod!!").getClass());
    assertEquals(Binding.RegexQueryBinding.class, Binding.create(fixture, "intMethod??").getClass());
  }

  @Test
  public void unmatchedPattern() throws Throwable {
    assertEquals(Binding.NullBinding.class, Binding.create(fixture, "**=**").getClass());
  }

  public static class TestFixture extends ParentTestFixture {
  }

  public static class ParentTestFixture extends Fixture {
    public int intField = 0;
    private int privateIntField = 0;
    public boolean spyWasCalled = false;

    public int intMethod() {
      return intField;
    }

    public int getPrivateIntField() {
      return privateIntField;
    }

    public Integer integerField = new Integer(42);

    public Integer integerMethodIsNull() {
      return integerField;
    }

    public String spyMethod() {
      spyWasCalled = true;
      return "spyMethod";
    }
  }

  @Test
  public void testQueryBinding() throws Throwable {
    Binding binding = Binding.create(fixture, "intMethod()");
    binding.doCell(fixture, cell1);
    assertEquals(1, fixture.counts.wrong);

    fixture.intField = 321;
    binding.doCell(fixture, cell2);
    assertEquals(1, fixture.counts.right);
  }

  @Test
  public void regexSetBindingCallsMethod() throws Throwable {
    Binding binding = Binding.create(fixture, "spyMethod!!");
    binding.doCell(fixture, cell1);
    assertTrue(fixture.spyWasCalled);
    assertTrue(binding.adapter.isRegex);
  }

  @Test
  public void testSetBinding() throws Throwable {
    Binding binding = Binding.create(fixture, "intField");
    binding.doCell(fixture, cell1);
    assertEquals(123, fixture.intField);

    binding.doCell(fixture, cell2);
    assertEquals(321, fixture.intField);
  }

  @Test
  public void testPrivateSetBinding() throws Throwable {
    Binding binding = Binding.create(fixture, "privateIntField");
    binding.doCell(fixture, cell1);
    assertEquals(123, fixture.getPrivateIntField());

    binding.doCell(fixture, cell2);
    assertEquals(321, fixture.getPrivateIntField());
  }

  @Test
  public void testQueryBindingWithBlankCell() throws Throwable {
    Binding binding = Binding.create(fixture, "intField");
    binding.doCell(fixture, cell4);
    assertSubString("0", cell4.text());
  }

  @Test
  public void testPrivateQueryBindingWithBlankCell() throws Throwable {
    Binding binding = Binding.create(fixture, "privateIntField");
    binding.doCell(fixture, cell4);
    assertSubString("0", cell4.text());
  }

  @Test
  public void testSaveBinding() throws Throwable {
    Binding binding = Binding.create(fixture, "=intMethod()");
    binding.doCell(fixture, cell1);
    assertEquals("0", Fixture.getSymbol("123"));
    assertSubString("123  = 0", cell1.text());

    fixture.intField = 999;
    binding.doCell(fixture, cell2);
    assertEquals("999", Fixture.getSymbol("321"));
  }

  @Test
  public void testSaveBindingWithNull() throws Throwable {
    Binding binding = Binding.create(fixture, "=integerMethodIsNull()");
    fixture.integerField = null;
    binding.doCell(fixture, cell1);
    assertEquals("null", Fixture.getSymbol("123"));
    assertSubString("123  = null", cell1.text());

    binding.doCell(fixture, cell2);
    assertEquals("null", Fixture.getSymbol("321"));
  }

  @Test
  public void testRecallBinding() throws Throwable {
    Binding binding = Binding.create(fixture, "intField=");
    Fixture.setSymbol("123", "999");
    binding.doCell(fixture, cell1);
    assertEquals(999, fixture.intField);

    binding.doCell(fixture, cell3);
    assertSubString("No such symbol: abc", cell3.text());
  }

  @Test
  public void testPrivateRecallBinding() throws Throwable {
    Binding binding = Binding.create(fixture, "privateIntField=");
    Fixture.setSymbol("123", "999");
    binding.doCell(fixture, cell1);
    assertEquals(999, fixture.getPrivateIntField());

    binding.doCell(fixture, cell3);
    assertSubString("No such symbol: abc", cell3.text());
  }

  // -AcD- Found this while testing with nulls
  @Test
  public void testRecallBindingWithNull() throws Throwable {
    Binding binding = Binding.create(fixture, "integerField=");
    Fixture.setSymbol("123", null);
    binding.doCell(fixture, cell1);
    assertEquals(null, fixture.integerField);
  }

  @Test
  public void testRecallBindingSymbolTableText() throws Throwable {
    Binding binding = Binding.create(fixture, "intField=");
    Fixture.setSymbol("123", "999");
    binding.doCell(fixture, cell1);
    assertEquals("123  = 999", cell1.text());
  }

  @Test
  public void testPrivateRecallBindingSymbolTableText() throws Throwable {
    Binding binding = Binding.create(fixture, "privateIntField=");
    Fixture.setSymbol("123", "999");
    binding.doCell(fixture, cell1);
    assertEquals("123  = 999", cell1.text());
  }

  @Test
  public void testUseOfGracefulNamingForMethods() throws Throwable {
    checkForMethodBinding("intMethod()", true);
    checkForMethodBinding("int Method?", true);
    checkForMethodBinding("int method?", true);
    checkForMethodBinding("intmethod?", false);
    checkForMethodBinding("Intmethod?", false);
    checkForMethodBinding("IntMethod?", false);
  }

  @Test
  public void testUseOfGracefulNamingForFields() throws Throwable {
    checkForFieldBinding("intField", true);
    checkForFieldBinding("int Field", true);
    checkForFieldBinding("int field", true);
    checkForFieldBinding("intfield", false);
    checkForFieldBinding("Intfield", false);
    checkForFieldBinding("IntField", false);
  }

  @Test
  public void testUseOfGracefulNamingForPrivateFields() throws Throwable {
    checkForPrivateFieldBinding("privateIntField", true);
    checkForPrivateFieldBinding("private int Field", true);
    checkForPrivateFieldBinding("private Int field", true);
    checkForPrivateFieldBinding("private int field", true);
    checkForPrivateFieldBinding("private Int Field", true);
    checkForPrivateFieldBinding("privateintfield", false);
    checkForPrivateFieldBinding("PrivateIntfield", false);
    checkForPrivateFieldBinding("privateintField", false);
    checkForPrivateFieldBinding("PrivateIntField", false);
  }

  private void checkForMethodBinding(String name, boolean expected) throws Throwable {
    Binding binding = null;
    try {
      binding = Binding.create(fixture, name);
    } catch (NoSuchMethodFitFailureException e) {
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
    } catch (NoSuchFieldFitFailureException e) {
      assertFalse("field not found", expected);
      return;
    }
    assertTrue("field was found", expected);
    assertTrue(binding instanceof Binding.SetBinding);
    assertEquals("intField", binding.adapter.field.getName());
  }

  private void checkForPrivateFieldBinding(String name, boolean expected) throws Throwable {
    Binding binding = null;
    try {
      binding = Binding.create(fixture, name);
    } catch (NoSuchFieldFitFailureException e) {
      assertFalse("field not found", expected);
      return;
    }
    assertTrue("field was found", expected);
    assertTrue(binding instanceof Binding.SetBinding);
    assertEquals("privateIntField", binding.adapter.field.getName());
  }
}
