package fitnesse.testsystems.slim;

import fitnesse.slim.SlimVersion;
import fitnesse.slim.instructions.Instruction;
import fitnesse.slim.protocol.SlimListBuilder;
import fitnesse.slim.protocol.SlimSerializer;
import fitnesse.testsystems.ClassPath;
import fitnesse.testsystems.TestExecutionException;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.slim.tables.SlimAssertion;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class SlimPageTest {
  @Test
  public void TablesAreParsed() {
    TestPage page = new MyTestPage();
    List<SlimTable> result = SlimPage.Make(page, new SlimTestContextImpl(page), new SlimTableFactory(), new CustomComparatorRegistry()).getTables();
    try {
      List<Instruction> instructions = SlimAssertion.getInstructions(result.get(0).getAssertions());
      String serial = SlimSerializer.serialize(new SlimListBuilder(Double.parseDouble(SlimVersion.VERSION)).toList(instructions));
      assertEquals(
        "[000002:000087:[000004:000015:scriptTable_0_0:000004:make:000016:scriptTableActor:000011:SampleClass:]:000088:[000004:000015:scriptTable_0_1:000004:call:000016:scriptTableActor:000012:SomeProperty:]:]",
        serial);
    } catch (TestExecutionException e) {
      e.printStackTrace();
    }
  }

  static class MyTestPage implements TestPage {
    @Override
    public String getName() {
      return null;
    }

    @Override
    public String getFullPath() {
      return null;
    }

    @Override
    public String getVariable(String name) {
      return null;
    }

    @Override
    public ClassPath getClassPath() {
      return null;
    }

    @Override
    public String getContent() {
      return null;
    }

    @Override
    public String getHtml() {
      return "<table><tr><td>script</td><td>SampleClass</td></tr><tr><td>check</td><td>SomeProperty</td><td>valid</td></tr></table>";
    }
  }
}
