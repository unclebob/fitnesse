package fitnesse.testsystems;

import java.io.IOException;

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
   * @throws IOException IOException thrown
   */
  TestSystem create(Descriptor descriptor) throws IOException;

}
