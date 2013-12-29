package fitnesse.junit;

import org.junit.Test;
import static org.junit.Assert.*;

public class CommandBuilderTest {

  @Test
  public void getCommand_formatting(){
    assertEquals("test, no filter", "TestName?test&debug=true&nohistory=true&format=java", new CommandBuilder("TestName","test").build());
    assertEquals("suite, no filter", "SuiteName?suite&debug=true&nohistory=true&format=java", new CommandBuilder("SuiteName","suite").build());
    assertEquals("suite, with filter", "SuiteName?suite&debug=true&nohistory=true&format=java&suiteFilter=xxx", new CommandBuilder("SuiteName","suite").withSuiteFilter("xxx").build());
    assertEquals("suite, with excludefilter", "SuiteName?suite&debug=true&nohistory=true&format=java&excludeSuiteFilter=xxx", new CommandBuilder("SuiteName","suite").withExcludeSuiteFilter("xxx").build());
  }

  @Test
  public void getCommand_formatting_without_debug(){
    assertEquals("test, no filter", "TestName?test&nohistory=true&format=java", new CommandBuilder("TestName","test").withDebug(false).build());
    assertEquals("suite, no filter", "SuiteName?suite&nohistory=true&format=java", new CommandBuilder("SuiteName","suite").withDebug(false).build());
    assertEquals("suite, with filter", "SuiteName?suite&nohistory=true&format=java&suiteFilter=xxx", new CommandBuilder("SuiteName","suite").withSuiteFilter("xxx").withDebug(false).build());
    assertEquals("suite, with excludefilter", "SuiteName?suite&nohistory=true&format=java&excludeSuiteFilter=xxx", new CommandBuilder("SuiteName","suite").withExcludeSuiteFilter("xxx").withDebug(false).build());
  }
}
