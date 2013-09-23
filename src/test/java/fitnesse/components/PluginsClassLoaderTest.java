package fitnesse.components;

import static org.junit.Assert.*;
import org.junit.Test;

import java.io.File;


public class PluginsClassLoaderTest {

  @Test
  public void whenPluginsDirectoryDoesNotExist() {
    try {
      PluginsClassLoader pluginsClassLoader = new PluginsClassLoader();
      pluginsClassLoader.pluginsDirectory = "nonExistingPluginsDirectory";
      assertFalse(new File(pluginsClassLoader.pluginsDirectory).exists());
      pluginsClassLoader.addPluginsToClassLoader();
      assertTrue("didn't cause exception", true);
    } catch (Exception e) {
      fail("if exception occurs when plugins directory does not exist");
    }
  }

  @Test
  public void addPluginsToClassLoader() throws Exception {
    String[] dynamicClasses = new String[]{"fitnesse.testing.PluginX", "fitnesse.testing.PluginY"};
//todo This fails because some other test probably loads plugin path    assertLoadingClassCausesException(dynamicClasses);
    PluginsClassLoader pluginsClassLoader = new PluginsClassLoader();
    assertTrue(new File(pluginsClassLoader.pluginsDirectory).exists());
    pluginsClassLoader.addPluginsToClassLoader();
    assertLoadingClassWorksNow(dynamicClasses);
  }

  private void assertLoadingClassWorksNow(String... dynamicClasses) {
    for (String dynamicClass : dynamicClasses) {
      try {
        Class<?> dynamicallyLoadedClass = Class.forName(dynamicClass);
        assertEquals(dynamicClass, dynamicallyLoadedClass.getName());
      } catch (ClassNotFoundException e) {
        fail(e.getMessage());
      }
    }
  }

  private void assertLoadingClassCausesException(String... dynamicClasses) {
    for (String dynamicClass : dynamicClasses) {
      try {
        Class.forName(dynamicClass);
        fail("plugins are not yet added to the classloader");
      } catch (ClassNotFoundException e) {
        assertEquals(dynamicClass, e.getMessage());
      }
    }
  }

}
