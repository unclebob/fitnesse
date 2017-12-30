package fitnesse.components;

import org.junit.Test;

import static org.junit.Assert.*;


public class PluginsClassLoaderFactoryTest {

  @Test
  public void whenPluginsDirectoryDoesNotExist() throws Exception {
    new PluginsClassLoaderFactory().getClassLoader("nonExistingRootDirectory");

    assertTrue("didn't cause exception", true);
  }

  @Test
  public void addPluginsToClassLoader() throws Exception {
    ClassLoader cl = PluginsClassLoaderFactory.getClassLoader(".");

    assertLoadingClassWorksNow(cl, "fitnesse.testing.PluginX", "fitnesse.testing.PluginY");
  }

  private void assertLoadingClassWorksNow(ClassLoader cl, String... dynamicClasses) {
    for (String dynamicClass : dynamicClasses) {
      try {
        Class<?> dynamicallyLoadedClass = Class.forName(dynamicClass, true, cl);
        assertEquals(dynamicClass, dynamicallyLoadedClass.getName());
      } catch (ClassNotFoundException e) {
        fail("Class not found: " + e.getMessage());
      }
    }
  }
}
