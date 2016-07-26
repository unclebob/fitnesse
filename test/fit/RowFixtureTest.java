// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

package fit;

import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class RowFixtureTest extends TestCase {

  class BusinessObject {
    private String[] strs;

    public BusinessObject(String[] strs) {
      this.strs = Arrays.copyOf(strs, strs.length);
    }

    public String[] getStrings() {
      return strs;
    }
  }

  public RowFixtureTest(String name) {
    super(name);
  }

  public void testMatch() throws Exception {

    /*
    Now back to the bug I found: The problem stems from the fact
    that java doesn't do deep equality for arrays. Little known to
    me (I forget easily ;-), java arrays are equal only if they
    are identical. Unfortunately the 2 sort methods returns a map
    that is directly keyed on the value of the column without
    considering this little fact. Conclusion there is a missing
    and a surplus row where there should be one right row.
    -- Jacques Morel
    */

    RowFixture fixture = new TestRowFixture();
    TypeAdapter arrayAdapter = TypeAdapter.on(fixture,
      BusinessObject.class.getMethod("getStrings", new Class[0]));
    Binding binding = new Binding.QueryBinding();
    binding.adapter = arrayAdapter;
    fixture.columnBindings = new Binding[]{binding};

    List<BusinessObject> computed = new LinkedList<>();
    computed.add(new BusinessObject(new String[]{"1"}));
    LinkedList<Parse> expected = new LinkedList<>();
    expected.add(new Parse("tr", "", new Parse("td", "1", null, null), null));
    fixture.match(expected, computed, 0);
    assertEquals("right", 1, fixture.counts.right);
    assertEquals("exceptions", 0, fixture.counts.exceptions);
    assertEquals("missing", 0, fixture.missing.size());
    assertEquals("surplus", 0, fixture.surplus.size());
  }

  public void testBindColumnToField() throws Exception {
    RowFixture fixture = new SimpleRowFixture();
    Parse table = new Parse("<table><tr><td>field</td></tr></table>");
    Parse tableHead = table.parts.parts;
    fixture.bindColumnHeadersToMethodsAndFields(tableHead);
    assertNotNull(fixture.columnBindings[0]);
    Field field = fixture.columnBindings[0].adapter.field;
    assertNotNull(field);
    assertEquals("field", field.getName());
    assertEquals(int.class, field.getType());
  }

  private class SimpleRowFixture extends RowFixture {
    @Override
    public Class<?> getTargetClass()             // get expected type of row
    {
      return SimpleBusinessObject.class;
    }

    @Override
    public Object[] query() throws Exception  // get rows to be compared
    {
      return new Object[0];
    }
  }

  private class SimpleBusinessObject {
    /** referenced via reflection */
    @SuppressWarnings("unused")
    public int field;
  }

  private class TestRowFixture extends RowFixture {
    @Override
    public Object[] query() throws Exception  // get rows to be compared
    {
      return new Object[0];
    }

    @Override
    public Class<?> getTargetClass()             // get expected type of row
    {
      return BusinessObject.class;
    }
  }
}
