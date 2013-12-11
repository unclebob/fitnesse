package fitnesse;

import java.io.IOException;
import java.util.Properties;

import fitnesse.FitNesseContext;
import fitnesse.PluginException;
import fitnesse.PluginsLoader;
import fitnesse.components.ComponentFactory;
import fitnesse.responders.WikiImportTestEventListener;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.responders.editing.SaveResponder;
import fitnesse.testrunner.TestSystemFactoryRegistrar;
import fitnesse.wiki.RecentChanges;
import fitnesse.wiki.RecentChangesWikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wiki.fs.VersionsController;
import fitnesse.wiki.fs.ZipFileVersionsController;
import fitnesse.wikitext.parser.SymbolProvider;

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

  private final Properties properties;

  public ContextConfigurator(Properties properties) {
    this.properties = properties;
  }

  public FitNesseContext makeFitNesseContext() throws IOException, PluginException {

    ComponentFactory componentFactory = new ComponentFactory(properties);

    WikiPageFactory wikiPageFactory = (WikiPageFactory) componentFactory.createComponent(ComponentFactory.WIKI_PAGE_FACTORY_CLASS, FileSystemPageFactory.class);

    FitNesseContext.Builder builder = new FitNesseContext.Builder();
    builder.properties = properties;
    builder.port = getPort();
    builder.rootPath = properties.getProperty("RootPath", DEFAULT_PATH);
    builder.rootDirectoryName = properties.getProperty("RootDirectory", DEFAULT_ROOT);

    builder.versionsController = (VersionsController) componentFactory.createComponent(ComponentFactory.VERSIONS_CONTROLLER_CLASS, ZipFileVersionsController.class);
    builder.versionsController.setHistoryDepth(getVersionDays());
    builder.recentChanges = (RecentChanges) componentFactory.createComponent(ComponentFactory.RECENT_CHANGES_CLASS, RecentChangesWikiPage.class);

    builder.root = wikiPageFactory.makeRootPage(builder.rootPath, builder.rootDirectoryName);


    PluginsLoader pluginsLoader = new PluginsLoader(componentFactory);

    builder.logger = pluginsLoader.makeLogger(properties.getProperty("LogDirectory"));
    builder.authenticator = pluginsLoader.makeAuthenticator(properties.getProperty("Credentials"));

    FitNesseContext context = builder.createFitNesseContext();

    SymbolProvider symbolProvider = SymbolProvider.wikiParsingProvider;

    pluginsLoader.loadPlugins(context.responderFactory, symbolProvider);
    pluginsLoader.loadResponders(context.responderFactory);
    pluginsLoader.loadTestSystems((TestSystemFactoryRegistrar) context.testSystemFactory);
    pluginsLoader.loadSymbolTypes(symbolProvider);
    pluginsLoader.loadSlimTables();
    pluginsLoader.loadCustomComparators();

    ContentFilter contentFilter = pluginsLoader.loadContentFilter();

    // Need something like pre- and post- notifications to hook up this kind of functionality
    SaveResponder.contentFilter = contentFilter;
    WikiImportTestEventListener.register();

    return context;
  }

  private int getPort() {
    String port = properties.getProperty("Port");
    if (port == null) {
      if (properties.getProperty("Command") != null) {
        return DEFAULT_COMMAND_PORT;
      } else {
        return DEFAULT_PORT;
      }
    }
    return Integer.parseInt(port);
  }

  public int getVersionDays() {
    String days = properties.getProperty(ComponentFactory.VERSIONS_CONTROLLER_DAYS);
    return days == null ? DEFAULT_VERSION_DAYS : Integer.parseInt(days);
  }

}