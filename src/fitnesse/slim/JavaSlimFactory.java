package fitnesse.slim;

import fitnesse.slim.fixtureInteraction.DefaultInteraction;
import fitnesse.slim.fixtureInteraction.FixtureInteraction;

public class JavaSlimFactory extends SlimFactory {

  private final NameTranslator identityTranslator = new NameTranslatorIdentity();
  private final Integer timeout;
  private final boolean verbose;
  private final FixtureInteraction interaction;

  private JavaSlimFactory(FixtureInteraction interaction, Integer timeout, boolean verbose) {
    this.interaction = interaction;
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
  public NameTranslator getMethodNameTranslator() {
    return getIdentityTranslator();
  }

  @Override
  public boolean isVerbose() {
    return verbose;
  }

  private NameTranslator getIdentityTranslator() {
    return identityTranslator;
  }

  // Called from main
  public static SlimFactory createJavaSlimFactory(SlimService.Options options) {
    return createJavaSlimFactory(options.interaction, options.statementTimeout, options.verbose);
  }

  public static SlimFactory createJavaSlimFactory(FixtureInteraction interaction, Integer timeout, boolean verbose) {
    return new JavaSlimFactory(interaction, timeout, verbose);
  }

  // Only used in tests
  public static SlimFactory createJavaSlimFactory() {
    return createJavaSlimFactory(createInteraction(null), null, false);
  }

  @SuppressWarnings("unchecked")
  public static FixtureInteraction createInteraction(String interactionClassName) {
    if (interactionClassName == null) {
      return new DefaultInteraction();
    }
    try {
      return ((Class<FixtureInteraction>) Class.forName(interactionClassName)).newInstance();
    } catch (Exception e) {
      throw new SlimError(e);
    }
  }
}
