// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import fitnesse.ConfigurationParameter;

/**
 * Create components for FitNesse.
 *
 * Components have one of the following constructors:
 * <ul>
 *   <li><code>Component(fitnesse.componentsComponentFactory componentFactory)</code></li>
 *   <li><code>Component(java.util.Properties properties)</code></li>
 *   <li><code>Component()</code></li>
 * </ul>
 *
 * Components requested by parameter/type/name are instantiated once for the application.
 */
public class ComponentFactory {

  private final Properties properties;
  private Map<String, Object> components;

  public ComponentFactory(Properties properties) {
    this.properties = properties;
    this.components = new HashMap<>();
  }

  public <T> T createComponent(String componentType, Class<T> defaultComponent) throws ComponentInstantiationException {
    if (components.containsKey(componentType)) {
      return (T) components.get(componentType);
    }

    String componentClassName = properties.getProperty(componentType);
    Class<?> componentClass;
    try {
      if (componentClassName != null)
        componentClass = Class.forName(componentClassName);
      else
        componentClass = defaultComponent;
    } catch (Exception e) {
      throw new ComponentInstantiationException("Unable to look up component for type '" + componentType + "' with classname '" + componentClassName + "'", e);
    }

    if (componentClass != null) {
      T component = (T) createComponent(componentClass);
      components.put(componentType, component);
      return component;
    }
    return null;
  }

  public <T> T createComponent(Class<T> componentClass) throws ComponentInstantiationException {
    try {
      try {
        Constructor<?> constructor = componentClass.getConstructor(ComponentFactory.class);
        return (T) constructor.newInstance(this);
      } catch (NoSuchMethodException e) {
        // no problem, we can deal with some other constructors as well
      }

      try {
        Constructor<?> constructor = componentClass.getConstructor(Properties.class);
        return (T) constructor.newInstance(properties);
      } catch (NoSuchMethodException e) {
        Constructor<?> constructor = componentClass.getConstructor();
        return (T) constructor.newInstance();
      }
    } catch (Exception e) {
      throw new ComponentInstantiationException("Unable to instantiate component for type " + componentClass.getName(), e);
    }
  }

  public <T> T createComponent(ConfigurationParameter componentType, Class<T> defaultComponent) {
    return createComponent(componentType.getKey(), defaultComponent);
  }

  public <T> T createComponent(ConfigurationParameter componentType) {
    return createComponent(componentType, (Class<T>) null);
  }

  public String getProperty(String key) {
    return properties.getProperty(key);
  }

  public String getProperty(String key, String defaultValue) {
    return properties.getProperty(key, defaultValue);
  }
}
