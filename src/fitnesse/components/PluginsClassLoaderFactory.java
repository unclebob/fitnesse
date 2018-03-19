package fitnesse.components;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;


/**
 * Gets a class loader which extends the class path with jars found in "plugins" directory.
 */
public class PluginsClassLoaderFactory {

  public static ClassLoader getClassLoader(String rootPath) throws IOException {
    ClassLoader result = ClassLoader.getSystemClassLoader();
    File pluginsDirectory = new File(rootPath, "plugins");

    List<String> plugins = pluginJars(pluginsDirectory);
    if (!plugins.isEmpty()) {
      URL[] urls = urlsForPlugins(plugins);
      result = new URLClassLoader(urls, result);
      appendPluginsToClassPathProperty(plugins);
    }
    return result;
  }

  private static List<String> pluginJars(File pluginsDirectory) throws IOException {
    List<String> result = new ArrayList<>();
    if (pluginsDirectory.exists() && pluginsDirectory.isDirectory()) {
      for (File plugin : pluginsDirectory.listFiles()) {
        if (plugin.getName().endsWith("jar")) {
          result.add(plugin.getCanonicalPath());
        }
      }
    }
    return result;
  }

  private static URL[] urlsForPlugins(List<String> plugins) throws MalformedURLException {
    URL[] urls = new URL[plugins.size()];

    int i = 0;
    for (String plugin : plugins) {
      urls[i] = toUrl(plugin);
      i++;
    }

    return urls;
  }

  private static URL toUrl(String fileName) throws MalformedURLException {
    return new File(fileName).toURI().toURL();
  }

  private static void appendPluginsToClassPathProperty(List<String> plugins) {
    String currentClassPath = System.getProperty("java.class.path");

    StringBuilder classpathItems = new StringBuilder();
    classpathItems.append(currentClassPath);
    for (String plugin : plugins) {
      classpathItems.append(File.pathSeparator);
      classpathItems.append(plugin);
    }
    System.setProperty("java.class.path", classpathItems.toString());
  }
}
