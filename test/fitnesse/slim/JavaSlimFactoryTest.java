package fitnesse.slim;

import fitnesse.slim.fixtureInteraction.DefaultInteraction;
import org.junit.Test;

import static fitnesse.slim.JavaSlimFactory.*;
import static org.junit.Assert.assertTrue;

public class JavaSlimFactoryTest {

  @Test
  public void slimFactoryWithNoStatementTimeout() {
    SlimFactory factory = createJavaSlimFactory(createInteraction(null), null, false);
    StatementExecutorInterface executor = factory.getStatementExecutor();
    assertTrue(executor instanceof StatementExecutor);
  }

  @Test
  public void slimFactoryWithStatementTimeout() {
    SlimFactory factory = createJavaSlimFactory(optionsWithStatementTimeout());
    StatementExecutorInterface executor = factory.getStatementExecutor();
    assertTrue(executor instanceof StatementTimeoutExecutor);
  }

  private SlimService.Options optionsWithStatementTimeout() {
    return new SlimService.Options(false, 8099, new DefaultInteraction(), false, 1000, false, null);
  }

}
