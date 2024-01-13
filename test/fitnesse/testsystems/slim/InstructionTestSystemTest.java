package fitnesse.testsystems.slim;

import fitnesse.testsystems.ClassPath;
import fitnesse.testsystems.TestExecutionException;
import fitnesse.testsystems.TestPage;
import fitnesse.testsystems.TestSystemListener;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class InstructionTestSystemTest {
  @Test
  public void TablesAreParsed() {
    TestPage page = new MyTestPage();
    StringBuilder result = new StringBuilder();
    InstructionTestSystem testSystem = new InstructionTestSystem(result);
    testSystem.addTestSystemListener(new MyListener());
    try {
      testSystem.runTests(page);
      assertEquals(
        "TestPageFullPath|[000002:000087:[000004:000015:scriptTable_0_0:000004:make:000016:scriptTableActor:000011:SampleClass:]:000104:[000005:000015:scriptTable_0_1:000004:call:000016:scriptTableActor:000012:SomeProperty:000008:in%23put%0A%0D:]:]" + System.lineSeparator(),
        result.toString());
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
      return "TestPageFullPath";
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
      return "<table><tr><td>script</td><td>SampleClass</td></tr><tr><td>check</td><td>SomeProperty</td><td>in%put\r\n</td><td>valid</td></tr></table>";
    }
  }

  static class MyListener implements TestSystemListener {  }
}
