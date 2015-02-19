package fitnesse.plugins;

import fitnesse.authentication.Authenticator;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.testrunner.TestSystemFactoryRegistry;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wiki.WikiPageFactoryRegistry;
import fitnesse.wikitext.parser.SymbolProvider;

public class PluginFeatureFactoryBase implements PluginFeatureFactory {
  protected final java.util.logging.Logger LOG = java.util.logging.Logger.getLogger(getClass().getName());

  @Override
  public Authenticator getAuthenticator() {
    return null;
  }

  @Override
  public ContentFilter getContentFilter() {
    return null;
  }

  @Override
  public void registerResponders(ResponderFactory responderFactory) throws PluginException {

  }

  @Override
  public void registerSymbolTypes(SymbolProvider symbolProvider) throws PluginException {

  }

  @Override
  public void registerWikiPageFactories(WikiPageFactoryRegistry wikiPageFactoryRegistry) throws PluginException {

  }

  @Override
  public void registerTestSystemFactories(TestSystemFactoryRegistry testSystemFactoryRegistry) throws PluginException {

  }

  @Override
  public void registerSlimTables(SlimTableFactory slimTableFactory) throws PluginException {

  }

  @Override
  public void registerCustomComparators(CustomComparatorRegistry customComparatorRegistry) throws PluginException {

  }
}
