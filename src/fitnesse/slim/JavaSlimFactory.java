package fitnesse.slim;

public class JavaSlimFactory extends SlimFactory {

  private NameTranslator identityTranslator = new NameTranslatorIdentity();
  private Integer timeout;

  private JavaSlimFactory(Integer timeout) {
    this.timeout = timeout;
  }

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

  private NameTranslator getIdentityTranslator() {
    return identityTranslator;
  }

  public static SlimFactory createJavaSlimFactory(SlimService.Options options) {
    return new JavaSlimFactory(options.statementTimeout);
  }

  public static SlimFactory createJavaSlimFactory() {
    return new JavaSlimFactory(null);
  }
}
