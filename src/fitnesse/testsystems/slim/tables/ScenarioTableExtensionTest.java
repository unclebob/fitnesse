// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim.tables;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.testsystems.slim.HtmlTableScanner;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.SlimTestContextImpl;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.TableScanner;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.mem.InMemoryPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class ScenarioTableExtensionTest {
  private static final String EXTENSION_NAME = "autoArgScenario";
  private WikiPage root;
  private List<Object> instructions;
  public ScenarioTable st;
  private SlimTestContextImpl testContext;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    instructions = new ArrayList<Object>();
    SlimTableFactory.addTableType(EXTENSION_NAME, AutoArgScenarioTable.class);
  }

  private ScenarioTable makeScenarioTable(String contents)
      throws Exception {
    String pageContents = "|" + EXTENSION_NAME + "|" + contents + "|\n";
    WikiPageUtil.setPageContents(root, pageContents);

    TableScanner ts = new HtmlTableScanner(root.getData().getHtml());
    Table t = ts.getTable(0);
    testContext = new SlimTestContextImpl();
    st = new AutoArgScenarioTable(t, "id", testContext);
    instructions.addAll(st.getAssertions());

    return st;
  }

  @Test
  public void noArgs() throws Exception {
    makeScenarioTable("myScenario");
    assertEquals("myScenario", st.getName());
    assertEquals(0, st.getInputs().size());
    assertEquals(0, st.getOutputs().size());
    assertFalse(st.isParameterized());
  }

  @Test
  public void parameterizedNameWithOneArg() throws Exception {
    makeScenarioTable("login user|\n|enter|@{name}|for|login");
    assertEquals("LoginUser", st.getName());

    Set<String> inputs = st.getInputs();
    assertEquals(1, inputs.size());
    assertTrue(inputs.contains("name"));
    assertTrue(st.isParameterized());
  }

  @Test
  public void parameterizedNameWithTwoArgs() throws Exception {
    makeScenarioTable("login user|\n|enter|@{name}|for|login|\n|enter|@{password}|for|secret");
    assertEquals("LoginUser", st.getName());

    Set<String> inputs = st.getInputs();
    assertEquals(2, inputs.size());
    assertTrue(inputs.contains("name"));
    assertTrue(inputs.contains("password"));
    assertTrue(st.isParameterized());
  }

  @Test
  public void parameterizedNameWithOneArgAndWordWithEmbeddedUnderscore()
      throws Exception {
    makeScenarioTable("login user_name|\n|enter|@{name}|for|login|\n|enter|bla|for|secret");
    assertEquals("LoginUser_name", st.getName());

    Set<String> inputs = st.getInputs();
    assertEquals(1, inputs.size());
    assertTrue(inputs.contains("name"));
    assertTrue(st.isParameterized());
  }

  @Test
  public void parameterizedNameWithRepeatingArgs() throws Exception {
    makeScenarioTable("login user|\n|enter|@{name}|for|login|\n|enter|@{password}|for|secret|\n|check|current user|@{name}");
    assertEquals("LoginUser", st.getName());

    Set<String> inputs = st.getInputs();
    assertEquals(2, inputs.size());
    assertTrue(inputs.contains("name"));
    assertTrue(inputs.contains("password"));
    assertTrue(st.isParameterized());
  }

  @Test
  public void parameterizedNameWithMultipleArgsInOneCell() throws Exception {
    makeScenarioTable("login user|\n|enter|id:@{name}:@{password}|for|user");
    assertEquals("LoginUser", st.getName());

    Set<String> inputs = st.getInputs();
    assertEquals(2, inputs.size());
    assertTrue(inputs.contains("name"));
    assertTrue(inputs.contains("password"));
    assertTrue(st.isParameterized());
  }

  /**
   * ScenarioTable that looks for input parameters in all its rows, without the
   * parameters having to be specified in the first row also.
   */
  public static class AutoArgScenarioTable extends ScenarioTable {
    private final static Pattern ARG_PATTERN = Pattern.compile("@\\{(.+?)\\}");

    private Set<String> inputs;

    public AutoArgScenarioTable(Table table, String tableId, SlimTestContext testContext) {
      super(table, tableId, testContext);
    }

    @Override
    public List<SlimAssertion> getAssertions() throws SyntaxError {
      inputs = findInputs();
      return super.getAssertions();
    }

    @Override
    protected boolean determineParameterized() {
      return !inputs.isEmpty();
    }

    @Override
    protected void getScenarioArguments() {
      for (String input : inputs) {
        addInput(input);
      }
    }

    private Set<String> findInputs() {
      Set<String> found = new LinkedHashSet<String>();
      int rowCount = table.getRowCount();
      for (int row = 0; row < rowCount; row++) {
        int columnCount = table.getColumnCountInRow(row);
        for (int column = 0; column < columnCount; column++) {
          String cellContent = table.getCellContents(column, row);
          Matcher m = ARG_PATTERN.matcher(cellContent);
          while (m.find()) {
            String input = m.group(1);
            found.add(input);
          }
        }
      }
      return found;
    }
  }
}
