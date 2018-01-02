package fitnesse.components;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.logging.Level;
import java.util.logging.Logger;


public class PluginsClassLoader {
  private static final Logger LOG = Logger.getLogger(PluginsClassLoader.class.getName());
  private final static Method ADD_URL_METHOD;

  static {
    Method method = null;
    ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
    if (systemClassLoader instanceof URLClassLoader) {
      try {
        Class<URLClassLoader> sysClass = URLClassLoader.class;
        method = sysClass.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
      } catch (NoSuchMethodException e) {
        throw new IllegalStateException("Unable to find 'addURL' method in URLClassLoader", e);
      }
    }
    ADD_URL_METHOD = method;
  }

  private File pluginsDirectory;

  public PluginsClassLoader(String rootPath) {
    pluginsDirectory = new File(rootPath, "plugins");
  }

  public void addPluginsToClassLoader() throws Exception {
    boolean logged = false;
    if (pluginsDirectory.exists())
      for (File plugin : pluginsDirectory.listFiles())
        if (plugin.getName().endsWith("jar")) {
          if (ADD_URL_METHOD == null && !logged) {
            LOG.log(Level.WARNING,
              "Unable to add plugin to classpath. Are you running Java 9? "
                + "Please ensure you add the required plugins to the wiki's classpath through other means.");
            logged = true;
          }
          addItemsToClasspath(plugin.getCanonicalPath());
      }
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
    ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
    if (ADD_URL_METHOD != null && systemClassLoader instanceof URLClassLoader) {
      ADD_URL_METHOD.invoke(systemClassLoader, new Object[]{u});
    } else {
      LOG.log(Level.WARNING, "Unable to extend classpath to include plugin: " + u.getFile());
    }
  }
}
