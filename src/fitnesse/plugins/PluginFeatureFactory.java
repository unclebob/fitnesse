package fitnesse.plugins;

import fitnesse.authentication.Authenticator;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.testrunner.TestSystemFactoryRegistry;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wiki.WikiPageFactoryRegistry;
import fitnesse.wikitext.parser.SymbolProvider;

public interface PluginFeatureFactory {

  Authenticator getAuthenticator();

  ContentFilter getContentFilter();

  void registerResponders(ResponderFactory responderFactory) throws PluginException;

  void registerSymbolTypes(SymbolProvider symbolProvider) throws PluginException;

  void registerWikiPageFactories(WikiPageFactoryRegistry wikiPageFactoryRegistry) throws PluginException;

  void registerTestSystemFactories(TestSystemFactoryRegistry testSystemFactoryRegistry) throws PluginException;

  void registerSlimTables(SlimTableFactory slimTableFactory) throws PluginException;

  void registerCustomComparators(CustomComparatorRegistry customComparatorRegistry) throws PluginException;

}
