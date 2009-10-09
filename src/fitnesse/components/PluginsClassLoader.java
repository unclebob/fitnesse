package fitnesse.components;

import java.io.File;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;


public class PluginsClassLoader {

  protected String pluginsDirectory = "plugins";

  public void addPluginsToClassLoader() {
    File pluginsDir = new File(pluginsDirectory);
    if (pluginsDir.exists()) {
      for (File plugin : pluginsDir.listFiles()) {
        if (plugin.getName().endsWith("jar")) {
          addFile(plugin);
        }
      }
    }
  }

  public void addFile(File plugin) {
    try {
      addURL(plugin.toURI().toURL());
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  public void addURL(URL url) {
    URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class sysclass = URLClassLoader.class;
    try {
      Method method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
      method.setAccessible(true);
      method.invoke(sysloader, url);
    } catch (Exception e) {
      throw new RuntimeException("Error, could not add URL to system classloader", e);
    }
  }

}
