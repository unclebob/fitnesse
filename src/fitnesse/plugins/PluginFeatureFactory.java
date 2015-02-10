package fitnesse.plugins;

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

public interface PluginFeatureFactory {
  /**
   * @deprecated Override one or more of the specific methods in this interface instead.
   */
  @Deprecated
  List<? extends Object> getPlugins() throws PluginException;

  Map<String, Class<? extends Responder>> getResponders() throws PluginException;

  Authenticator getAuthenticator();

  List<? extends SymbolType> getSymbolTypes() throws PluginException;

  List<? extends WikiPageFactory> getWikiPageFactories() throws PluginException;

  ContentFilter getContentFilter();

  Map<String, Class<? extends SlimTable>> getSlimTables() throws PluginException;

  Map<String, ? extends CustomComparator> getCustomComparators() throws PluginException;

  Map<String, ? extends TestSystemFactory> getTestSystemFactories() throws PluginException;
}
