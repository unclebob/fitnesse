// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import java.lang.reflect.Constructor;
import java.util.Properties;

public class ComponentFactory {

  public static final String WIKI_PAGE_FACTORY_CLASS = "WikiPageFactory";
  public static final String PLUGINS = "Plugins";
  public static final String RESPONDERS = "Responders";
  public static final String SYMBOL_TYPES = "SymbolTypes";
  public static final String SLIM_TABLES = "SlimTables";
  public static final String AUTHENTICATOR = "Authenticator";
  public static final String CUSTOM_COMPARATORS = "CustomComparators";
  public static final String CONTENT_FILTER = "ContentFilter";
  public static final String VERSIONS_CONTROLLER_CLASS = "VersionsController";
  public static final String VERSIONS_CONTROLLER_DAYS = VERSIONS_CONTROLLER_CLASS + ".days";
  public static final String RECENT_CHANGES_CLASS = "RecentChanges";

  private final Properties properties;

  public ComponentFactory(Properties properties) {
    this.properties = properties;
  }


  public Object createComponent(String componentType, Class<?> defaultComponent) {
    String componentClassName = properties.getProperty(componentType);
    Class<?> componentClass;
    try {
      if (componentClassName != null)
        componentClass = Class.forName(componentClassName);
      else
        componentClass = defaultComponent;
  
      if (componentClass != null) {
        try {
          Constructor<?> constructor = componentClass.getConstructor(Properties.class);
          return constructor.newInstance(properties);
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

  public String getProperty(String propertyName) {
    return properties.getProperty(propertyName);
  }
}
