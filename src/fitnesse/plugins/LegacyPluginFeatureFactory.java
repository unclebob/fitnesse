package fitnesse.plugins;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import fitnesse.responders.ResponderFactory;
import fitnesse.testrunner.TestSystemFactoryRegistry;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wiki.WikiPageFactoryRegistry;
import fitnesse.wikitext.parser.SymbolProvider;

/**
 * Wraps old-style plugins in the new PluginFeatureFactory service.
 */
public class LegacyPluginFeatureFactory extends PluginFeatureFactoryBase {

  private final Object plugin;

  public LegacyPluginFeatureFactory(Object plugin) {
    this.plugin = plugin;
  }

  @Override
  public void registerResponders(ResponderFactory responderFactory) throws PluginException {
    if (register(plugin, "registerResponders", ResponderFactory.class, responderFactory)) {
      LOG.info("Registered responders from: " + getPluginDescription());
    }
  }

  @Override
  public void registerSymbolTypes(SymbolProvider symbolProvider) throws PluginException {
    if (register(plugin, "registerSymbolTypes", SymbolProvider.class, symbolProvider)) {
      LOG.info("Registered Symbol types from: " + getPluginDescription());
    }
  }

  @Override
  public void registerWikiPageFactories(WikiPageFactoryRegistry wikiPageFactoryRegistry) throws PluginException {
    if (register(plugin, "registerWikiPageFactories", WikiPageFactoryRegistry.class, wikiPageFactoryRegistry)) {
      LOG.info("Registered wiki page factories from: " + getPluginDescription());
    }
  }

  @Override
  public void registerTestSystemFactories(TestSystemFactoryRegistry testSystemFactoryRegistry) throws PluginException {
    if (register(plugin, "registerTestSystemFactories", TestSystemFactoryRegistry.class, testSystemFactoryRegistry)) {
      LOG.info("Registered test system factories from: " + getPluginDescription());
    }
  }

  @Override
  public void registerSlimTables(SlimTableFactory slimTableFactory) throws PluginException {
    if (register(plugin, "registerSlimTableFactories", SlimTableFactory.class, slimTableFactory)) {
      LOG.info("Registered Slim table factories from: " + getPluginDescription());
    }
  }

  @Override
  public void registerCustomComparators(CustomComparatorRegistry customComparatorRegistry) throws PluginException {
    if (register(plugin, "registerCustomComparatorRegistries", CustomComparatorRegistry.class, customComparatorRegistry)) {
      LOG.info("Registered custom comparator registries from: " + getPluginDescription());
    }
  }

  protected String getPluginDescription() {
    return plugin.getClass().getName();
  }

  private <T> boolean register(Object plugin, String methodName, Class<T> registrarType, T registrar)
          throws PluginException {
    Method method;
    try {
      method = plugin.getClass().getMethod(methodName, registrarType);
    } catch (NoSuchMethodException e) {
      // ok, no widgets to register in this plugin
      return false;
    }

    try {
      method.invoke(plugin, registrar);
    } catch (InvocationTargetException | IllegalAccessException e) {
      throw new PluginException("Unable to execute method " + methodName, e);
    }
    return true;
  }
}
