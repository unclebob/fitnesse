// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import java.lang.reflect.Constructor;
import java.util.Properties;

import fitnesse.ConfigurationParameter;

public class ComponentFactory {

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

  public Object createComponent(ConfigurationParameter componentType, Class<?> defaultComponent) {
    return createComponent(componentType.getKey(), defaultComponent);
  }

  public Object createComponent(ConfigurationParameter componentType) {
    return createComponent(componentType, null);
  }
}
