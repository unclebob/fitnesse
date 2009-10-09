package fitnesse.components;

import java.io.File;


public class PluginClasspathLoader {

    private String pluginsDirecory = "./plugins";

    public void addPluginsToClasspath() {
        File pluginsDir = new File(pluginsDirecory);
        File[] plugins = pluginsDir.listFiles();
        for (File plugin : plugins) {
            if (plugin.getName().endsWith("jar")) {
                System.setProperty("java.class.path", getClasspath() + getPathSeperator() + plugin.getAbsolutePath());
            }
        }
    }

    protected String getClasspath() {
        return System.getProperty("java.class.path");
    }


    protected String getPathSeperator() {
        return System.getProperty("path.separator");
    }

}
