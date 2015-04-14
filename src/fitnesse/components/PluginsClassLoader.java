package fitnesse.components;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;


public class PluginsClassLoader {

  private File pluginsDirectory;

  public PluginsClassLoader(String rootPath) {
    pluginsDirectory = new File(rootPath, "plugins");
  }

  public void addPluginsToClassLoader() throws Exception {
    if (pluginsDirectory.exists())
      for (File plugin : pluginsDirectory.listFiles())
        if (plugin.getName().endsWith("jar"))
          addItemsToClasspath(plugin.getCanonicalPath());
  }

  public static void addItemsToClasspath(String classpathItems) throws Exception {
    final String separator = File.pathSeparator;
    String currentClassPath = System.getProperty("java.class.path");
    System.setProperty("java.class.path", currentClassPath + separator + classpathItems);
    String[] items = classpathItems.split(separator);
    for (String item : items) {
      addFileToClassPath(item);
    }
  }

  private static void addFileToClassPath(String fileName) throws Exception {
    addUrlToClasspath(new File(fileName).toURI().toURL());
  }

  public static void addUrlToClasspath(URL u) throws Exception {
    URLClassLoader sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class<URLClassLoader> sysClass = URLClassLoader.class;
    Method method = sysClass.getDeclaredMethod("addURL", new Class[]{URL.class});
    method.setAccessible(true);
    method.invoke(sysLoader, new Object[]{u});
  }

}
