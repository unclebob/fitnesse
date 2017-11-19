package fitnesse.slim.fixtureInteraction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Test;

/*
 * Note regarding  <Object args[] > creation: 
 *  - the client fixture call will send to the server String values
 */
public class DefaultInteractionTest {

  Class<Testee> testeeClass = Testee.class;
  Constructor<?> cstr = testeeClass.getConstructor(Integer.TYPE);
  Method setI;
  Method getI;

  public DefaultInteractionTest() throws Exception {
    setI = testeeClass.getMethod("setI", new Class[]{int.class});
    getI = testeeClass.getMethod("getI");
  }

  @Test
  public void canExecuteConstructorWhenIntAndDateArgType() throws Throwable {
    //given 
    DefaultInteraction defaultInteraction = new DefaultInteraction();
    Object args[] = new Object[]{"1", "stringVal", "10-Dec-1981"};
    Constructor<?> constructor = defaultInteraction.getConstructor(Testee.class, args);
    //when
    Object convertedArgs[] = defaultInteraction.getConvertedConstructorArgsTypes(constructor, args);
    Testee testee = (Testee) constructor.newInstance(convertedArgs);
    //then 
    assertEquals(new Integer(1), testee.getIntWrapperVal());
    assertEquals("stringVal", testee.getStringVal());
    assertEquals(convertedArgs[2], testee.getDateVal());
  }
  
  
  @Test
  public void canExecuteConstructorWithDateArgTypePriorityOverString() throws Throwable {
    //given 
    DefaultInteraction defaultInteraction = new DefaultInteraction();
    Object args[] = new Object[]{"10-Dec-1981"};
    Constructor<?> constructor = defaultInteraction.getConstructor(Testee.class, args);
    //when
    Object convertedArgs[] = defaultInteraction.getConvertedConstructorArgsTypes(constructor, args);
    Testee testee = (Testee) constructor.newInstance(convertedArgs);
    //then 
    assertNull(testee.getStringVal());
    assertEquals(convertedArgs[0], testee.getDateVal());
  }

  @Test
  public void canExecuteConstructorWhenDoubleArgType() throws Throwable {
    //given 
    DefaultInteraction defaultInteraction = new DefaultInteraction();
    Object args[] = new Object[]{"1", "2.0d"};
    Constructor<?> constructor = defaultInteraction.getConstructor(Testee.class, args);
    //when
    Object convertedArgs[] = defaultInteraction.getConvertedConstructorArgsTypes(constructor, args);
    Testee testee = (Testee) constructor.newInstance(convertedArgs);
    //then 
    assertEquals(2.0d, testee.getDoubleVal(), 0.0d);
    assertEquals(1, testee.getI());
  }

  @Test
  public void canExecuteConstructorWhenFloatArgType() throws Throwable {
    //given 
    DefaultInteraction defaultInteraction = new DefaultInteraction();
    Object args[] = new Object[]{"1", "2.0f"};
    Constructor<?> constructor = defaultInteraction.getConstructor(Testee.class, args);
    //when
    Object convertedArgs[] = defaultInteraction.getConvertedConstructorArgsTypes(constructor, args);
    Testee testee = (Testee) constructor.newInstance(convertedArgs);
    //then 
    // we have support only for Double convertor in FitNesse
    assertEquals(2.0d, testee.getDoubleVal(), 0.0d);
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
