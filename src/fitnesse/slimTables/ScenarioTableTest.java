// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slimTables;

import fitnesse.responders.run.slimResponder.MockSlimTestContext;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


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

    private ScenarioTable makeScenarioTable(String pageContents)
        throws Exception {
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
        assertFalse(st.isParameterized());
    }

    @Test
    public void oneInputArg() throws Exception {
        makeScenarioTable("|scenario|myScenario|input|\n");
        assertEquals("myScenario", st.getName());

        Set<String> inputs = st.getInputs();
        assertEquals(1, inputs.size());
        assertTrue(inputs.contains("input"));
        assertEquals(0, st.getOutputs().size());
        assertFalse(st.isParameterized());
    }

    @Test
    public void oneInputArgWithTrailingName() throws Exception {
        makeScenarioTable("|scenario|myScenario|input|trailer|\n");
        assertEquals("MyScenarioTrailer", st.getName());

        Set<String> inputs = st.getInputs();
        assertEquals(1, inputs.size());
        assertTrue(inputs.contains("input"));
        assertEquals(0, st.getOutputs().size());
        assertFalse(st.isParameterized());
    }

    @Test
    public void manyInputsNoTrailer() throws Exception {
        makeScenarioTable(
            "|scenario|login user|user name|with password|password|\n");
        assertEquals("LoginUserWithPassword", st.getName());

        Set<String> inputs = st.getInputs();
        assertEquals(2, inputs.size());
        assertTrue(inputs.contains("userName"));
        assertTrue(inputs.contains("password"));
        assertEquals(0, st.getOutputs().size());
        assertFalse(st.isParameterized());
    }

    @Test
    public void manyInputsWithTrailer() throws Exception {
        makeScenarioTable(
            "|scenario|login user|user name|with password|password|now|\n");
        assertEquals("LoginUserWithPasswordNow", st.getName());

        Set<String> inputs = st.getInputs();
        assertEquals(2, inputs.size());
        assertTrue(inputs.contains("userName"));
        assertTrue(inputs.contains("password"));
        assertEquals(0, st.getOutputs().size());
        assertFalse(st.isParameterized());
    }

    @Test
    public void manyInputsAndOutputs() throws Exception {
        makeScenarioTable(
            "|scenario|login user|user name|with password|password|giving message|message?|and status|login status?|\n");
        assertEquals("LoginUserWithPasswordGivingMessageAndStatus", st.getName());

        Set<String> inputs = st.getInputs();
        assertEquals(2, inputs.size());
        assertTrue(inputs.contains("userName"));
        assertTrue(inputs.contains("password"));

        Set<String> outputs = st.getOutputs();
        assertEquals(2, outputs.size());
        assertTrue(outputs.contains("message"));
        assertTrue(outputs.contains("loginStatus"));
        assertFalse(st.isParameterized());
    }

    @Test
    public void simpleNameWithUnnamedArguments() throws Exception {
        makeScenarioTable("|scenario|f|a||b|\n");
        assertEquals("f", st.getName());

        Set<String> inputs = st.getInputs();
        assertEquals(2, inputs.size());
        assertTrue(inputs.contains("a"));
        assertTrue(inputs.contains("b"));
        assertFalse(st.isParameterized());
    }

    @Test
    public void parameterizedNameWithOneArgAtEnd() throws Exception {
        makeScenarioTable("|scenario|login user _|name|\n");
        assertEquals("LoginUser", st.getName());

        Set<String> inputs = st.getInputs();
        assertEquals(1, inputs.size());
        assertTrue(inputs.contains("name"));
        assertTrue(st.isParameterized());
    }

    @Test
    public void parameterizedNameWithOneArgInMiddle() throws Exception {
        makeScenarioTable("|scenario|login _ user|name|\n");
        assertEquals("LoginUser", st.getName());

        Set<String> inputs = st.getInputs();
        assertEquals(1, inputs.size());
        assertTrue(inputs.contains("name"));
        assertTrue(st.isParameterized());
    }

    @Test
    public void parameterizedNameWithTwoArgs() throws Exception {
        makeScenarioTable("|scenario|login user _ password _|name,password|\n");
        assertEquals("LoginUserPassword", st.getName());

        Set<String> inputs = st.getInputs();
        assertEquals(2, inputs.size());
        assertTrue(inputs.contains("name"));
        assertTrue(inputs.contains("password"));
        assertTrue(st.isParameterized());
    }

    @Test
    public void getArgumentsFromParameterizedInvocation()
        throws Exception {
        makeScenarioTable("|scenario|login user _ password _|name,password|\n");

        String[] arguments = st.matchParameters("login user Bob password xyzzy");
        assertEquals(2, arguments.length);
        assertEquals("Bob", arguments[0]);
        assertEquals("xyzzy", arguments[1]);
    }

    @Test
    public void parameterizedNameWithOneArgAtEndAndWordWithEmbeddedUnderscore()
        throws Exception {
        makeScenarioTable("|scenario|login user_name _|name|\n");
        assertEquals("LoginUser_name", st.getName());

        Set<String> inputs = st.getInputs();
        assertEquals(1, inputs.size());
        assertTrue(inputs.contains("name"));
        assertTrue(st.isParameterized());
    }

    @Test
    public void underscoreInWordNotParameterized() throws Exception {
        makeScenarioTable("|scenario|login user_name |name|\n");
        assertEquals("LoginUser_name", st.getName());

        Set<String> inputs = st.getInputs();
        assertEquals(1, inputs.size());
        assertTrue(inputs.contains("name"));
        assertFalse(st.isParameterized());
    }
}
