package fitnesse;

import java.io.IOException;
import java.util.Properties;

import fitnesse.authentication.Authenticator;
import fitnesse.components.ComponentFactory;
import fitnesse.components.Logger;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.responders.editing.ContentFilterResponder;
import fitnesse.testrunner.MultipleTestSystemFactory;
import fitnesse.testsystems.TestSystemListener;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wiki.RecentChanges;
import fitnesse.wiki.RecentChangesWikiPage;
import fitnesse.wiki.WikiPage;
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

  private static final String DEFAULT_PATH = ".";
  public static final String DEFAULT_ROOT = "FitNesseRoot";
  public static final String DEFAULT_CONTEXT_ROOT = "/";
  private static final int DEFAULT_VERSION_DAYS = 14;
  private static final int DEFAULT_COMMAND_PORT = 9123;
  public static final int DEFAULT_PORT = 80;
  public static final String DEFAULT_CONFIG_FILE = "plugins.properties";

  /** Some properties are stored in typed fields: */
  private WikiPage root;
  private Integer port;
  private String rootPath;
  private String rootDirectoryName;
  private String contextRoot;
  private Logger logger;
  private Authenticator authenticator;
  private VersionsController versionsController;
  private RecentChanges recentChanges;
  /** Others as name-value pairs: */
  private final Properties properties = new Properties();
  private TestSystemListener testSystemListener;

  private ContextConfigurator() {
  }

  public static ContextConfigurator empty() {
    return new ContextConfigurator();
  }

  public static ContextConfigurator systemDefaults() {
    return empty()
      .withRootPath(DEFAULT_PATH)
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
    ComponentFactory componentFactory = new ComponentFactory(properties);

    WikiPageFactory wikiPageFactory = (WikiPageFactory) componentFactory.createComponent(WIKI_PAGE_FACTORY_CLASS, FileSystemPageFactory.class);

    if (port == null) {
      port = getPort();
    }

    if (versionsController == null) {
      versionsController = (VersionsController) componentFactory.createComponent(VERSIONS_CONTROLLER_CLASS, ZipFileVersionsController.class);
      Integer versionDays = getVersionDays();
      if (versionDays != null) {
        versionsController.setHistoryDepth(versionDays);
      }
    }
    if (recentChanges == null) {
      recentChanges = (RecentChanges) componentFactory.createComponent(RECENT_CHANGES_CLASS, RecentChangesWikiPage.class);
    }

    if (root == null) {
      root = wikiPageFactory.makeRootPage(rootPath, rootDirectoryName);
    }

    PluginsLoader pluginsLoader = new PluginsLoader(componentFactory, properties);

    if (logger == null) {
      logger = pluginsLoader.makeLogger(get(LOG_DIRECTORY));
    }
    if (authenticator == null) {
      authenticator = pluginsLoader.makeAuthenticator(get(CREDENTIALS));
    }

    SlimTableFactory slimTableFactory = new SlimTableFactory();
    CustomComparatorRegistry customComparatorRegistry = new CustomComparatorRegistry();

    MultipleTestSystemFactory testSystemFactory = new MultipleTestSystemFactory(slimTableFactory, customComparatorRegistry);
    FitNesseVersion version = new FitNesseVersion();
    addBackwardsCompatibleProperties(version);

    FitNesseContext context = new FitNesseContext(version,
          root,
          rootPath,
          rootDirectoryName,
          contextRoot,
          versionsController,
          recentChanges,
          port,
          authenticator,
          logger,
          testSystemFactory,
          testSystemListener,
          properties);

    SymbolProvider symbolProvider = SymbolProvider.wikiParsingProvider;

    pluginsLoader.loadPlugins(context.responderFactory, symbolProvider);
    pluginsLoader.loadResponders(context.responderFactory);
    pluginsLoader.loadTestSystems(testSystemFactory);
    pluginsLoader.loadSymbolTypes(symbolProvider);
    pluginsLoader.loadSlimTables(slimTableFactory);
    pluginsLoader.loadCustomComparators(customComparatorRegistry);

    ContentFilter contentFilter = pluginsLoader.loadContentFilter();

    // Need something like pre- and post- notifications to hook up this kind of functionality
    if (contentFilter != null)
      context.responderFactory.addFilter("save", new ContentFilterResponder(contentFilter));

    return context;
  }

  private void addBackwardsCompatibleProperties(FitNesseVersion version) {
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

  public ContextConfigurator withRoot(WikiPage root) {
    this.root = root;
    return this;
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
      default:
        properties.setProperty(parameter.getKey(), value);
    }
    return this;
  }

  public ContextConfigurator withRootDirectoryName(String rootDirectoryName) {
    this.rootDirectoryName = rootDirectoryName;
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
      default:
        return properties.getProperty(parameter.getKey());
    }
  }

  public Integer getVersionDays() {
    String days = get(VERSIONS_CONTROLLER_DAYS);
    return days == null ? null : Integer.parseInt(days);
  }
}