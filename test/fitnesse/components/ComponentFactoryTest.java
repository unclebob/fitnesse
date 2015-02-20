package fitnesse.components;

import java.util.Properties;

import org.junit.Test;

import static org.junit.Assert.*;

public class ComponentFactoryTest {

  @Test
  public void shouldInstantiateComponentsOnce() {
    ComponentFactory componentFactory = new ComponentFactory(new Properties());

    Object component1 = componentFactory.createComponent("SomePropertyName", TheComponent.class);
    Object component2 = componentFactory.createComponent("SomePropertyName", TheComponent.class);

    assertSame(component1, component2);
  }

  public static class TheComponent {

  }
}