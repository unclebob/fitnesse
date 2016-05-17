package fitnesse.slim;

public class JavaSlimFactory extends SlimFactory {

  private final NameTranslator identityTranslator = new NameTranslatorIdentity();
  private final Integer timeout;
  private final boolean verbose;

  private JavaSlimFactory(Integer timeout, boolean verbose) {
    this.timeout = timeout;
    this.verbose = verbose;
  }

  @Override
  public StatementExecutorInterface getStatementExecutor() {
    StatementExecutorInterface statementExecutor = new StatementExecutor();
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

  public static SlimFactory createJavaSlimFactory(SlimService.Options options) {
    return createJavaSlimFactory(options.statementTimeout, options.verbose);
  }

  public static SlimFactory createJavaSlimFactory(Integer timeout, boolean verbose) {
    return new JavaSlimFactory(timeout, verbose);
  }

  public static SlimFactory createJavaSlimFactory() {
    return new JavaSlimFactory(null, false);
  }
}
