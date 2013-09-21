package fitnesse.testsystems;

import org.junit.Test;

import static fitnesse.testsystems.ExecutionResult.*;
import static org.junit.Assert.assertEquals;


public class ExecutionResultTest {

	  @Test
	  public void summaryClass() throws Exception {
	    assertEquals(PASS, getExecutionResult("TestPage", new TestSummary(1, 0, 0, 0)));
	    assertEquals(PASS, getExecutionResult("TestPage", new TestSummary(1, 0, 1, 0)));
	    assertEquals(FAIL, getExecutionResult("TestPage", new TestSummary(1, 1, 0, 0)));
	    assertEquals(FAIL, getExecutionResult("TestPage", new TestSummary(0, 1, 0, 0)));
	    assertEquals(FAIL, getExecutionResult("TestPage", new TestSummary(1, 1, 1, 0)));
	    assertEquals(FAIL, getExecutionResult("TestPage", new TestSummary(1, 1, 1, 1)));
	    assertEquals(ERROR, getExecutionResult("TestPage", new TestSummary(0, 0, 0, 1)));
	    assertEquals(ERROR, getExecutionResult("TestPage", new TestSummary(0, 0, 1, 1)));
	    assertEquals(IGNORE, getExecutionResult("TestPage", new TestSummary(0, 0, 0, 0)));
	    assertEquals(IGNORE, getExecutionResult("TestPage", new TestSummary(0, 0, 1, 0)));
	  }

	  @Test
	  public void summaryClassForSuiteMetaPages() throws Exception {
	    assertEquals(PASS, getExecutionResult("SuiteSetUp", new TestSummary(1, 0, 0, 0)));
	    assertEquals(PASS, getExecutionResult("SuiteSetUp", new TestSummary(1, 0, 1, 0)));
	    assertEquals(FAIL, getExecutionResult("SuiteSetUp", new TestSummary(1, 1, 0, 0)));
	    assertEquals(FAIL, getExecutionResult("SuiteSetUp", new TestSummary(0, 1, 0, 0)));
	    assertEquals(FAIL, getExecutionResult("SuiteSetUp", new TestSummary(1, 1, 1, 0)));
	    assertEquals(FAIL, getExecutionResult("SuiteSetUp", new TestSummary(1, 1, 1, 1)));
	    assertEquals(ERROR, getExecutionResult("SuiteSetUp", new TestSummary(0, 0, 0, 1)));
	    assertEquals(ERROR, getExecutionResult("SuiteSetUp", new TestSummary(0, 0, 1, 1)));
	    assertEquals(PASS, getExecutionResult("SuiteSetUp", new TestSummary(0, 0, 0, 0)));
	    assertEquals(IGNORE, getExecutionResult("SuiteSetUp", new TestSummary(0, 0, 1, 0)));
	  }

}
