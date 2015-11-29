package fitnesse.plugins;

import java.util.Calendar;

import fitnesse.testrunner.TestSystemFactoryRegistry;
import fitnesse.testsystems.slim.CustomComparatorRegistry;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.plugins.slimcoverage.CoverageSlimTestSystemFactory;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.Today;

public class DummyPluginFeatureFactory extends PluginFeatureFactoryBase {
  static final String SLIM_TABLE = "dummySlimTable";
  private SlimTableFactory slimTableFactory;
  private CustomComparatorRegistry customComparatorRegistry;
  private TestSystemFactoryRegistry testSystemFactoryRegistry;

  @Override
  public void registerSymbolTypes(SymbolProvider symbolProvider) throws PluginException {
    symbolProvider.add(new MonthsFromToday2());
  }

  @Override
  public void registerSlimTables(SlimTableFactory slimTableFactory) throws PluginException {
    slimTableFactory.addTableType(SLIM_TABLE, PluginsLoaderTest.TestSlimTable.class);
    this.slimTableFactory = slimTableFactory;
    registerSlimCoverageIfPossible();
  }

  public static class MonthsFromToday2 extends Today {
    public MonthsFromToday2() {
      super("MonthsFromToday2", "!monthsFromToday2", Calendar.MONTH);
    }
  }

  @Override
  public void registerCustomComparators(CustomComparatorRegistry customComparatorRegistry) throws PluginException {
    this.customComparatorRegistry = customComparatorRegistry;
    registerSlimCoverageIfPossible();
  }

  @Override
  public void registerTestSystemFactories(TestSystemFactoryRegistry testSystemFactoryRegistry) throws PluginException {
    this.testSystemFactoryRegistry = testSystemFactoryRegistry;
    registerSlimCoverageIfPossible();
  }

  protected void registerSlimCoverageIfPossible() {
    if (slimTableFactory != null
            && customComparatorRegistry != null
            && testSystemFactoryRegistry != null) {

      testSystemFactoryRegistry
              .registerTestSystemFactory("slimcoverage",
                new CoverageSlimTestSystemFactory(slimTableFactory, customComparatorRegistry));
    }
  }
}
