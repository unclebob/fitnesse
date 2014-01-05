package fitnesse;

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
  RECENT_CHANGES_CLASS("RecentChanges");

  private final String name;

  private ConfigurationParameter(String key) {
    this.name = key;
  }

  public String getKey() {
    return name;
  }
}
