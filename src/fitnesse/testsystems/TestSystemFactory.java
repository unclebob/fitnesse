package fitnesse.testsystems;

/**
 * Creates new test systems
 */
public interface TestSystemFactory {

  /**
   * Create a test system given a descriptor.
   *
   *
   * @param descriptor Configuration for the test system.
   * @return a new TestSystem
   */
  TestSystem create(Descriptor descriptor);

}
