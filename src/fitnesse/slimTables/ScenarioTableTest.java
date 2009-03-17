// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import fitnesse.responders.run.slimResponder.MockSlimTestContext;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;

public class ScenarioTableTest {
  private WikiPage root;
  private List<Object> instructions;
  public ScenarioTable st;
  private MockSlimTestContext testContext;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("root");
    instructions = new ArrayList<Object>();
  }

  private ScenarioTable makeScenarioTable(String pageContents) throws Exception {
    WikiPageUtil.setPageContents(root, pageContents);
    TableScanner ts = new HtmlTableScanner(root.getData().getHtml());
    Table t = ts.getTable(0);
    testContext = new MockSlimTestContext();
    st = new ScenarioTable(t, "id", testContext);
    st.appendInstructions(instructions);
    return st;
  }

  @Test
  public void noArgs() throws Exception {
    makeScenarioTable("|scenario|myScenario|\n");
    assertEquals("myScenario", st.getName());
    assertEquals(0, st.getInputs().size());
    assertEquals(0, st.getOutputs().size());
  }

  @Test
  public void oneInputArg() throws Exception {
    makeScenarioTable("|scenario|myScenario|input|\n");
    assertEquals("myScenario", st.getName());
    Set<String> inputs = st.getInputs();
    assertEquals(1, inputs.size());
    assertTrue(inputs.contains("input"));
    assertEquals(0, st.getOutputs().size());
  }

  @Test
  public void oneInputArgWithTrailingName() throws Exception {
    makeScenarioTable("|scenario|myScenario|input|trailer|\n");
    assertEquals("MyScenarioTrailer", st.getName());
    Set<String> inputs = st.getInputs();
    assertEquals(1, inputs.size());
    assertTrue(inputs.contains("input"));
    assertEquals(0, st.getOutputs().size());
  }

  @Test
  public void manyInputsNoTrailer() throws Exception {
    makeScenarioTable("|scenario|login user|user name|with password|password|\n");
    assertEquals("LoginUserWithPassword", st.getName());
    Set<String> inputs = st.getInputs();
    assertEquals(2, inputs.size());
    assertTrue(inputs.contains("userName"));
    assertTrue(inputs.contains("password"));
    assertEquals(0, st.getOutputs().size());
  }

  @Test
  public void manyInputsWithTrailer() throws Exception {
    makeScenarioTable("|scenario|login user|user name|with password|password|now|\n");
    assertEquals("LoginUserWithPasswordNow", st.getName());
    Set<String> inputs = st.getInputs();
    assertEquals(2, inputs.size());
    assertTrue(inputs.contains("userName"));
    assertTrue(inputs.contains("password"));
    assertEquals(0, st.getOutputs().size());
  }

  @Test
  public void manyInputsAndOutputs() throws Exception {
     makeScenarioTable("|scenario|login user|user name|with password|password|giving message|message?|and status|login status?|\n");
    assertEquals("LoginUserWithPasswordGivingMessageAndStatus", st.getName());
    Set<String> inputs = st.getInputs();
    assertEquals(2, inputs.size());
    assertTrue(inputs.contains("userName"));
    assertTrue(inputs.contains("password"));
    Set<String> outputs = st.getOutputs();
    assertEquals(2, outputs.size());
    assertTrue(outputs.contains("message"));
    assertTrue(outputs.contains("loginStatus"));
  }

}
