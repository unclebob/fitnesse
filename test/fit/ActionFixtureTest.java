package fit;

import fit.exception.FitParseException;
import fit.testFxtr.TestActionFixture;
import org.junit.Before;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class ActionFixtureTest extends ActionFixture {
  private ActionFixture fixture;

  @Before
  public void setUp() throws Exception {
    fixture = new ActionFixture();
  }

  private Parse doTableOf(String rows) throws FitParseException {
    Parse table = new Parse(tableOf(rows));

    fixture.doRows(table.parts.more);
    return table;
  }

  private TestActionFixture actionFixture() {
    return (TestActionFixture) fixture.getActor();
  }

  @Test
  public void tryLoadMissingActionFixture() throws Exception {
    Parse table = doTableOf(row("start", "NoSuchFixture"));
    String s = getStringFor(table);
    assertTrue(s.contains("Could not find fixture: NoSuchFixture."));
  }

  @Test
  public void tryStartBlankActor() throws Exception {
    Parse table = doTableOf(row("start", ""));
    String s = getStringFor(table);
    assertTrue(s.contains("You must specify a fixture to start."));
  }

  @Test
  public void tryStartMissingActor() throws Exception {
    Parse table = doTableOf(row("start"));
    String s = getStringFor(table);
    assertTrue(s.contains("You must specify a fixture to start."));
  }

  @Test
  public void startRealActor() throws Exception {
    Parse table = doTableOf(row("start", "fit.testFxtr.TestActionFixture"));
    assertNotNull(fixture.getActor());
    assertTrue(fixture.getActor() instanceof TestActionFixture);
  }

  @Test
  public void tryWithoutActor() throws Exception {
    Parse table = doTableOf(
      row("check", "unadaptable"));
    String s = getStringFor(table);
    assertTrue(s.contains("You must start a fixture using the 'start' keyword."));
  }

  @Test
  public void checkIsCalled() throws Exception {
    Parse table = doTableOf(
      row("start", "fit.testFxtr.TestActionFixture") +
        row("check", "data", "42"));
    assertTrue(actionFixture().checked);
  }

  @Test
  public void tryCheckWithNoValue() throws Exception {
    Parse table = doTableOf(
      row("start", "fit.testFxtr.TestActionFixture") +
        row("check", "data"));
    String s = getStringFor(table);
    assertTrue(s.contains("You must specify a value to check."));
  }

  @Test
  public void tryCheckWithUnadaptableReturn() throws Exception {
    Parse table = doTableOf(
      row("start", "fit.testFxtr.TestActionFixture") +
        row("check", "unadaptable", "42"));
    String s = getStringFor(table);
    assertTrue(s.contains("Could not parse: 42"));
  }

  @Test
  public void tryCheckWithNoFunction() throws Exception {
    Parse table = doTableOf(
      row("start", "fit.testFxtr.TestActionFixture") +
        row("check"));
    String s = getStringFor(table);
    assertTrue(s.contains("You must specify a method."));
  }

  @Test
  public void tryCheckWithBlankFunction() throws Exception {
    Parse table = doTableOf(
      row("start", "fit.testFxtr.TestActionFixture") +
        row("check", ""));
    String s = getStringFor(table);
    assertTrue(s.contains("You must specify a method."));
  }

  @Test
  public void enterPassesCorrectData() throws Exception {
    Parse table = doTableOf(
      row("start", "fit.testFxtr.TestActionFixture") +
        row("enter", "data", "42"));
    assertEquals(42, actionFixture().entered);
  }

  @Test
  public void tryEnterWithNoArg() throws Exception {
    Parse table = doTableOf(
      row("start", "fit.testFxtr.TestActionFixture") +
        row("enter", "data"));
    String s = getStringFor(table);
    assertTrue(s.contains("You must specify an argument."));
  }

  @Test
  public void tryEnterWithUnparseableArg() throws Exception {
    Parse table = doTableOf(
      row("start", "fit.testFxtr.TestActionFixture") +
        row("enter", "data", "unparseable"));
    String s = getStringFor(table);
    assertTrue(s.contains("Could not parse: unparseable,"));
  }

  @Test
  public void tryEnterWithOverloadedFunction() throws Exception {
    Parse table = doTableOf(
      row("start", "fit.testFxtr.TestActionFixture") +
        row("enter", "overload", "1"));
    String s = getStringFor(table);
    assertTrue(s.contains("You can only have one overload(arg) method in your fixture."));
  }

  @Test
  public void tryEnterWithUndefinedFunction() throws Exception {
    Parse table = doTableOf(
      row("start", "fit.testFxtr.TestActionFixture") +
        row("enter", "undefined", "1"));
    String s = getStringFor(table);
    assertTrue(s.contains("Could not find method: undefined."));
  }

  @Test
  public void pressCallsRightFunction() throws Exception {
    Parse table = doTableOf(
      row("start", "fit.testFxtr.TestActionFixture") +
        row("press", "button"));

    assertTrue(actionFixture().buttonPressed);
  }

  @Test
  public void tryArbitraryFunction() throws Exception {
    Parse table = doTableOf(
      row("start", "fit.testFxtr.TestActionFixture") +
        row("arbitrary"));
    String s = getStringFor(table);
    assertTrue(s.contains("NoSuchMethodException"));
  }

  @Test
  public void tryBlankFunction() throws Exception {
    Parse table = doTableOf(
      row("start", "fit.testFxtr.TestActionFixture") +
        row(""));
    String s = getStringFor(table);
    assertTrue(s.contains("NoSuchMethodException"));
  }

  private String getStringFor(Parse table) {
    StringWriter stringWriter = new StringWriter();
    PrintWriter out = new PrintWriter(stringWriter);
    table.print(out);
    out.flush();
    out.close();
    return stringWriter.toString();
  }

  private String tableOf(String rows) {
    return "<table>" + "<tr> <td>Action Fixture</td> </tr>" + rows + "</table>";
  }

  private String row(String... cells) {
    String row = "<tr>";
    for (String cell : cells)
      row += "<td>" + cell + "</td>";
    row += "</tr>";
    return row;
  }
}
