package fitnesse;

import java.io.IOException;
import java.util.Properties;

import fitnesse.components.ComponentFactory;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.responders.editing.ContentFilterResponder;
import fitnesse.testrunner.MultipleTestSystemFactory;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wiki.RecentChanges;
import fitnesse.wiki.RecentChangesWikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wiki.fs.VersionsController;
import fitnesse.wiki.fs.ZipFileVersionsController;
import fitnesse.wikitext.parser.SymbolProvider;

import static fitnesse.ConfigurationParameter.*;

/**
 * Set up a context for running a FitNesse Instance.
 *
 * Please call this only once: some features are registered on (static) factories.
 */
public class ContextConfigurator {

  public static final String DEFAULT_PATH = ".";
  public static final String DEFAULT_ROOT = "FitNesseRoot";
  public static final int DEFAULT_VERSION_DAYS = 14;
  public static final int DEFAULT_COMMAND_PORT = 9123;
  public static final int DEFAULT_PORT = 80;
  public static final String DEFAULT_CONFIG_FILE = "plugins.properties";

  private final Properties properties;

  public ContextConfigurator(Properties properties) {
    this.properties = properties;
  }

  public String getProperty(ConfigurationParameter parameter) {
    return properties.getProperty(parameter.getKey());
  }

  public String getProperty(ConfigurationParameter parameter, String defaultValue) {
    return properties.getProperty(parameter.getKey(), defaultValue);
  }

  public FitNesseContext makeFitNesseContext() throws IOException, PluginException {

    ComponentFactory componentFactory = new ComponentFactory(properties);

    WikiPageFactory wikiPageFactory = (WikiPageFactory) componentFactory.createComponent(WIKI_PAGE_FACTORY_CLASS, FileSystemPageFactory.class);

    FitNesseContext.Builder builder = new FitNesseContext.Builder();
    builder.properties = properties;
    builder.port = getPort();
    builder.rootPath = getProperty(ROOT_PATH, DEFAULT_PATH);
    builder.rootDirectoryName = getProperty(ROOT_DIRECTORY, DEFAULT_ROOT);

    builder.versionsController = (VersionsController) componentFactory.createComponent(VERSIONS_CONTROLLER_CLASS, ZipFileVersionsController.class);
    builder.versionsController.setHistoryDepth(getVersionDays());
    builder.recentChanges = (RecentChanges) componentFactory.createComponent(RECENT_CHANGES_CLASS, RecentChangesWikiPage.class);

    builder.root = wikiPageFactory.makeRootPage(builder.rootPath, builder.rootDirectoryName);


    PluginsLoader pluginsLoader = new PluginsLoader(componentFactory, properties);

    builder.logger = pluginsLoader.makeLogger(getProperty(LOG_DIRECTORY));
    builder.authenticator = pluginsLoader.makeAuthenticator(getProperty(CREDENTIALS));

    SlimTableFactory slimTableFactory = new SlimTableFactory();
    CustomComparatorRegistry customComparatorRegistry = new CustomComparatorRegistry();

    MultipleTestSystemFactory multipleTestSystemFactory = new MultipleTestSystemFactory(slimTableFactory, customComparatorRegistry);
    builder.testSystemFactory = multipleTestSystemFactory;

    FitNesseContext context = builder.createFitNesseContext();

    SymbolProvider symbolProvider = SymbolProvider.wikiParsingProvider;

    pluginsLoader.loadPlugins(context.responderFactory, symbolProvider);
    pluginsLoader.loadResponders(context.responderFactory);
    pluginsLoader.loadTestSystems(multipleTestSystemFactory);
    pluginsLoader.loadSymbolTypes(symbolProvider);
    pluginsLoader.loadSlimTables(slimTableFactory);
    pluginsLoader.loadCustomComparators(customComparatorRegistry);

    ContentFilter contentFilter = pluginsLoader.loadContentFilter();

    // Need something like pre- and post- notifications to hook up this kind of functionality
    if (contentFilter != null)
      context.responderFactory.addFilter("save", new ContentFilterResponder(contentFilter));

    return context;
  }

  private int getPort() {
    String port = getProperty(PORT);
    if (port == null) {
      if (getProperty(COMMAND) != null) {
        return DEFAULT_COMMAND_PORT;
      } else {
        return DEFAULT_PORT;
      }
    }
    return Integer.parseInt(port);
  }

  public int getVersionDays() {
    String days = getProperty(VERSIONS_CONTROLLER_DAYS);
    return days == null ? DEFAULT_VERSION_DAYS : Integer.parseInt(days);
  }


}