package fitnesse.plugins;

import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.ComponentFactory;
import fitnesse.components.Logger;
import fitnesse.reporting.FormatterRegistry;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.testrunner.TestSystemFactoryRegistry;
import fitnesse.testrunner.run.TestRunFactoryRegistry;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wiki.WikiPageFactoryRegistry;
import fitnesse.wikitext.MarkUpSystems;
import fitnesse.wikitext.parser.SymbolProvider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;

public class PluginsLoader {
  private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(PluginsLoader.class.getName());

  private final ComponentFactory componentFactory;
  private final ClassLoader classLoader;
  private final Collection<PluginFeatureFactory> pluginFeatureFactories;

  public PluginsLoader(ComponentFactory componentFactory, ClassLoader classLoader) throws PluginException {
    this.componentFactory = componentFactory;
    this.classLoader = classLoader;
    this.pluginFeatureFactories = findPluginFeatureFactories();
  }

  private Collection<PluginFeatureFactory> findPluginFeatureFactories() throws PluginException {
    List<PluginFeatureFactory> factories = new ArrayList<>(PropertyBasedPluginFeatureFactory.loadFromProperties(componentFactory));

    for (PluginFeatureFactory factory : ServiceLoader.load(PluginFeatureFactory.class, classLoader)) {
      factories.add(factory);
    }
    return factories;
  }

  public void loadResponders(final ResponderFactory responderFactory) throws PluginException {
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      pff.registerResponders(responderFactory);
    }
  }

  public Logger makeLogger(String logDirectory) {
    return logDirectory != null ? new Logger(logDirectory) : null;
  }

  public Authenticator makeAuthenticator(String authenticationParameter) throws IOException, PluginException {
    Authenticator authenticator = new PromiscuousAuthenticator();
    if (authenticationParameter != null) {
      if (new File(authenticationParameter).exists())
        try {
          authenticator = new MultiUserAuthenticator(authenticationParameter);
        } catch (ReflectiveOperationException e) {
          throw new PluginException("Could not instantiate authentication classes", e);
        }
      else {
        String[] values = authenticationParameter.split(":");
        authenticator = new OneUserAuthenticator(values[0], values[1]);
      }
    }

    return getAuthenticator(authenticator);
  }

  public Authenticator getAuthenticator(Authenticator defaultAuthenticator) {
    Authenticator authenticator = null;
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      authenticator = pff.getAuthenticator();
      if (authenticator != null) {
        break;
      }
    }
    return authenticator == null ? defaultAuthenticator : authenticator;
  }

  public String getDefaultTheme() {
    String theme = null;
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      theme = pff.getDefaultTheme();
      if (theme != null) {
        break;
      }
    }
    return theme;
  }

  public void loadSymbolTypes(SymbolProvider symbolProvider) throws PluginException {
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      pff.registerSymbolTypes(symbolProvider);
    }
  }

  public void loadWikiPageFactories(WikiPageFactoryRegistry registrar) throws PluginException {
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      pff.registerWikiPageFactories(registrar);
    }
  }

  public void loadFormatters(FormatterRegistry registrar) throws PluginException {
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      pff.registerFormatters(registrar);
    }
  }

  public ContentFilter loadContentFilter() {
    ContentFilter filter = null;
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      filter = pff.getContentFilter();
      if (filter != null) {
        break;
      }
    }
    if (filter != null) {
      LOG.info("Content filter installed: " + filter.getClass().getName());
    }
    return filter;
  }

  public void loadSlimTables(final SlimTableFactory slimTableFactory) throws PluginException {
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      pff.registerSlimTables(slimTableFactory);
    }
  }

  public void loadCustomComparators(final CustomComparatorRegistry customComparatorRegistry) throws PluginException {
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      pff.registerCustomComparators(customComparatorRegistry);
    }
  }

  public void loadTestSystems(final TestSystemFactoryRegistry registrar) throws PluginException {
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      pff.registerTestSystemFactories(registrar);
    }
  }

  public void loadTestRunFactories(final TestRunFactoryRegistry registry) throws PluginException {
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      pff.registerTestRunFactories(registry);
    }
  }

  public void loadMarkupSystems(MarkUpSystems systems) {
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      pff.registerMarkupSystems(systems);
    }
  }
}
