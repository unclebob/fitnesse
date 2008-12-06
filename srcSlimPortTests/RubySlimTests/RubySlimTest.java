package fitnesse.slim;

import fitnesse.components.CommandRunner;
import org.junit.Test;

public class RubySlimTest extends SlimServiceTest {
  private CommandRunner runner;

  protected void createSlimService() throws Exception {
    runner = new CommandRunner("ruby /Users/unclebob/projects/RubySlim/lib/run_ruby_slim.rb 8099", "");
    runner.start();
  }

  protected void teardown() throws Exception {
    super.teardown();
    runner.join();
  }

  @Override
  protected String getImport() {
    return "TestModule";
  }

  @Test
  public void junk() throws Exception {

  }
}
