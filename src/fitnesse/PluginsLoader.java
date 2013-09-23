package fitnesse;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.ComponentFactory;
import fitnesse.components.Logger;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.responders.editing.SaveResponder;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolType;

import static fitnesse.components.ComponentFactory.*;

public class PluginsLoader {

  java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(PluginsLoader.class.getName());

  private final ComponentFactory componentFactory;

  public PluginsLoader(ComponentFactory componentFactory) {
    this.componentFactory = componentFactory;
  }

  public void loadPlugins(ResponderFactory responderFactory, SymbolProvider symbolProvider) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
    String[] responderPlugins = getListFromProperties(PLUGINS);
    if (responderPlugins != null) {
      for (String responderPlugin : responderPlugins) {
        Class<?> pluginClass = Class.forName(responderPlugin);
        loadRespondersFromPlugin(pluginClass, responderFactory);
        loadSymbolTypesFromPlugin(pluginClass, symbolProvider);
      }
    }
  }

  private void loadRespondersFromPlugin(Class<?> pluginClass, ResponderFactory responderFactory)
    throws IllegalAccessException, InvocationTargetException {
    try {
      Method method = pluginClass.getMethod("registerResponders", ResponderFactory.class);
      method.invoke(pluginClass, responderFactory);
      LOG.info("Loaded responder : " + pluginClass.getName());
    } catch (NoSuchMethodException e) {
      // ok, no responders to register in this plugin
    }
  }

  private void loadSymbolTypesFromPlugin(Class<?> pluginClass, SymbolProvider symbolProvider)
    throws IllegalAccessException, InvocationTargetException {
    try {
      Method method = pluginClass.getMethod("registerSymbolTypes", SymbolProvider.class);
      method.invoke(pluginClass, symbolProvider);
      LOG.info("Loaded SymbolType: " + pluginClass.getName());
    } catch (NoSuchMethodException e) {
      // ok, no widgets to register in this plugin
    }
  }

  public void loadResponders(ResponderFactory responderFactory) throws ClassNotFoundException {
    String[] responderList = getListFromProperties(RESPONDERS);
    if (responderList != null) {
      for (String responder : responderList) {
        String[] values = responder.trim().split(":");
        String key = values[0];
        String className = values[1];
        responderFactory.addResponder(key, className);
        LOG.info("Loaded responder " + key + ":" + className);
      }
    }
  }

  private String[] getListFromProperties(String propertyName) {
    String value = componentFactory.getProperty(propertyName);
    if (value == null)
      return null;
    else
      return value.split(",");
  }

  public Logger makeLogger(String logDirectory) {
    return logDirectory != null ? new Logger(logDirectory) : null;
  }

  public Authenticator makeAuthenticator(String authenticationParameter) throws Exception {
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
    Authenticator authenticator = (Authenticator) componentFactory.createComponent(AUTHENTICATOR);
    return authenticator == null ? defaultAuthenticator : authenticator;
  }

  public void loadSymbolTypes(SymbolProvider symbolProvider) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    String[] symbolTypeNames = getListFromProperties(SYMBOL_TYPES);
    if (symbolTypeNames != null) {
      for (String symbolTypeName : symbolTypeNames) {
        Class<?> symbolTypeClass = Class.forName(symbolTypeName.trim());
        symbolProvider.add((SymbolType)symbolTypeClass.newInstance());
        LOG.info("Loaded SymbolType " + symbolTypeClass.getName());
      }
    }
  }

  public void loadContentFilter() {
    ContentFilter filter = (ContentFilter) componentFactory.createComponent(CONTENT_FILTER);
    if (filter != null) {
      SaveResponder.contentFilter = filter;
      LOG.info("Content filter installed: " + filter.getClass().getName());
    }
  }

  @SuppressWarnings("unchecked")
  public void loadSlimTables() throws ClassNotFoundException {
    String[] tableList = getListFromProperties(SLIM_TABLES);
    if (tableList != null) {
      for (String table : tableList) {
        table = table.trim();
        int colonIndex = table.lastIndexOf(':');
        String key = table.substring(0, colonIndex);
        String className = table.substring(colonIndex + 1, table.length());

        SlimTableFactory.addTableType(key, (Class<? extends SlimTable>) Class.forName(className));
        LOG.info("Loaded custom SLiM table type "+ key + ":" + className);
      }
    }
  }

}
