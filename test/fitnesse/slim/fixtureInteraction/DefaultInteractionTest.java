package fitnesse.slim.fixtureInteraction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Date;
import junit.framework.Assert;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

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
  public void canExecuteConstructorWhenIntArgType() throws Throwable {
    //given 
    DefaultInteraction defaultInteraction = new DefaultInteraction();
    Constructor<?> constructor = null;
    Date dateVal = new Date();
    Object args[] = new Object[]{1, "stringVal", dateVal};
    //when 
    constructor = defaultInteraction.getConstructor(Testee.class, args);
    Testee testee = (Testee) constructor.newInstance(args);
    //then 
    assertEquals(1, testee.getI());
    assertEquals("stringVal", testee.getStringVal());
    assertEquals(dateVal, testee.getDateVal());
  }

  @Test
  public void canExecuteConstructorWhenDoubleArgType() throws Throwable {
    //given 
    DefaultInteraction defaultInteraction = new DefaultInteraction();
    Constructor<?> constructor = null;
    Object args[] = new Object[]{1, 2.0d};
    //when 
    constructor = defaultInteraction.getConstructor(Testee.class, args);
    Testee testee = (Testee) constructor.newInstance(args);
    //then 
    assertEquals(2.0d, testee.getDoubleVal(), 0.0d);
    assertEquals(1, testee.getI());
  }

  @Test
  public void canExecuteConstructorWhenFloatArgType() throws Throwable {
    //given 
    DefaultInteraction defaultInteraction = new DefaultInteraction();
    Constructor<?> constructor = null;
    Object args[] = new Object[]{1, 2.0f};
    //when 
    constructor = defaultInteraction.getConstructor(Testee.class, args);
    Testee testee = (Testee) constructor.newInstance(args);
    //then 
    Assert.assertEquals(2.0f, testee.getFloatVal(), 0.0f);
    assertEquals(1, testee.getI());
  }

  @Test
  public void canCreateAndUseATestObject() throws Throwable {
    Integer expectedInt = new Integer(7);
    DefaultInteraction interaction = new DefaultInteraction();

    Testee o = (Testee) interaction.newInstance(cstr, new Object[]{1});
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
