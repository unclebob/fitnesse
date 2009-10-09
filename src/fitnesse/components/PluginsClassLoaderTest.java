package fitnesse.components;

import static org.junit.Assert.*;
import org.junit.Test;


public class PluginsClassLoaderTest {

    @Test
    public void whenPluginsDirectoryDoesNotExist() {
        try {
            PluginsClassLoader pluginsClassLoader = new PluginsClassLoader();
            pluginsClassLoader.pluginsDirectory = "nonExistingPluginsDirectory";
            pluginsClassLoader.addPluginsToClassLoader();
            assertTrue("didn't cause exception", true);
        } catch (Exception e) {
            fail("if exception occurs when plugins directory does not exist");
        }
    }

    @Test
    public void addPluginsToClassLoader() {
        String[] dynamicClasses = new String[]{"fitnesse.testing.PluginX", "fitnesse.testing.PluginY"};
        assertLoadingClassCausesException(dynamicClasses);
        new PluginsClassLoader().addPluginsToClassLoader();
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
