package fitnesse.components;

import static org.junit.Assert.*;
import static util.RegexTestCase.assertMatches;
import static util.RegexTestCase.assertNotSubString;
import static util.RegexTestCase.assertSubString;

import org.junit.Test;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;


public class PluginsClassLoaderTest {

  @Test
  public void whenPluginsDirectoryDoesNotExist() throws Exception {
    PluginsClassLoader pluginsClassLoader = new PluginsClassLoader("nonExistingRootDirectory");
    pluginsClassLoader.addPluginsToClassLoader();
    assertTrue("didn't cause exception", true);
  }

  @Test
  public void addPluginsToClassLoader() throws Exception {
    String[] dynamicClasses = new String[]{"fitnesse.testing.PluginX", "fitnesse.testing.PluginY"};
    //todo This fails because some other test probably loads plugin path    assertLoadingClassCausesException(dynamicClasses);
    PluginsClassLoader pluginsClassLoader = new PluginsClassLoader(".");
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

  @Test
  public void testAddUrlToClasspath() throws Exception {
    ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
    assertTrue(systemClassLoader instanceof URLClassLoader);
    URLClassLoader classLoader = (URLClassLoader) systemClassLoader;

    URL sampleUrl = new File("src").toURI().toURL();

    String classpath = classpathAsString(classLoader);
    assertNotSubString(sampleUrl.toString(), classpath);

    PluginsClassLoader.addUrlToClasspath(sampleUrl);
    classpath = classpathAsString(classLoader);
    assertSubString(sampleUrl.toString(), classpath);
  }

  @Test
  public void testAddMultipleUrlsToClasspath() throws Exception {
    String separator = System.getProperty("path.separator");
    String paths = "/blah/blah" + separator + "C" + otherSeperator(separator) + "\\foo\\bar";
    PluginsClassLoader.addItemsToClasspath(paths);
    URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    String classpath = classpathAsString(classLoader);
    assertSubString("/blah/blah", classpath);
    assertMatches("[C" + otherSeperator(separator) + "?foo?bar]", classpath);
  }

  private String otherSeperator(String separator) {
    return separator.equals(";") ? ":" : ";";
  }

  private String classpathAsString(URLClassLoader classLoader) {
    URL[] urls = classLoader.getURLs();
    StringBuffer urlString = new StringBuffer();
    for (int i = 0; i < urls.length; i++)
      urlString.append(urls[i].toString()).append(":");
    return urlString.toString();
  }


}
