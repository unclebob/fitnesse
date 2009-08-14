package fitnesse.slim;

public class JavaSlimFactory extends SlimFactory {

  private NameTranslator identityTranslator = new NameTranslatorIdentity();
  
  public StatementExecutorInterface getStatementExecutor() throws Exception {
    return new StatementExecutor();
  }

  @Override
  public NameTranslator getMethodNameTranslator() {
    return getIdentityTranslator();
  }
  
  private NameTranslator getIdentityTranslator() {
    return identityTranslator;
  }
}
