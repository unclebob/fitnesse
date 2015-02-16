package fitnesse.plugins;

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
public class PropertyBasedPluginFeatureFactory implements PluginFeatureFactory {
  private static final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(PropertyBasedPluginFeatureFactory.class.getName());
  private final ComponentFactory componentFactory;

  public PropertyBasedPluginFeatureFactory(ComponentFactory componentFactory) {
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

  private <T> Class<T> forName(String className) throws PluginException {
    try {
      return (Class<T>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new PluginException("Unable to load class " + className, e);
    }
  }

  static private interface Registrar<T> {
    void register(String key, Class<T> clazz);
  }
}
