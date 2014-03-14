package fitnesse;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parameters used to configure FitNesse.
 */
public enum ConfigurationParameter {

  CONFIG_FILE("ConfigFile"),
  LOG_LEVEL("LogLevel"),
  LOG_DIRECTORY("LogDirectory"),
  CREDENTIALS("Credentials"),
  ROOT_PATH("RootPath"),
  ROOT_DIRECTORY("FitNesseRoot"),
  PORT("Port"),
  OUTPUT("RedirectOutput"),
  OMITTING_UPDATES("OmittingUpdates"),
  INSTALL_ONLY("InstallOnly"),
  COMMAND("Command"),
  WIKI_PAGE_FACTORY_CLASS("WikiPageFactory"),
  PLUGINS("Plugins"),
  RESPONDERS("Responders"),
  TEST_SYSTEMS("TestSystems"),
  SYMBOL_TYPES("SymbolTypes"),
  SLIM_TABLES("SlimTables"),
  AUTHENTICATOR("Authenticator"),
  CUSTOM_COMPARATORS("CustomComparators"),
  CONTENT_FILTER("ContentFilter"),
  VERSIONS_CONTROLLER_CLASS("VersionsController"),
  VERSIONS_CONTROLLER_DAYS("VersionsController.days"),
  RECENT_CHANGES_CLASS("RecentChanges"),
  CONTEXT_ROOT("ContextRoot");

  private static final Logger LOG = Logger.getLogger(ConfigurationParameter.class.getName());

  private final String name;

  private ConfigurationParameter(String key) {
    this.name = key;
  }

  public String getKey() {
    return name;
  }

  public static Properties makeProperties(Object... keyValuePairs) {
    if (keyValuePairs.length % 2 != 0) {
      throw new IllegalArgumentException("Number of arguments should be even (name, value)");
    }

    Properties properties = new Properties();
    for (int i = 0; i < keyValuePairs.length; i += 2) {
      String key = keyValuePairs[i] instanceof ConfigurationParameter ? ((ConfigurationParameter) keyValuePairs[i]).getKey() : keyValuePairs[i].toString();
      String value = keyValuePairs[i+1].toString();
      properties.setProperty(key, value);
    }
    return properties;
  }

  public static Properties loadProperties(File propertiesFile) {
    FileInputStream propertiesStream = null;
    Properties properties = new Properties();
    try {
      propertiesStream = new FileInputStream(propertiesFile);
    } catch (FileNotFoundException e) {
      try {
        LOG.info(String.format("No configuration file found (%s)", propertiesFile.getCanonicalPath()));
      } catch (IOException ioe) {
        LOG.info(String.format("No configuration file found (%s)", propertiesFile));
      }
    }

    if (propertiesStream != null) {
      try {
        properties.load(propertiesStream);
        propertiesStream.close();
      } catch (IOException e) {
        LOG.log(Level.WARNING, String.format("Error reading configuration: %s", e.getMessage()));
      }
    }

    return properties;
  }

  public static ConfigurationParameter byKey(String key) {
    for (ConfigurationParameter parameter : ConfigurationParameter.values()) {
      if (parameter.getKey().equals(key)) {
        return parameter;
      }
    }
    return null;
  }
}
