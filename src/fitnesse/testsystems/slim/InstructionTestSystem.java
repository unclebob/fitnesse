package fitnesse.testsystems.slim;

import fitnesse.slim.SlimVersion;
import fitnesse.slim.instructions.Instruction;
import fitnesse.slim.protocol.SlimListBuilder;
import fitnesse.slim.protocol.SlimSerializer;
import fitnesse.testsystems.TestExecutionException;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSummary;
import fitnesse.testsystems.TestSystem;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.UnableToStartException;
import fitnesse.testsystems.UnableToStopException;
import fitnesse.testsystems.slim.tables.SlimAssertion;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;

import java.util.List;

public class InstructionTestSystem implements TestSystem {

  public InstructionTestSystem(StringBuilder result) {
    this.result = result;
  }

  @Override
  public String getName() {
    return "slim";
  }

  @Override
  public void start() throws UnableToStartException { }

  @Override
  public void bye() throws UnableToStopException { }

  @Override
  public void kill() { }

  @Override
  public void runTests(TestPage page) throws TestExecutionException {
    List<SlimTable> tables = SlimPage.Make(page, new SlimTestContextImpl(page), new SlimTableFactory(), new CustomComparatorRegistry()).getTables();
    for (SlimTable table: tables) {
      List<Instruction> instructions = SlimAssertion.getInstructions(table.getAssertions());
      String serial = SlimSerializer.serialize(new SlimListBuilder(Double.parseDouble(SlimVersion.VERSION)).toList(instructions));
      result.append(page.getFullPath()).append("|").append(encode(serial)).append(System.lineSeparator());
    }
    listener.testComplete(page, new TestSummary());
  }

  @Override
  public boolean isSuccessfullyStarted() {
    return true;
  }

  @Override
  public void addTestSystemListener(TestSystemListener listener) {
    this.listener = listener;
  }

  private String encode(String input) {
    return input
      .replace("%", "%23")
      .replace("\r", "%0A")
      .replace("\n", "%0D");
  }

  private final StringBuilder result;

  private TestSystemListener listener;
}
