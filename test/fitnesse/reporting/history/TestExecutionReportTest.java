package fitnesse.reporting.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.List;

import fitnesse.FitNesseVersion;
import org.junit.Before;
import org.junit.Test;

import fitnesse.FitNesseContext;
import fitnesse.reporting.history.TestExecutionReport.Expectation;
import fitnesse.reporting.history.TestExecutionReport.InstructionResult;
import fitnesse.reporting.history.TestExecutionReport.TestResult;
import fitnesse.testsystems.TestSummary;
import fitnesse.testutil.FitNesseUtil;

public class TestExecutionReportTest {
  private TestExecutionReport expected;
  private FitNesseContext context;
  private TestExecutionReport actual;
  private TestResult result;

  @Before
  public void setup() throws Exception {
    expected = new TestExecutionReport(new FitNesseVersion("version"), "rootPath");
    expected.getFinalCounts().add(new TestSummary(1, 2, 3, 4));
    context = FitNesseUtil.makeTestContext();
  }

  private String reportToXml(TestExecutionReport report) throws Exception {
    StringWriter writer = new StringWriter();
    report.toXml(writer, context.pageFactory.getVelocityEngine());
    writer.close();
    return writer.toString();
  }

  private void serializeAndDeserialize() throws Exception {
    String xmlReport = reportToXml(expected);
    actual = new TestExecutionReport(new ByteArrayInputStream(xmlReport.getBytes()));
  }

  private void addDummyResult() {
    result = new TestResult();
    result.content = "content";
    result.right = "1";
    result.wrong = "2";
    result.ignores = "3";
    result.exceptions = "4";
    result.relativePageName = "relativePageName";
    result.tags = "tags";
    result.dateString = "2015-12-07T17:12:11+01:00";
    result.runTimeInMillis = "234";
    expected.addResult(result);
  }

  @Test
  public void headerCanBeSerializedAndDeserialized() throws Exception {
    serializeAndDeserialize();
    assertEquals(expected.getVersion(), actual.getVersion());
    assertEquals(expected.getFinalCounts(), actual.getFinalCounts());
    assertEquals(expected.getRootPath(), actual.getRootPath());
  }

  @Test
  public void simpleResultCanBeSerializedAndDeserialized() throws Exception {
    addDummyResult();
    serializeAndDeserialize();
    assertEquals(1, actual.getResults().size());
    assertEquals("234", actual.getResults().get(0).runTimeInMillis);
  }

  @Test
  public void getAssertions() throws Exception {
    addDummyResult();
    assertEquals(expected.getAssertionCounts(), new TestSummary(1, 2, 3, 4));
  }

  @Test
  public void instructionsShouldBeDeserialized() throws Exception {
    addDummyResult();
    addInstructionsToResult();
    serializeAndDeserialize();
    List<InstructionResult> instructions = actual.getResults().get(0).instructions;
    assertEquals(2, instructions.size());
    InstructionResult ir1 = instructions.get(0);
    InstructionResult ir2 = instructions.get(1);

    assertEquals("instruction1", ir1.instruction);
    assertEquals("slimResult1", ir1.slimResult);
    List<Expectation> expectations1 = ir1.getExpectations();
    assertEquals(2, expectations1.size());
    Expectation e11 = expectations1.get(0);
    Expectation e12 = expectations1.get(1);
    assertEquals("s1", e11.status);
    assertEquals("id1", e11.instructionId);
    assertEquals("c1", e11.col);
    assertEquals("r1", e11.row);
    assertEquals("t1", e11.type);
    assertEquals("a1", e11.actual);
    assertEquals("e1", e11.expected);
    assertEquals("m1", e11.evaluationMessage);

    assertEquals("s2", e12.status);
    assertEquals("id2", e12.instructionId);
    assertEquals("c2", e12.col);
    assertEquals("r2", e12.row);
    assertEquals("t2", e12.type);
    assertNull(e12.actual);
    assertNull(e12.expected);
    assertNull(e12.evaluationMessage);

    assertEquals("instruction2", ir2.instruction);
    assertEquals("slimResult2", ir2.slimResult);
    assertEquals(0, ir2.getExpectations().size());
  }

  private void addInstructionsToResult() {
    InstructionResult instruction1 = new InstructionResult();
    instruction1.instruction = "instruction1";
    instruction1.slimResult = "slimResult1";

    Expectation e1 = new Expectation();
    instruction1.addExpectation(e1);
    e1.status = "s1";
    e1.instructionId = "id1";
    e1.col = "c1";
    e1.row = "r1";
    e1.type = "t1";
    e1.actual = "a1";
    e1.expected = "e1";
    e1.evaluationMessage = "m1";

    Expectation e2 = new Expectation();
    instruction1.addExpectation(e2);
    e2.status = "s2";
    e2.instructionId = "id2";
    e2.col = "c2";
    e2.row = "r2";
    e2.type = "t2";
    result.instructions.add(instruction1);

    InstructionResult instruction2 = new InstructionResult();
    instruction2.instruction = "instruction2";
    instruction2.slimResult = "slimResult2";
    result.instructions.add(instruction2);
  }

}
