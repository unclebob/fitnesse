// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import fitnesse.authentication.Authenticator;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.responders.editing.SaveResponder;
import fitnesse.wiki.NullVersionsController;
import fitnesse.wiki.VersionsController;
import fitnesse.wiki.zip.ZipFileVersionsController;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolType;

public class ComponentFactory {
  private final String endl = System.getProperty("line.separator");
  public static final String PROPERTIES_FILE = "plugins.properties";
  public static final String WIKI_PAGE_FACTORY_CLASS = "WikiPageFactory";
  public static final String PLUGINS = "Plugins";
  public static final String RESPONDERS = "Responders";
  public static final String SYMBOL_TYPES = "SymbolTypes";
  public static final String SLIM_TABLES = "SlimTables";
  public static final String AUTHENTICATOR = "Authenticator";
  public static final String CONTENT_FILTER = "ContentFilter";
  public static final String VERSIONS_CONTROLLER = "VersionsController";
  public static final String DEFAULT_NEWPAGE_CONTENT = "newpage.default.content";
  public static final String THEME = "Theme";

  private final Properties loadedProperties;
  private final String propertiesLocation;
  private boolean propertiesAreLoaded = false;

  public ComponentFactory() {
    this(new Properties());
  }

  public ComponentFactory(String propertiesLocation) {
    this(propertiesLocation, new Properties());
  }

  public ComponentFactory(Properties properties) {
    this.propertiesLocation = null;
    this.loadedProperties = properties;
    propertiesAreLoaded = true;
  }

  public ComponentFactory(String propertiesLocation, Properties properties) {
    this.propertiesLocation = propertiesLocation;
    this.loadedProperties = properties;
    loadProperties(propertiesLocation);
  }

  protected void loadProperties(String propertiesLocation) {
    try {
      String propertiesPath = propertiesLocation + "/" + PROPERTIES_FILE;
      FileInputStream propertiesStream = new FileInputStream(propertiesPath);
      loadedProperties.load(propertiesStream);
    } catch (IOException e) {
      // No properties files means all defaults are loaded
    }
  }

  public Properties getProperties() {
    if (!propertiesAreLoaded) {
      loadProperties(propertiesLocation);
      propertiesAreLoaded = true;
    }
    return loadedProperties;
  }

  public String getProperty(String propertyName) {
    return getProperties().getProperty(propertyName);
  }

  public Object createComponent(String componentType, Class<?> defaultComponent) {
    String componentClassName = loadedProperties.getProperty(componentType);
    Class<?> componentClass;
    try {
      if (componentClassName != null)
        componentClass = Class.forName(componentClassName);
      else
        componentClass = defaultComponent;
  
      if (componentClass != null) {
        try {
          Constructor<?> constructor = componentClass.getConstructor(Properties.class);
          return constructor.newInstance(loadedProperties);
        } catch (NoSuchMethodException e) {
          Constructor<?> constructor = componentClass.getConstructor();
          return constructor.newInstance();
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Unable to instantiate component for type " + componentType, e);
    }
    return null;
  }

  public Object createComponent(String componentType) {
    return createComponent(componentType, null);
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
    String value = loadedProperties.getProperty(propertyName);
    if (value == null)
      return null;
    else
      return value.split(",");
  }

  public Authenticator getAuthenticator(Authenticator defaultAuthenticator) {
    Authenticator authenticator = (Authenticator) createComponent(AUTHENTICATOR);
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
    ContentFilter filter = (ContentFilter) createComponent(CONTENT_FILTER);
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
}
