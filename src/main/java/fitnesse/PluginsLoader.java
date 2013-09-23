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
import fitnesse.testsystems.slim.CustomComparator;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolType;

import static fitnesse.components.ComponentFactory.*;

public class PluginsLoader {

  private final String endl = System.getProperty("line.separator");

  private final ComponentFactory componentFactory;

  public PluginsLoader(ComponentFactory componentFactory) {
    this.componentFactory = componentFactory;
  }

  public String loadPlugins(ResponderFactory responderFactory, SymbolProvider symbolProvider) throws ClassNotFoundException, IllegalAccessException, InvocationTargetException {
    StringBuffer buffer = new StringBuffer();
    String[] responderPlugins = getListFromProperties(PLUGINS);
    if (responderPlugins != null) {
      buffer.append("\tCustom plugins loaded:").append(endl);
      for (String responderPlugin : responderPlugins) {
        Class<?> pluginClass = Class.forName(responderPlugin);
        loadRespondersFromPlugin(pluginClass, responderFactory, buffer);
        loadSymbolTypesFromPlugin(pluginClass, symbolProvider, buffer);
      }
    }
    return buffer.toString();
  }

  private void loadRespondersFromPlugin(Class<?> pluginClass, ResponderFactory responderFactory, StringBuffer buffer)
          throws IllegalAccessException, InvocationTargetException {
    try {
      Method method = pluginClass.getMethod("registerResponders", ResponderFactory.class);
      method.invoke(pluginClass, responderFactory);
      buffer.append("\t\t").append("responders:").append(pluginClass.getName()).append(endl);
    } catch (NoSuchMethodException e) {
      // ok, no responders to register in this plugin
    }
  }

  private void loadSymbolTypesFromPlugin(Class<?> pluginClass, SymbolProvider symbolProvider, StringBuffer buffer)
          throws IllegalAccessException, InvocationTargetException {
    try {
      Method method = pluginClass.getMethod("registerSymbolTypes", SymbolProvider.class);
      method.invoke(pluginClass, symbolProvider);
      buffer.append("\t\t").append("widgets:").append(pluginClass.getName()).append(endl);
    } catch (NoSuchMethodException e) {
      // ok, no widgets to register in this plugin
    }
  }

  public String loadResponders(ResponderFactory responderFactory) throws ClassNotFoundException {
    StringBuffer buffer = new StringBuffer();
    String[] responderList = getListFromProperties(RESPONDERS);
    if (responderList != null) {
      buffer.append("\tCustom responders loaded:").append(endl);
      for (String responder : responderList) {
        String[] values = responder.trim().split(":");
        String key = values[0];
        String className = values[1];
        responderFactory.addResponder(key, className);
        buffer.append("\t\t").append(key).append(":").append(className).append(endl);
      }
    }
    return buffer.toString();
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

  public String loadSymbolTypes(SymbolProvider symbolProvider) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    StringBuffer buffer = new StringBuffer();
    String[] symbolTypeNames = getListFromProperties(SYMBOL_TYPES);
    if (symbolTypeNames != null) {
      buffer.append("\tCustom symbol types loaded:").append(endl);
      for (String symbolTypeName : symbolTypeNames) {
        Class<?> symbolTypeClass = Class.forName(symbolTypeName.trim());
        symbolProvider.add((SymbolType)symbolTypeClass.newInstance());
        buffer.append("\t\t").append(symbolTypeClass.getName()).append(endl);
      }
    }
    return buffer.toString();
  }

  public String loadContentFilter() {
    ContentFilter filter = (ContentFilter) componentFactory.createComponent(CONTENT_FILTER);
    if (filter != null) {
      SaveResponder.contentFilter = filter;
      return "\tContent filter installed: " + filter.getClass().getName() + "\n";
    }
    return "";
  }

  @SuppressWarnings("unchecked")
  public String loadSlimTables() throws ClassNotFoundException {
    StringBuffer buffer = new StringBuffer();
    String[] tableList = getListFromProperties(SLIM_TABLES);
    if (tableList != null) {
      buffer.append("\tCustom SLiM table types loaded:").append(endl);
      for (String table : tableList) {
        table = table.trim();
        int colonIndex = table.lastIndexOf(':');
        String key = table.substring(0, colonIndex);
        String className = table.substring(colonIndex + 1, table.length());

        SlimTableFactory.addTableType(key, (Class<? extends SlimTable>) Class.forName(className));
        buffer.append("\t\t").append(key).append(":").append(className).append(endl);
      }
    }
    return buffer.toString();
  }

  public String loadCustomComparators() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
      StringBuffer buffer = new StringBuffer();
      String[] tableList = getListFromProperties(CUSTOM_COMPARATORS);
      if (tableList != null) {
        buffer.append("\tCustom Comparators loaded:").append(endl);
        for (String table : tableList) {
          table = table.trim();
          int colonIndex = table.lastIndexOf(':');
          String prefix = table.substring(0, colonIndex);
          String className = table.substring(colonIndex + 1, table.length());
          
          CustomComparatorRegistry.addCustomComparator(prefix, (CustomComparator) Class.forName(className).newInstance());
          buffer.append("\t\t").append(prefix).append(":").append(className).append(endl);
        }
      }
      return buffer.toString();
    }
}
