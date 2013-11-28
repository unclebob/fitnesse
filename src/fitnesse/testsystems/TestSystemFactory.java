package fitnesse.testsystems;

import java.io.IOException;

import fitnesse.FitNesseContext;

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
  TestSystem create(Descriptor descriptor) throws IOException;

}
