package fitnesse.testsystems;

/**
 * This is all there is to a Descriptor really...
 */
public abstract class DescriptorBase {

  protected abstract String getCommandPattern();

  protected abstract String getTestRunner();

  protected abstract String getTestSystemName();


}
