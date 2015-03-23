package fitnesse.plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fitnesse.ConfigurationParameter;
import fitnesse.authentication.Authenticator;
import fitnesse.components.ComponentFactory;
import fitnesse.components.ComponentInstantiationException;
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

/**
 * Determines which plugin features to load based on componentFactory's properties (e.g. plugins.properties).
 */
public class PropertyBasedPluginFeatureFactory extends PluginFeatureFactoryBase {
  private final ComponentFactory componentFactory;

  public static Collection<PluginFeatureFactory> loadFromProperties(ComponentFactory componentFactory) throws PluginException {
    PropertyBasedPluginFeatureFactory propBased = new PropertyBasedPluginFeatureFactory(componentFactory);
    Collection<PluginFeatureFactory> legacyWrappers = createWrappersForLegacyPlugins(componentFactory);
    List<PluginFeatureFactory> all = new ArrayList<PluginFeatureFactory>(legacyWrappers.size() + 1);
    all.add(propBased);
    all.addAll(legacyWrappers);
    return all;
  }

  private PropertyBasedPluginFeatureFactory(ComponentFactory componentFactory) {
    this.componentFactory = componentFactory;
  }

  @Override
  public void registerResponders(final ResponderFactory responderFactory) throws PluginException {
    forEachNamedObject(ConfigurationParameter.RESPONDERS, new Registrar() {
      @Override public void register(String key, Class clazz) {
        responderFactory.addResponder(key, clazz);
        LOG.info("Loaded responder " + key + ": " + clazz.getName());
      }
    });
  }

  private String[] getListFromProperties(ConfigurationParameter propertyName) {
    return getListFromProperties(componentFactory, propertyName);
  }

  private static String[] getListFromProperties(ComponentFactory componentFactory, ConfigurationParameter propertyName) {
    String value = componentFactory.getProperty(propertyName.getKey());
    if (value == null)
      return null;
    else
      return value.split(",");
  }

  @Override
  public Authenticator getAuthenticator() {
    return componentFactory.createComponent(ConfigurationParameter.AUTHENTICATOR);
  }

  @Override
  public void registerSymbolTypes(SymbolProvider symbolProvider) throws PluginException {
    String[] symbolTypeNames = getListFromProperties(ConfigurationParameter.SYMBOL_TYPES);
    if (symbolTypeNames != null) {
      for (String symbolTypeName : symbolTypeNames) {
        Class<SymbolType> symbolTypeClass = forName(symbolTypeName.trim());
        symbolProvider.add(componentFactory.createComponent(symbolTypeClass));
        LOG.info("Loaded SymbolType " + symbolTypeClass.getName());
      }
    }
  }

  @Override
  public void registerWikiPageFactories(WikiPageFactoryRegistry registrar) throws PluginException {
    String[] factoryNames = getListFromProperties(ConfigurationParameter.WIKI_PAGE_FACTORIES);
    if (factoryNames != null) {
      for (String factoryName : factoryNames) {
        Class<WikiPageFactory> factory = forName(factoryName.trim());
        registrar.registerWikiPageFactory(componentFactory.createComponent(factory));
        LOG.info("Loaded WikiPageFactory " + factory.getName());
      }
    }
  }

  @Override
  public ContentFilter getContentFilter() {
    return componentFactory.createComponent(ConfigurationParameter.CONTENT_FILTER);
  }

  @Override
  public void registerSlimTables(final SlimTableFactory slimTableFactory) throws PluginException {
    forEachNamedObject(ConfigurationParameter.SLIM_TABLES, new Registrar<SlimTable>() {
      @Override public void register(String key, Class<SlimTable> clazz) {
        slimTableFactory.addTableType(key, clazz);
        LOG.info("Loaded custom SLiM table type " + key + ":" + clazz.getName());
      }
    });
  }

  @Override
  public void registerCustomComparators(final CustomComparatorRegistry customComparatorRegistry) throws PluginException {
    forEachNamedObject(ConfigurationParameter.CUSTOM_COMPARATORS, new Registrar<CustomComparator>() {
      @Override public void register(String key, Class<CustomComparator> clazz) {
        customComparatorRegistry.addCustomComparator(key, componentFactory.createComponent(clazz));
        LOG.info("Loaded custom comparator " + key + ": " + clazz.getName());
      }
    });
  }

  @Override
  public void registerTestSystemFactories(final TestSystemFactoryRegistry registrar) throws PluginException {
    forEachNamedObject(ConfigurationParameter.TEST_SYSTEMS, new Registrar<TestSystemFactory>() {
      @Override public void register(String key, Class<TestSystemFactory> clazz) {
        registrar.registerTestSystemFactory(key, componentFactory.createComponent(clazz));
        LOG.info("Loaded test system " + key + ": " + clazz.getName());
      }
    });
  }

  private void forEachNamedObject(final ConfigurationParameter parameter, Registrar registrar) throws PluginException {
    String[] propList = getListFromProperties(parameter);
    if (propList != null) {
      for (String entry : propList) {
        entry = entry.trim();
        int colonIndex = entry.lastIndexOf(':');
        String prefix = entry.substring(0, colonIndex);
        String className = entry.substring(colonIndex + 1, entry.length());

        register(registrar, prefix, className);
      }
    }
  }

  private void register(Registrar registrar, String prefix, String className) throws PluginException {
    try {
      registrar.register(prefix, forName(className));
    } catch (ComponentInstantiationException e) {
      throw new PluginException("Can not register plug in " + className, e);
    }
  }

  private static Collection<PluginFeatureFactory> createWrappersForLegacyPlugins(ComponentFactory componentFactory) throws PluginException {
    String[] pluginNames = getListFromProperties(componentFactory, ConfigurationParameter.PLUGINS);
    if (pluginNames == null) {
      return Collections.emptyList();
    } else {
      List<PluginFeatureFactory> providers = new ArrayList<PluginFeatureFactory>(pluginNames.length);
      for (String pluginName : pluginNames) {
        Class<?> pluginClass = forName(pluginName);
        Object plugin = componentFactory.createComponent(pluginClass);
        providers.add(new LegacyPluginFeatureFactory(plugin));
      }
      return providers;
    }
  }

  private static <T> Class<T> forName(String className) throws PluginException {
    try {
      return (Class<T>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new PluginException("Unable to load class " + className, e);
    }
  }

  private static interface Registrar<T> {
    void register(String key, Class<T> clazz);
  }
}
