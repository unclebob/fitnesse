package fitnesse.plugins;

import fitnesse.authentication.Authenticator;
import fitnesse.reporting.FormatterRegistry;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.testrunner.TestSystemFactoryRegistry;
import fitnesse.testrunner.run.TestRunFactoryRegistry;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wiki.WikiPageFactoryRegistry;
import fitnesse.wikitext.MarkUpSystems;
import fitnesse.wikitext.parser.SymbolProvider;

public interface PluginFeatureFactory {

  default Authenticator getAuthenticator() {
    return null;
  }

  default ContentFilter getContentFilter() {
    return null;
  }

  default String getDefaultTheme() {
    return null;
  }

  default void registerResponders(ResponderFactory responderFactory) throws PluginException {
  }

  default void registerSymbolTypes(SymbolProvider symbolProvider) throws PluginException {
  }

  default void registerWikiPageFactories(WikiPageFactoryRegistry wikiPageFactoryRegistry) throws PluginException {
  }

  default void registerFormatters(FormatterRegistry registrar) throws PluginException {
  }

  default void registerTestSystemFactories(TestSystemFactoryRegistry testSystemFactoryRegistry) throws PluginException {
  }

  default void registerSlimTables(SlimTableFactory slimTableFactory) throws PluginException {
  }

  default void registerCustomComparators(CustomComparatorRegistry customComparatorRegistry) throws PluginException {
  }

  default void registerTestRunFactories(TestRunFactoryRegistry runFactoryRegistry) throws PluginException {
  }

  default void registerMarkupSystems(MarkUpSystems systems) {}
}
