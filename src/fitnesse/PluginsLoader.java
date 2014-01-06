package fitnesse;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.ComponentFactory;
import fitnesse.components.Logger;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.testrunner.TestSystemFactoryRegistrar;
import fitnesse.testsystems.TestSystemFactory;
import fitnesse.testsystems.slim.CustomComparator;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolType;

public class PluginsLoader {
  private final static java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(PluginsLoader.class.getName());

  private final ComponentFactory componentFactory;
  private final Properties properties;

  public PluginsLoader(ComponentFactory componentFactory, Properties properties) {
    this.componentFactory = componentFactory;
    this.properties = properties;
  }

  public void loadPlugins(ResponderFactory responderFactory, SymbolProvider symbolProvider) throws PluginException {
    String[] responderPlugins = getListFromProperties(ConfigurationParameter.PLUGINS);
    if (responderPlugins != null) {
      for (String responderPlugin : responderPlugins) {
        Class<?> pluginClass = forName(responderPlugin);
        loadRespondersFromPlugin(pluginClass, responderFactory);
        loadSymbolTypesFromPlugin(pluginClass, symbolProvider);
      }
    }
  }

  private void loadRespondersFromPlugin(Class<?> pluginClass, ResponderFactory responderFactory)
    throws PluginException {
    try {
      Method method = pluginClass.getMethod("registerResponders", ResponderFactory.class);
      method.invoke(pluginClass, responderFactory);
      LOG.info("Loaded responder: " + pluginClass.getName());
    } catch (NoSuchMethodException e) {
      // ok, no responders to register in this plugin
    } catch (InvocationTargetException e) {
      throw new PluginException("Unable to execute method registerResponders", e);
    } catch (IllegalAccessException e) {
      throw new PluginException("Unable to execute method registerResponders", e);
    }
  }

  private void loadSymbolTypesFromPlugin(Class<?> pluginClass, SymbolProvider symbolProvider)
          throws PluginException {
    try {
      Method method = pluginClass.getMethod("registerSymbolTypes", SymbolProvider.class);
      method.invoke(pluginClass, symbolProvider);
      LOG.info("Loaded SymbolType: " + pluginClass.getName());
    } catch (NoSuchMethodException e) {
      // ok, no widgets to register in this plugin
    } catch (InvocationTargetException e) {
      throw new PluginException("Unable to execute method registerSymbolTypes", e);
    } catch (IllegalAccessException e) {
      throw new PluginException("Unable to execute method registerSymbolTypes", e);
    }
  }

  public void loadResponders(final ResponderFactory responderFactory) throws PluginException {
    forEachNamedObject(ConfigurationParameter.RESPONDERS, new Registrar() {
      @Override public void register(String key, Class clazz) {
        responderFactory.addResponder(key, clazz);
        LOG.info("Loaded responder " + key + ": " + clazz.getName());
      }
    });
  }

  private String[] getListFromProperties(ConfigurationParameter propertyName) {
    String value = properties.getProperty(propertyName.getKey());
    if (value == null)
      return null;
    else
      return value.split(",");
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
    Authenticator authenticator = (Authenticator) componentFactory.createComponent(ConfigurationParameter.AUTHENTICATOR);
    return authenticator == null ? defaultAuthenticator : authenticator;
  }

  public void loadSymbolTypes(SymbolProvider symbolProvider) throws PluginException {
    String[] symbolTypeNames = getListFromProperties(ConfigurationParameter.SYMBOL_TYPES);
    if (symbolTypeNames != null) {
      for (String symbolTypeName : symbolTypeNames) {
        Class<?> symbolTypeClass = forName(symbolTypeName.trim());
        symbolProvider.add((SymbolType) newInstance(symbolTypeClass));
        LOG.info("Loaded SymbolType " + symbolTypeClass.getName());
      }
    }
  }

  public ContentFilter loadContentFilter() {
    ContentFilter filter = (ContentFilter) componentFactory.createComponent(ConfigurationParameter.CONTENT_FILTER);
    if (filter != null) {
      LOG.info("Content filter installed: " + filter.getClass().getName());
    }
    return filter;
  }

  public void loadSlimTables() throws PluginException {
    forEachNamedObject(ConfigurationParameter.SLIM_TABLES, new Registrar() {
      @Override public void register(String key, Class clazz) {
        SlimTableFactory.addTableType(key, (Class<? extends SlimTable>) clazz);
        LOG.info("Loaded custom SLiM table type " + key + ":" + clazz.getName());
      }
    });
  }

  public void loadCustomComparators() throws PluginException {
    forEachNamedObject(ConfigurationParameter.CUSTOM_COMPARATORS, new Registrar() {
      @Override public void register(String key, Class clazz) throws IllegalAccessException, InstantiationException {
        CustomComparatorRegistry.addCustomComparator(key, (CustomComparator) clazz.newInstance());
        LOG.info("Loaded custom comparator " + key + ": " + clazz.getName());
      }
    });
  }

  public void loadTestSystems(final TestSystemFactoryRegistrar registrar) throws PluginException {
    forEachNamedObject(ConfigurationParameter.TEST_SYSTEMS, new Registrar() {
      @Override public void register(String key, Class clazz) throws IllegalAccessException, InstantiationException {
        registrar.registerTestSystemFactory(key, (TestSystemFactory) clazz.newInstance());
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
    } catch (IllegalAccessException e) {
      throw new PluginException("Can not register class " + className, e);
    } catch (InstantiationException e) {
      throw new PluginException("Can not instantiate class " + className, e);
    }
  }

  private Class<?> forName(String className) throws PluginException {
    try {
      return Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new PluginException("Unable to load class " + className, e);
    }
  }

  private <T> T newInstance(Class<T> clazz) throws PluginException {
    try {
      return clazz.newInstance();
    } catch (IllegalAccessException e) {
      throw new PluginException("Can not register class " + clazz.getName(), e);
    } catch (InstantiationException e) {
      throw new PluginException("Can not instantiate class " + clazz.getName(), e);
    }
  }

  static private interface Registrar {
    void register(String key, Class<?> clazz) throws IllegalAccessException, InstantiationException;
  }
}
