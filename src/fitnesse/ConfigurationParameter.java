package fitnesse;

/**
 * Parameters used to configure FitNesse.
 */
public final class ConfigurationParameter {

  public static final String CONFIG_FILE = "ConfigFile";
  public static final String LOG_LEVEL = "LogLevel";
  public static final String LOG_DIRECTORY = "LogDirectory";
  public static final String CREDENTIALS = "Credentials";
  public static final String ROOT_PATH = "RootPath";
  public static final String ROOT_DIRECTORY = "FitNesseRoot";
  public static final String PORT = "Port";
  public static final String OUTPUT = "RedirectOutput";
  public static final String OMITTING_UPDATES = "OmittingUpdates";
  public static final String INSTALL_ONLY = "InstallOnly";
  public static final String COMMAND = "Command";
  public static final String WIKI_PAGE_FACTORY_CLASS = "WikiPageFactory";
  public static final String PLUGINS = "Plugins";
  public static final String RESPONDERS = "Responders";
  public static final String TEST_SYSTEMS = "TestSystems";
  public static final String SYMBOL_TYPES = "SymbolTypes";
  public static final String SLIM_TABLES = "SlimTables";
  public static final String AUTHENTICATOR = "Authenticator";
  public static final String CUSTOM_COMPARATORS = "CustomComparators";
  public static final String CONTENT_FILTER = "ContentFilter";
  public static final String VERSIONS_CONTROLLER_CLASS = "VersionsController";
  public static final String VERSIONS_CONTROLLER_DAYS = VERSIONS_CONTROLLER_CLASS + ".days";
  public static final String RECENT_CHANGES_CLASS = "RecentChanges";

  private ConfigurationParameter() {
  }
}
