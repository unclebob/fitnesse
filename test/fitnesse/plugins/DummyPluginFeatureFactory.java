package fitnesse.plugins;

import java.util.Calendar;

import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.Today;

public class DummyPluginFeatureFactory extends PluginFeatureFactoryBase {
  static final String SLIM_TABLE = "dummySlimTable";

  @Override
  public void registerSymbolTypes(SymbolProvider symbolProvider) throws PluginException {
    symbolProvider.add(new MonthsFromToday2());
  }

  @Override
  public void registerSlimTables(SlimTableFactory slimTableFactory) throws PluginException {
    slimTableFactory.addTableType(SLIM_TABLE, PluginsLoaderTest.TestSlimTable.class);
  }

  public static class MonthsFromToday2 extends Today {
    public MonthsFromToday2() {
      super("MonthsFromToday2", "!monthsFromToday2", Calendar.MONTH);
    }
  }

}
