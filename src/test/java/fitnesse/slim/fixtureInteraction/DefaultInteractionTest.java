package fitnesse.slim.fixtureInteraction;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class DefaultInteractionTest {
  Class<Testee> testeeClass = Testee.class;
  Constructor<?> cstr = testeeClass.getConstructors()[0];
  Method setI;
  Method getI;

  public DefaultInteractionTest() throws Exception {
    setI = testeeClass.getMethod("setI", new Class[]{int.class});
    getI = testeeClass.getMethod("getI");
  }

  @Test
  public void canCreateAndUseATestObject() throws Exception {
    Integer expectedInt = new Integer(7);
    DefaultInteraction interaction = new DefaultInteraction();


    Testee o = (Testee) interaction.newInstance(cstr, (Object[]) null);
    interaction.methodInvoke(setI, o, expectedInt);
    Integer gotI = (Integer) interaction.methodInvoke(getI, o);

    assertEquals("should be able create an object, and use methods on a class", expectedInt, gotI);
  }

  @Test
  public void canUseMockingFramework() throws Exception {
    MockingInteraction interaction = new MockingInteraction();

    Testee testee = (Testee) interaction.newInstance(cstr, (Object[]) null);
    interaction.methodInvoke(setI, testee, new Integer(3));
    String gotI = (String) interaction.methodInvoke(getI, testee);

    String expectedMockingOnlyString = "----mockingOnly----";
    assertEquals("should be able create, and call setters and getters. These won't work", expectedMockingOnlyString, gotI);
  }
}
