package fitnesse.plugins;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import fitnesse.Responder;
import fitnesse.authentication.Authenticator;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.testsystems.TestSystemFactory;
import fitnesse.testsystems.slim.CustomComparator;
import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wikitext.parser.SymbolType;

public class PluginFeatureFactoryBase implements PluginFeatureFactory {
  /**
   * @deprecated Override one or more of the specific methods in the interface instead.
   */
  @Deprecated
  @Override
  public List<? extends Object> getPlugins() throws PluginException {
    return createList();
  }

  @Override
  public Map<String, Class<? extends Responder>> getResponders() throws PluginException {
      return createMap();
  }

  @Override
  public Authenticator getAuthenticator() {
    return null;
  }

  @Override
  public List<? extends SymbolType> getSymbolTypes() throws PluginException {
    return createList();
  }

  @Override
  public List<? extends WikiPageFactory> getWikiPageFactories() throws PluginException {
    return createList();
  }

  @Override
  public ContentFilter getContentFilter() {
    return null;
  }

  @Override
  public Map<String, Class<? extends SlimTable>> getSlimTables() throws PluginException {
    return createMap();
  }

  @Override
  public Map<String, ? extends CustomComparator> getCustomComparators() throws PluginException {
    return createMap();
  }

  @Override
  public Map<String, ? extends TestSystemFactory> getTestSystemFactories() throws PluginException {
    return createMap();
  }

  private <T> Map<String, T> createMap() {
    return new LinkedHashMap<String, T>();
  }

  private <T> List<T> createList() {
    return new ArrayList<T>();
  }
}
