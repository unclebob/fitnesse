package fitnesse.plugins;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fitnesse.ConfigurationParameter;
import fitnesse.Responder;
import fitnesse.authentication.Authenticator;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.testsystems.TestSystemFactory;
import fitnesse.testsystems.slim.CustomComparator;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wikitext.parser.SymbolType;

/**
 * Determines which plugin features to load based on componentFactory's properties (e.g. plugins.properties).
 */
public class PropertyBasedPluginFeatureFactory extends PluginFeatureFactoryBase {

  @Override
  public Map<String, Class<? extends Responder>> getResponders() throws PluginException {
    Map<String, Class<? extends Responder>> responders = super.getResponders();
    addEachNamedClass(ConfigurationParameter.RESPONDERS, responders);
    return responders;
  }

  @Override
  public Authenticator getAuthenticator() {
    return getComponentFactory().createComponent(ConfigurationParameter.AUTHENTICATOR);
  }

  @Override
  public List<SymbolType> getSymbolTypes() throws PluginException {
    return allConfigured(ConfigurationParameter.SYMBOL_TYPES);
  }

  @Override
  public List<WikiPageFactory> getWikiPageFactories() throws PluginException {
    return allConfigured(ConfigurationParameter.WIKI_PAGE_FACTORIES);
  }

  @Override
  public ContentFilter getContentFilter() {
    return getComponentFactory().createComponent(ConfigurationParameter.CONTENT_FILTER);
  }

  @Override
  public Map<String, Class<? extends SlimTable>> getSlimTables() throws PluginException {
    Map<String, Class<? extends SlimTable>> tables = super.getSlimTables();
    addEachNamedClass(ConfigurationParameter.SLIM_TABLES, tables);
    return tables;
  }

  @Override
  public Map<String, CustomComparator> getCustomComparators() throws PluginException {
    Map<String, CustomComparator> ccClasses = super.getCustomComparators();
    addEachNamedObject(ConfigurationParameter.CUSTOM_COMPARATORS, ccClasses);
    return ccClasses;
  }

  @Override
  public Map<String, TestSystemFactory> getTestSystemFactories() throws PluginException {
    Map<String, TestSystemFactory> tsClasses = super.getTestSystemFactories();
    addEachNamedObject(ConfigurationParameter.TEST_SYSTEMS, tsClasses);
    return tsClasses;
  }

  private <T> List<T> allConfigured(ConfigurationParameter propertyName) throws PluginException {
    String[] typeNames = getListFromProperties(propertyName);
    return createComponents(typeNames);
  }

  private <T> void addEachNamedObject(final ConfigurationParameter parameter, Map<String, T> register) throws PluginException {
    String[][] kvList = getKeyValueListFromProperties(parameter);
    for (String[] entry : kvList) {
      String prefix = entry[0];
      String className = entry[1];

      Class<T> componentClass = forName(className);
      T component = createComponent(componentClass);
      register.put(prefix, component);
    }
  }

  private <T> void addEachNamedClass(final ConfigurationParameter parameter, Map<String, Class<? extends T>> register) throws PluginException {
    String[][] kvList = getKeyValueListFromProperties(parameter);
    for (String[] entry : kvList) {
      String prefix = entry[0];
      String className = entry[1];

      Class<T> aClass = forName(className);
      register.put(prefix, aClass);
    }
  }

  private String[][] getKeyValueListFromProperties(final ConfigurationParameter parameter) {
    String[][] result = new String[0][0];
    String[] propList = getListFromProperties(parameter);
    if (propList != null) {
      result = new String[propList.length][2];
      int i = 0;
      for (String entry : propList) {
        entry = entry.trim();
        int colonIndex = entry.lastIndexOf(':');
        String prefix = entry.substring(0, colonIndex);
        String className = entry.substring(colonIndex + 1, entry.length());
        result[i][0] = prefix;
        result[i][1] = className;
        i++;
      }
    }
    return result;
  }

  private String[] getListFromProperties(ConfigurationParameter propertyName) {
    String value = getComponentFactory().getProperty(propertyName.getKey());
    if (value == null)
      return null;
    else
      return value.split(",");
  }

  private <T> List<T> createComponents(String[] typeNames) throws PluginException {
    List<T> result = new ArrayList<T>();
    if (typeNames != null) {
      for (String typeName : typeNames) {
        Class<T> typeClass = forName(typeName.trim());
        T component = createComponent(typeClass);
        result.add(component);
      }
    }
    return result;
  }

  private <T> Class<T> forName(String className) throws PluginException {
    try {
      return (Class<T>) Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new PluginException("Unable to load class " + className, e);
    }
  }
}
