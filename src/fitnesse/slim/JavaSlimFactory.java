package fitnesse.slim;

import fitnesse.slim.fixtureInteraction.DefaultInteraction;
import fitnesse.slim.fixtureInteraction.FixtureInteraction;

public class JavaSlimFactory extends SlimFactory {

  private final NameTranslator nameTranslator;
  private final Integer timeout;
  private final boolean verbose;
  private final FixtureInteraction interaction;

  private JavaSlimFactory(FixtureInteraction interaction, NameTranslator nameTranslator, Integer timeout, boolean verbose) {
    this.interaction = interaction;
    this.nameTranslator = nameTranslator;
    this.timeout = timeout;
    this.verbose = verbose;
  }

  @Override
  public StatementExecutorInterface getStatementExecutor() {
    StatementExecutorInterface statementExecutor = new StatementExecutor(new SlimExecutionContext(interaction));
    if (timeout != null) {
      statementExecutor = StatementTimeoutExecutor.decorate(statementExecutor, timeout);
    }
    return statementExecutor;
  }

  @Override
  public NameTranslator getNameTranslator() {
    return nameTranslator;
  }

  @Override
  public boolean isVerbose() {
    return verbose;
  }

  // Called from main
  public static SlimFactory createJavaSlimFactory(SlimService.Options options) {
    return createJavaSlimFactory(options.interaction, options.nameTranslator, options.statementTimeout, options.verbose);
  }

  public static SlimFactory createJavaSlimFactory(FixtureInteraction interaction, NameTranslator nameTranslator, Integer timeout, boolean verbose) {
    return new JavaSlimFactory(interaction, nameTranslator, timeout, verbose);
  }

  // Only used in tests
  public static SlimFactory createJavaSlimFactory() {
    return new JavaSlimFactory(new DefaultInteraction(), new NameTranslatorIdentity(), null, false);
  }
}
