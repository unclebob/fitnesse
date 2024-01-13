package fitnesse;

import fitnesse.authentication.Authenticator;
import fitnesse.components.ComponentFactory;
import fitnesse.components.Logger;
import fitnesse.plugins.PluginException;
import fitnesse.plugins.PluginsLoader;
import fitnesse.reporting.FormatterFactory;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.responders.editing.ContentFilterResponder;
import fitnesse.testrunner.MultipleTestSystemFactory;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.util.ClassUtils;
import fitnesse.wiki.RecentChanges;
import fitnesse.wiki.RecentChangesWikiPage;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.WikiPageFactoryRegistry;
import fitnesse.wiki.fs.FileSystemPageFactory;
import fitnesse.wiki.fs.VersionsController;
import fitnesse.wiki.fs.ZipFileVersionsController;
import fitnesse.wikitext.MarkUpSystems;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.decorator.SlimTableDefaultColoring;

import java.io.IOException;
import java.util.Properties;

import static fitnesse.ConfigurationParameter.COMMAND;
import static fitnesse.ConfigurationParameter.CONFIG_FILE;
import static fitnesse.ConfigurationParameter.CONTEXT_ROOT;
import static fitnesse.ConfigurationParameter.CREDENTIALS;
import static fitnesse.ConfigurationParameter.LOG_DIRECTORY;
import static fitnesse.ConfigurationParameter.RECENT_CHANGES_CLASS;
import static fitnesse.ConfigurationParameter.ROOT_DIRECTORY;
import static fitnesse.ConfigurationParameter.THEME;
import static fitnesse.ConfigurationParameter.VERSIONS_CONTROLLER_CLASS;
import static fitnesse.ConfigurationParameter.VERSIONS_CONTROLLER_DAYS;
import static fitnesse.ConfigurationParameter.WIKI_PAGE_FACTORY_CLASS;

/**
 * Set up a context for running a FitNesse Instance.
 *
 * Please call this only once: some features are registered on (static) factories.
 */
public class ContextConfigurator {
  private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(ContextConfigurator.class.getName());

  private static final String DEFAULT_PATH = ".";
  public static final String DEFAULT_ROOT = "FitNesseRoot";
  public static final String DEFAULT_CONTEXT_ROOT = "/";
  private static final int DEFAULT_VERSION_DAYS = 14;
  private static final int DEFAULT_COMMAND_PORT = 9123;
  public static final int DEFAULT_PORT = 80;
  public static final String DEFAULT_CONFIG_FILE = "plugins.properties";
  public static final String DEFAULT_THEME = "bootstrap";
  public static final int DEFAULT_MAXIMUM_WORKERS = 100;

  /** Some properties are stored in typed fields: */
  private WikiPageFactory wikiPageFactory;
  private Integer port;
  private String rootPath = DEFAULT_PATH;
  private String rootDirectoryName = DEFAULT_ROOT;
  private Integer maximumWorkers = DEFAULT_MAXIMUM_WORKERS;
  private String contextRoot;
  private Logger logger;
  private Authenticator authenticator;
  private VersionsController versionsController;
  private RecentChanges recentChanges;
  /** Others as name-value pairs: */
  private final Properties properties = new Properties();
  private TestSystemListener testSystemListener;
  private ClassLoader classLoader;

  private ContextConfigurator() {
  }

  public static ContextConfigurator empty() {
    return new ContextConfigurator();
  }

  public static ContextConfigurator systemDefaults() {
    return empty()
      .withRootPath(DEFAULT_PATH)
      .withClassLoader(ClassUtils.getClassLoader())
      .withParameter(ROOT_DIRECTORY, DEFAULT_ROOT)
      .withParameter(CONTEXT_ROOT, DEFAULT_CONTEXT_ROOT)
      .withParameter(VERSIONS_CONTROLLER_DAYS, Integer.toString(DEFAULT_VERSION_DAYS))
      .withParameter(CONFIG_FILE, DEFAULT_CONFIG_FILE);
  }

  public ContextConfigurator updatedWith(Properties newProperties) {
    for (String key : newProperties.stringPropertyNames()) {
      withParameter(key, newProperties.getProperty(key));
    }
    return this;
  }

  public ContextConfigurator withTestSystemListener(TestSystemListener testSystemListener) {
    this.testSystemListener = testSystemListener;
    return this;
  }

  public FitNesseContext makeFitNesseContext() throws IOException, PluginException {

    // BIG WARNING: We're setting a static variable here!
    ClassUtils.setClassLoader(classLoader);
    Thread.currentThread().setContextClassLoader(classLoader);

    ComponentFactory componentFactory = new ComponentFactory(properties, classLoader);

    if (port == null) {
      port = getPort();
    }

    FitNesseVersion version = new FitNesseVersion();

    updateFitNesseProperties(version);

    if (wikiPageFactory == null) {
      wikiPageFactory = componentFactory.createComponent(WIKI_PAGE_FACTORY_CLASS, FileSystemPageFactory.class);
    }

    if (versionsController == null) {
      versionsController = componentFactory.createComponent(VERSIONS_CONTROLLER_CLASS, ZipFileVersionsController.class);
    }
    if (recentChanges == null) {
      recentChanges = componentFactory.createComponent(RECENT_CHANGES_CLASS, RecentChangesWikiPage.class);
    }

    PluginsLoader pluginsLoader = new PluginsLoader(componentFactory, classLoader);

    if (logger == null) {
      logger = pluginsLoader.makeLogger(get(LOG_DIRECTORY));
    }
    if (authenticator == null) {
      authenticator = pluginsLoader.makeAuthenticator(get(CREDENTIALS));
    }

    SystemVariableSource variableSource = new SystemVariableSource(properties);

    String theme = variableSource.getProperty(THEME.getKey());
    if (theme == null) {
      theme = pluginsLoader.getDefaultTheme();
      if (theme == null) {
        theme = DEFAULT_THEME;
      }
    }

    SlimTableFactory slimTableFactory = new SlimTableFactory();
    CustomComparatorRegistry customComparatorRegistry = new CustomComparatorRegistry();

    MultipleTestSystemFactory testSystemFactory = new MultipleTestSystemFactory(slimTableFactory, customComparatorRegistry, classLoader);

    FormatterFactory formatterFactory = new FormatterFactory(componentFactory);

    FitNesseContext context = new FitNesseContext(version,
          wikiPageFactory,
          rootPath,
          rootDirectoryName,
          maximumWorkers,
          contextRoot,
          versionsController,
          recentChanges,
          port,
          authenticator,
          logger,
          testSystemFactory,
          testSystemListener,
          formatterFactory,
          properties,
          variableSource,
          theme);

    SymbolProvider wikiParsingProvider = SymbolProvider.wikiParsingProvider;
    SymbolProvider noLinksTableParsingProvider = SymbolProvider.noLinksTableParsingProvider;

    SlimTableDefaultColoring.createInstanceIfNeeded(slimTableFactory);
    SlimTableDefaultColoring.install();

    pluginsLoader.loadResponders(context.responderFactory);

    if (wikiPageFactory instanceof WikiPageFactoryRegistry) {
      pluginsLoader.loadWikiPageFactories((WikiPageFactoryRegistry) wikiPageFactory);
    } else {
      LOG.warning("Wiki page factory does not implement interface WikiPageFactoryRegistrar, configured factories can not be loaded.");
    }
    pluginsLoader.loadTestSystems(testSystemFactory);
    pluginsLoader.loadFormatters(formatterFactory);
    pluginsLoader.loadSymbolTypes(wikiParsingProvider);
    pluginsLoader.loadSymbolTypes(noLinksTableParsingProvider);
    pluginsLoader.loadSlimTables(slimTableFactory);
    pluginsLoader.loadCustomComparators(customComparatorRegistry);
    pluginsLoader.loadTestRunFactories(context.testRunFactoryRegistry);
    pluginsLoader.loadMarkupSystems(MarkUpSystems.STORE);

    ContentFilter contentFilter = pluginsLoader.loadContentFilter();

    // Need something like pre- and post- notifications to hook up this kind of functionality
    if (contentFilter != null)
      context.responderFactory.addFilter("save", new ContentFilterResponder(contentFilter));

    return context;
  }

  private void updateFitNesseProperties(FitNesseVersion version) {
    // Those variables are defined so they can be looked up for as wiki variables.
    if (rootPath != null) {
      properties.setProperty("FITNESSE_ROOTPATH", rootPath);
    }
    properties.setProperty("FITNESSE_PORT", Integer.toString(port));
    properties.setProperty("FITNESSE_VERSION", version.toString());

    // Some code may still expect the values in the properties file rather than their 'typed'
    // counterparts, so provide that:
    for (ConfigurationParameter parameter : ConfigurationParameter.values()) {
      String value = get(parameter);
      if (value != null) {
        properties.setProperty(parameter.getKey(), value);
      }
    }
  }

  private int getPort() {
    if (port == null) {
      if (get(COMMAND) != null) {
        return DEFAULT_COMMAND_PORT;
      } else {
        return DEFAULT_PORT;
      }
    } else {
      return port;
    }
  }

  public ContextConfigurator withRootPath(String rootPath) {
    this.rootPath = rootPath;
    return this;
  }

  public ContextConfigurator withParameter(String key, String value) {
    ConfigurationParameter parameter = ConfigurationParameter.byKey(key);
    if (parameter == null) {
      properties.put(key, value);
    } else {
      withParameter(parameter, value);
    }
    return this;
  }

  public ContextConfigurator withParameter(ConfigurationParameter parameter, String value) {
    switch (parameter) {
      case ROOT_PATH:
        rootPath = value;
        break;
      case ROOT_DIRECTORY:
        rootDirectoryName = value;
        break;
      case CONTEXT_ROOT:
        contextRoot = value;
        if (!contextRoot.startsWith("/")) {
          contextRoot = "/" + contextRoot;
        }
        if (!contextRoot.endsWith("/")) {
          contextRoot = contextRoot + "/";
        }
        break;
      case PORT:
        port = Integer.parseInt(value);
        break;
      case MAXIMUM_WORKERS:
        maximumWorkers = Integer.parseInt(value);
        break;
      default:
        properties.setProperty(parameter.getKey(), value);
        break;
    }
    return this;
  }

  public ContextConfigurator withRootDirectoryName(String rootDirectoryName) {
    this.rootDirectoryName = rootDirectoryName;
    return this;
  }

  public ContextConfigurator withWikiPageFactory(WikiPageFactory wikiPageFactory) {
    this.wikiPageFactory = wikiPageFactory;
    return this;
  }

  public ContextConfigurator withPort(int port) {
    this.port = port;
    return this;
  }

  public ContextConfigurator withAuthenticator(Authenticator authenticator) {
    this.authenticator = authenticator;
    return this;
  }

  public ContextConfigurator withVersionsController(VersionsController versionsController) {
    this.versionsController = versionsController;
    return this;
  }

  public ContextConfigurator withRecentChanges(RecentChanges recentChanges) {
    this.recentChanges = recentChanges;
    return this;
  }

  public ContextConfigurator withClassLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
    return this;
  }

  public String get(ConfigurationParameter parameter) {
    switch(parameter) {
      case ROOT_PATH:
        return rootPath;
      case ROOT_DIRECTORY:
        return rootDirectoryName;
      case CONTEXT_ROOT:
        return contextRoot;
      case PORT:
        return String.valueOf(port);
      case MAXIMUM_WORKERS:
        return String.valueOf(maximumWorkers);
      default:
        return properties.getProperty(parameter.getKey());
    }
  }
}
