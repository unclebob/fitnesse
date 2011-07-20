package fitnesse.junit;

import org.junit.Test;
import static org.junit.Assert.*;
public class TestHelperTest {

  @Test
  public void getCommand_formatting(){
    TestHelper th=new TestHelper("","");
    assertEquals("test, no filter", "TestName?test&debug=true&nohistory=true&format=java", th.getCommand("TestName","test",null, null));
    assertEquals("suite, no filter", "SuiteName?suite&debug=true&nohistory=true&format=java", th.getCommand("SuiteName","suite",null, null));
    assertEquals("suite, with filter", "SuiteName?suite&debug=true&nohistory=true&format=java&suiteFilter=xxx", th.getCommand("SuiteName","suite","xxx", null));
    assertEquals("suite, with excludefilter", "SuiteName?suite&debug=true&nohistory=true&format=java&excludeSuiteFilter=xxx", th.getCommand("SuiteName","suite",null, "xxx"));
  }

  @Test
  public void getCommand_formatting_without_debug(){
    TestHelper th=new TestHelper("","");
    th.setDebugMode(false);
    assertEquals("test, no filter", "TestName?test&nohistory=true&format=java", th.getCommand("TestName","test",null, null));
    assertEquals("suite, no filter", "SuiteName?suite&nohistory=true&format=java", th.getCommand("SuiteName","suite",null, null));
    assertEquals("suite, with filter", "SuiteName?suite&nohistory=true&format=java&suiteFilter=xxx", th.getCommand("SuiteName","suite","xxx", null));
    assertEquals("suite, with excludefilter", "SuiteName?suite&nohistory=true&format=java&excludeSuiteFilter=xxx", th.getCommand("SuiteName","suite", null, "xxx"));
  }
}
