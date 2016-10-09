package fitnesse.slim.fixtureInteraction;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collections;

import fitnesse.slim.test.TableTableIncFirstCol;
import fitnesse.testsystems.slim.SlimTestContext;
import fitnesse.testsystems.slim.Table;
import fitnesse.testsystems.slim.tables.SlimTable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CachedInteractionTest {
  @Spy
  private CachedInteraction interaction;

  @Test
  public void canFindClass() {
    Class<? extends CachedInteractionTest> myClass = this.getClass();

    Class<?> clazz = interaction.getClass(myClass.getName());

    assertEquals(myClass, clazz);
    verify(interaction, times(1)).handleClassCacheMiss(myClass.getName());

    // 2nd call
    clazz = interaction.getClass(myClass.getName());

    assertEquals(myClass, clazz);
    // cache hit on 2nd call
    verify(interaction, times(1)).handleClassCacheMiss(myClass.getName());
  }

  @Test
  public void canDealWithNoClassFound() {
    String className = "IDontExist";
    Class<?> clazz = interaction.getClass(className);

    assertNull(clazz);

    // 2nd call
    clazz = interaction.getClass(className);

    assertNull(clazz);
    // cache hit on 2nd call
    verify(interaction, times(1)).handleClassCacheMiss(className);
  }

  @Test
  public void canFindConstructor() throws NoSuchMethodException {
    Class<? extends CachedInteractionTest> myClass = this.getClass();

    Object[] args = new Object[0];
    Constructor<?> constructor = interaction.getConstructor(myClass, args);
    assertEquals(myClass.getConstructor(), constructor);
    verify(interaction, times(1)).handleConstructorCacheMiss(myClass, args);

    // call 2nd time
    constructor = interaction.getConstructor(myClass, args);
    assertEquals(myClass.getConstructor(), constructor);
    // cache hit on 2nd call
    verify(interaction, times(1)).handleConstructorCacheMiss(myClass, args);

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

    Method method = interaction.findMatchingMethod(findMethod, this);

    assertEquals(findMethod, method.getName());
    assertEquals(getClass(), method.getDeclaringClass());
    verify(interaction, times(1)).handleMethodCacheMiss(findMethod, this, new Object[0]);

    //2nd call

    method = interaction.findMatchingMethod(findMethod, this);

    assertEquals(findMethod, method.getName());
    assertEquals(getClass(), method.getDeclaringClass());
    // cache hit, no 2nd call
    verify(interaction, times(1)).handleMethodCacheMiss(findMethod, this, new Object[0]);
  }

  @Test
  public void canFindMethodWithArguments() {
    String findMethod = "createInstance";
    DefaultInteraction instance = new DefaultInteraction();

    Method method = interaction.findMatchingMethod(findMethod, instance, null, null, null);

    assertEquals(findMethod, method.getName());
    assertEquals(DefaultInteraction.class, method.getDeclaringClass());
    verify(interaction, times(1)).handleMethodCacheMiss(findMethod, instance, new Object[3]);

    //2nd call
    method = interaction.findMatchingMethod(findMethod, instance, null, null, null);

    assertEquals(findMethod, method.getName());
    assertEquals(DefaultInteraction.class, method.getDeclaringClass());
    // cache hit, no 2nd call
    verify(interaction, times(1)).handleMethodCacheMiss(findMethod, instance, new Object[3]);
  }

  @Test
  public void canFindMethodWithSameSimpleClassName() {
    String findMethod = "doTable";
    TableTableIncFirstCol instance = new TableTableIncFirstCol();
    fitnesse.slim.test.statementexecutorconsumer.TableTableIncFirstCol consInstance =
      new fitnesse.slim.test.statementexecutorconsumer.TableTableIncFirstCol();

    Method method = interaction.findMatchingMethod(findMethod, instance, Collections.emptyList());

    assertEquals(findMethod, method.getName());
    assertEquals("Method returned is defined by the wrong class (i.e. not the class of the instance passed)",
      instance.getClass(), method.getDeclaringClass());

    //2nd call
    Method consMethod = interaction.findMatchingMethod(findMethod, consInstance, Collections.emptyList());

    assertEquals(findMethod, consMethod.getName());
    assertEquals("Method returned is defined by the wrong class (i.e. not the class of the instance passed)",
      consInstance.getClass(), consMethod.getDeclaringClass());
  }

  @Test
  public void canDealWithNoMethod() {
    String findMethod = "addChildTable";

    Method method = interaction.findMatchingMethod(findMethod, SlimTable.class, 3);
    assertNull(method);
    verify(interaction, times(1)).handleMethodCacheMiss(findMethod, SlimTable.class, new Integer[] { 3 });

    // 2nd call cache hit
    method = interaction.findMatchingMethod(findMethod, SlimTable.class, 3);
    assertNull(method);
    verify(interaction, times(1)).handleMethodCacheMiss(findMethod, SlimTable.class, new Integer[] { 3 });
  }
}
