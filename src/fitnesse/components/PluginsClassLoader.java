package fitnesse.components;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;


/**
 * Update the current thread class path with jars foundin a "plugins" directory.
 */
public class PluginsClassLoader {

  public static ClassLoader loadPlugins(String rootPath) throws Exception {
    File pluginsDirectory = new File(rootPath, "plugins");

    URL[] urls = urlsForPlugins(pluginsDirectory);
    if (urls.length > 0) {
      return new URLClassLoader(urls, ClassLoader.getSystemClassLoader());
    } else {
      return ClassLoader.getSystemClassLoader();
    }
  }

  private static URL[] urlsForPlugins(File pluginsDirectory) throws Exception {
    List<URL> urls = new ArrayList<>();

    if (pluginsDirectory.exists() && pluginsDirectory.isDirectory())
      for (File plugin : pluginsDirectory.listFiles())
        if (plugin.getName().endsWith("jar"))
          urls.addAll(toUrls(plugin.getCanonicalPath()));

    return urls.toArray(new URL[urls.size()]);
  }

  private static List<URL> toUrls(String classpathItems) throws Exception {
    final String separator = File.pathSeparator;
    String currentClassPath = System.getProperty("java.class.path");
    System.setProperty("java.class.path", currentClassPath + separator + classpathItems);
    String[] items = classpathItems.split(separator);
    List<URL> urls = new ArrayList<>(items.length);

    for (String item : items) {
      urls.add(toUrl(item));
    }
    return urls;
  }

  private static URL toUrl(String fileName) throws MalformedURLException {
    return new File(fileName).toURI().toURL();
  }
}
