package fitnesse.slim.fixtureInteraction;

import java.lang.reflect.Method;

/**
 * Interface to allow aspect-oriented behavior to be added to fixtures. These fixtures are explicitly aware
 * of Slim, and are allowed to have control of how Slim interacts with their methods.
 * Intended uses are, for instance: generic exception handling allowing exceptions from any of its methods to
 * be converted to SlimExceptions (without having to implement that in each method invoked), or waiting for the system
 * under test to be in a stable state before invoking the next method (again without having to add that waiting to each
 * method).
 */
public interface InteractionAwareFixture {
  /**
   * Any time Slim wants to invoke a method on the fixture it will invoke this method (instead of the actual method).
   * Implementations are free/responsible to deal with the invocation as they see fit.
   * To invoke the 'normal' or 'real' method, an implementation should invoke interaction#methodInvoke().
   * @param interaction the standard Slim wrapper to call a method (an implementation wanting to have the 'normal' Slim
   *                    invocation executed can call methodInvoke() on this object, with <code>this</code> as instance).
   * @param method the method being invoked.
   * @param arguments the arguments to the method.
   * @return the result of the method.
   * @throws Throwable exception thrown by the method (may be wrapped in an InvocationTargetException,
   * which will be stripped).
   */
  Object aroundSlimInvoke(FixtureInteraction interaction, Method method, Object... arguments) throws Throwable;
}
