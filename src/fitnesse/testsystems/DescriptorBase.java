package fitnesse.testsystems;

/**
 * This is all there is to a Descriptor really...
 */
public abstract class DescriptorBase {

  protected abstract String getCommandPattern();

  protected abstract String getTestRunner();

  protected abstract String getTestSystemName();

  @Override
  public int hashCode() {
    return getTestSystemName().hashCode() ^ getTestRunner().hashCode() ^ getCommandPattern().hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;

    Descriptor descriptor = (Descriptor) obj;
    return descriptor.getTestSystemName().equals(getTestSystemName()) &&
            descriptor.getTestRunner().equals(getTestRunner()) &&
            descriptor.getCommandPattern().equals(getCommandPattern());
  }

}
