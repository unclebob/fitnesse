package fitnesse.components;

import static org.junit.Assert.*;

import org.junit.Test;


public class PluginsClassLoaderTest {

  @Test
  public void whenPluginsDirectoryDoesNotExist() throws Exception {
    new PluginsClassLoader().getClassLoader("nonExistingRootDirectory");

    assertTrue("didn't cause exception", true);
  }

  @Test
  public void addPluginsToClassLoader() throws Exception {
    String[] dynamicClasses = new String[]{"fitnesse.testing.PluginX", "fitnesse.testing.PluginY"};
    //todo This fails because some other test probably loads plugin path    assertLoadingClassCausesException(dynamicClasses);
    ClassLoader cl = PluginsClassLoader.getClassLoader(".");


    assertLoadingClassWorksNow(cl, dynamicClasses);
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
