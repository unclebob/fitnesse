package fitnesse.components;

import java.io.File;


public class PluginsClassLoader {

  protected String pluginsDirectory = "plugins";

  public void addPluginsToClassLoader() throws Exception {
    File pluginsDir = new File(pluginsDirectory);
    if (pluginsDir.exists())
      for (File plugin : pluginsDir.listFiles())
        if (plugin.getName().endsWith("jar"))
          util.FileUtil.addItemsToClasspath(plugin.getCanonicalPath());
  }
}
