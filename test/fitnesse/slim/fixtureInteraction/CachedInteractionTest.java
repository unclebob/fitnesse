package fitnesse.slim.fixtureInteraction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.tables.SlimTable;
import org.junit.Test;

import static org.junit.Assert.*;

public class CachedInteractionTest {
  private final CachedInteraction interaction = new CachedInteraction();

  @Test
  public void canFindClass() {
    Class<? extends CachedInteractionTest> myClass = this.getClass();

    Class<?> clazz = interaction.getClass(myClass.getName());

    assertEquals(myClass, clazz);
  }

  @Test
  public void canFindConstructor() throws NoSuchMethodException {
    Class<? extends CachedInteractionTest> myClass = this.getClass();

    Constructor<?> constructor = interaction.getConstructor(myClass, new Object[0]);
    assertEquals(myClass.getConstructor(), constructor);

    // call 2nd time
    constructor = interaction.getConstructor(myClass, new Object[0]);
    assertEquals(myClass.getConstructor(), constructor);

    Constructor<?> constructor2 = interaction.getConstructor(SlimTable.class, new Object[3]);

    assertEquals(SlimTable.class.getConstructor(Table.class, String.class, SlimTestContext.class), constructor2);
  }

  @Test
  public void canFindConstructorWithArgs() throws NoSuchMethodException {
    Constructor<?> constructor2 = interaction.getConstructor(SlimTable.class, new Object[3]);

    assertEquals(SlimTable.class.getConstructor(Table.class, String.class, SlimTestContext.class), constructor2);
  }

  @Test
  public void canDealWithNoConstructor() throws NoSuchMethodException {
    Constructor<?> constructor2 = interaction.getConstructor(SlimTable.class, new Object[0]);

    assertNull(constructor2);

    constructor2 = interaction.getConstructor(SlimTable.class, new Object[0]);

    assertNull(constructor2);
  }

  @Test
  public void canFindMethod() {
    String findMethod = "canFindMethod";

    Method method = interaction.findMatchingMethod(findMethod, getClass(), 0);

    assertEquals(findMethod, method.getName());
    assertEquals(getClass(), method.getDeclaringClass());

    //2nd call

    method = interaction.findMatchingMethod(findMethod, getClass(), 0);

    assertEquals(findMethod, method.getName());
    assertEquals(getClass(), method.getDeclaringClass());
  }

  @Test
  public void canFindMethodWithArguments() {
    String findMethod = "addChildTable";

    Method method = interaction.findMatchingMethod(findMethod, SlimTable.class, 2);

    assertEquals(findMethod, method.getName());
    assertEquals(SlimTable.class, method.getDeclaringClass());

    //2nd call

    method = interaction.findMatchingMethod(findMethod, SlimTable.class, 2);

    assertEquals(findMethod, method.getName());
    assertEquals(SlimTable.class, method.getDeclaringClass());
  }

  @Test
  public void canDealWithNoMethod() {
    String findMethod = "addChildTable";

    Method method = interaction.findMatchingMethod(findMethod, SlimTable.class, 3);
    assertNull(method);
  }
}
