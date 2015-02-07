package fitnesse.plugins;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import fitnesse.Responder;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.ComponentFactory;
import fitnesse.components.Logger;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.testrunner.TestSystemFactoryRegistry;
import fitnesse.testsystems.TestSystemFactory;
import fitnesse.testsystems.slim.CustomComparator;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.WikiPageFactoryRegistry;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolType;

public class PluginsLoader {
  private final static java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(PluginsLoader.class.getName());

  private final ComponentFactory componentFactory;
  private final List<PluginFeatureFactory> pluginFeatureFactories = new ArrayList<PluginFeatureFactory>();

  public PluginsLoader(ComponentFactory componentFactory) {
    this.componentFactory = componentFactory;
    fillPluginFeatureFactories();
  }

  public void loadPlugins(ResponderFactory responderFactory,
                          SymbolProvider symbolProvider,
                          WikiPageFactoryRegistry wikiPageFactoryRegistry,
                          TestSystemFactoryRegistry testSystemFactoryRegistry,
                          SlimTableFactory slimTableFactory,
                          CustomComparatorRegistry customComparatorRegistry) throws PluginException {
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      for (Object plugin : pff.getPlugins()) {
        register(plugin, "registerResponders", ResponderFactory.class, responderFactory);
        register(plugin, "registerSymbolTypes", SymbolProvider.class, symbolProvider);
        register(plugin, "registerWikiPageFactories", WikiPageFactoryRegistry.class, wikiPageFactoryRegistry);
        register(plugin, "registerTestSystemFactories", TestSystemFactoryRegistry.class, testSystemFactoryRegistry);
        register(plugin, "registerSlimTableFactories", SlimTableFactory.class, slimTableFactory);
        register(plugin, "registerCustomComparatorRegistries", CustomComparatorRegistry.class, customComparatorRegistry);
      }
    }
  }

  private <T> void register(Object plugin, String methodName, Class<T> registrarType, T registrar)
          throws PluginException {
    Method method;
    try {
      method = plugin.getClass().getMethod(methodName, registrarType);
    } catch (NoSuchMethodException e) {
      // ok, no widgets to register in this plugin
      return;
    }

    try {
      method.invoke(plugin, registrar);
    } catch (InvocationTargetException e) {
      throw new PluginException("Unable to execute method " + methodName, e);
    } catch (IllegalAccessException e) {
      throw new PluginException("Unable to execute method " + methodName, e);
    }
  }

  private void fillPluginFeatureFactories() {
    pluginFeatureFactories.add(new PropertyBasedPluginFeatureFactory());

    for (PluginFeatureFactory factory : ServiceLoader.load(PluginFeatureFactory.class)) {
      pluginFeatureFactories.add(factory);
    }

    for (PluginFeatureFactory factory : pluginFeatureFactories) {
      factory.setComponentFactory(componentFactory);
    }
  }

  public void loadResponders(final ResponderFactory responderFactory) throws PluginException {
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      Map<String, Class<? extends Responder>> responderFactories = pff.getResponders();
      for (Map.Entry<String, Class<? extends Responder>> rF : responderFactories.entrySet()) {
        String key = rF.getKey();
        Class<? extends Responder> clazz = rF.getValue();

        responderFactory.addResponder(key, clazz);
        LOG.info("Loaded responder " + key + ": " + clazz.getName());
      }
    }
  }

  public Logger makeLogger(String logDirectory) {
    return logDirectory != null ? new Logger(logDirectory) : null;
  }

  public Authenticator makeAuthenticator(String authenticationParameter) throws IOException {
    Authenticator authenticator = new PromiscuousAuthenticator();
    if (authenticationParameter != null) {
      if (new File(authenticationParameter).exists())
        authenticator = new MultiUserAuthenticator(authenticationParameter);
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

  public void loadSymbolTypes(SymbolProvider symbolProvider) throws PluginException {
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      for (SymbolType st : pff.getSymbolTypes()) {
        symbolProvider.add(st);
        LOG.info("Loaded SymbolType " + st.getClass().getName());
      }
    }
  }

  public void loadWikiPageFactories(WikiPageFactory wikiPageFactory) throws PluginException {
    if (!(wikiPageFactory instanceof WikiPageFactoryRegistry)) {
      LOG.warning("Wiki page factory does not implement interface WikiPageFactoryRegistrar, configured factories can not be loaded.");
      return;
    }
    WikiPageFactoryRegistry registrar = (WikiPageFactoryRegistry) wikiPageFactory;
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      for (WikiPageFactory factory : pff.getWikiPageFactories()) {
        registrar.registerWikiPageFactory(factory);
        LOG.info("Loaded WikiPageFactory " + factory.getClass().getName());
      }
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
      Map<String, Class<? extends SlimTable>> tableFactories = pff.getSlimTables();
      for (Map.Entry<String, Class<? extends SlimTable>> entry : tableFactories.entrySet()) {
        String key = entry.getKey();
        Class<? extends SlimTable> clazz = entry.getValue();

        slimTableFactory.addTableType(key, clazz);
        LOG.info("Loaded custom SLiM table type " + key + ":" + clazz.getName());
      }
    }
  }

  public void loadCustomComparators(final CustomComparatorRegistry customComparatorRegistry) throws PluginException {
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      Map<String, ? extends CustomComparator> comparators = pff.getCustomComparators();
      for (Map.Entry<String, ? extends CustomComparator> entry : comparators.entrySet()) {
        String key = entry.getKey();
        CustomComparator customComparator = entry.getValue();

        customComparatorRegistry.addCustomComparator(key, customComparator);
        LOG.info("Loaded custom comparator " + key + ": " + customComparator);
      }
    }
  }

  public void loadTestSystems(final TestSystemFactoryRegistry registrar) throws PluginException {
    for (PluginFeatureFactory pff : pluginFeatureFactories) {
      Map<String, ? extends TestSystemFactory> systemFactories = pff.getTestSystemFactories();
      for (Map.Entry<String, ? extends TestSystemFactory> entry : systemFactories.entrySet()) {
        String key = entry.getKey();
        TestSystemFactory factory = entry.getValue();

        registrar.registerTestSystemFactory(key, factory);
        LOG.info("Loaded test system " + key + ": " + factory);
      }
    }
  }
}
