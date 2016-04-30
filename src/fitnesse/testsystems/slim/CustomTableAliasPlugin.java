package fitnesse.testsystems.slim;

import java.util.logging.Logger;

import fitnesse.plugins.PluginFeatureFactoryBase;
import fitnesse.slim.test.MyFixture;
import fitnesse.testsystems.slim.tables.SlimTableFactory;

/**
 * This is a test plugin, used by the Acceptance tests suite to demonstrate how custom
 * aliases can be created programmatically.
 */
public class CustomTableAliasPlugin extends PluginFeatureFactoryBase {

  private final static Logger LOG = Logger.getLogger(CustomTableAliasPlugin.class.getName());

  @Override
  public void registerSlimTables(SlimTableFactory slimTableFactory) {
    LOG.info("Creating alias from \"my requirement\" to \"script: " + MyFixture.class.getSimpleName() + "\"");
    slimTableFactory.addAlias("my requirement", MyFixture.class.getSimpleName());
  }

  // We need this function for old-school plugins compatibility:
  public void registerSlimTableFactories(SlimTableFactory slimTableFactory) {
    new CustomTableAliasPlugin().registerSlimTables(slimTableFactory);
  }
}
