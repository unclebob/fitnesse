package fitnesse.plugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fitnesse.authentication.Authenticator;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.testrunner.TestSystemFactoryRegistry;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wiki.WikiPageFactoryRegistry;
import fitnesse.wikitext.parser.SymbolProvider;

/**
 * Wraps old-style plugins in the new PluginFeatureFactory service.
 */
public class LegacyPluginFeatureFactory implements PluginFeatureFactory {

  private final Object plugin;

  public LegacyPluginFeatureFactory(Object plugin) {
    this.plugin = plugin;
  }

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
    register(plugin, "registerResponders", ResponderFactory.class, responderFactory);
  }

  @Override
  public void registerSymbolTypes(SymbolProvider symbolProvider) throws PluginException {
    register(plugin, "registerSymbolTypes", SymbolProvider.class, symbolProvider);
  }

  @Override
  public void registerWikiPageFactories(WikiPageFactoryRegistry wikiPageFactoryRegistry) throws PluginException {
    register(plugin, "registerWikiPageFactories", WikiPageFactoryRegistry.class, wikiPageFactoryRegistry);
  }

  @Override
  public void registerTestSystemFactories(TestSystemFactoryRegistry testSystemFactoryRegistry) throws PluginException {
    register(plugin, "registerTestSystemFactories", TestSystemFactoryRegistry.class, testSystemFactoryRegistry);

  }

  @Override
  public void registerSlimTables(SlimTableFactory slimTableFactory) throws PluginException {
    register(plugin, "registerSlimTableFactories", SlimTableFactory.class, slimTableFactory);
  }

  @Override
  public void registerCustomComparators(CustomComparatorRegistry customComparatorRegistry) throws PluginException {
    register(plugin, "registerCustomComparatorRegistries", CustomComparatorRegistry.class, customComparatorRegistry);
  }

  private <T> void register(Object plugin, String methodName, Class<T> registrarType, T registrar)
          throws PluginException {
    Method method;
    try {
      method = plugin.getClass().getMethod(methodName, registrarType);
    } catch (NoSuchMethodException e) {
      // ok, no widgets to register in this plugin
      return;
    }

    try {
      method.invoke(plugin, registrar);
    } catch (InvocationTargetException e) {
      throw new PluginException("Unable to execute method " + methodName, e);
    } catch (IllegalAccessException e) {
      throw new PluginException("Unable to execute method " + methodName, e);
    }
  }
}
