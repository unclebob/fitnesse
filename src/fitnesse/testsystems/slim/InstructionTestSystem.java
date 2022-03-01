package fitnesse.testsystems.slim;

import fitnesse.http.ResponseSender;
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

import java.io.IOException;
import java.util.List;

public class InstructionTestSystem implements TestSystem {

  public InstructionTestSystem(ResponseSender sender) {
    this.sender = sender;
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
    try {
      List<SlimTable> tables = SlimPage.Make(page, new SlimTestContextImpl(page), new SlimTableFactory(), new CustomComparatorRegistry()).getTables();
      for (SlimTable table: tables) {
        List<Instruction> instructions = SlimAssertion.getInstructions(table.getAssertions());
        String serial = SlimSerializer.serialize(new SlimListBuilder(Double.parseDouble(SlimVersion.VERSION)).toList(instructions));
        sender.sendLine(page.getFullPath() + "|" + serial);
      }
      listener.testComplete(page, new TestSummary());
    }
    catch (IOException e) {
      throw new TestExecutionException(e);
    }
  }

  @Override
  public boolean isSuccessfullyStarted() {
    return true;
  }

  @Override
  public void addTestSystemListener(TestSystemListener listener) {
    this.listener = listener;
  }

  private final ResponseSender sender;
  private TestSystemListener listener;
}
