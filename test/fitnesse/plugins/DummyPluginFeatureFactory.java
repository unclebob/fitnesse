package fitnesse.plugins;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import fitnesse.testsystems.slim.tables.SlimTable;
import fitnesse.wikitext.parser.SymbolType;
import fitnesse.wikitext.parser.Today;

public class DummyPluginFeatureFactory extends PluginFeatureFactoryBase {
  static final String SLIM_TABLE = "dummySlimTable";

  @Override
  public Map<String, Class<? extends SlimTable>> getSlimTables() throws PluginException {
    Map<String, Class<? extends SlimTable>> map = super.getSlimTables();
    map.put(SLIM_TABLE, PluginsLoaderTest.TestSlimTable.class);
    return map;
  }

  @Override
  public List<? extends SymbolType> getSymbolTypes() throws PluginException {
    return Arrays.asList(new MonthsFromToday2());
  }

  public static class MonthsFromToday2 extends Today {
    public MonthsFromToday2() {
      super("MonthsFromToday2", "!monthsFromToday2", Calendar.MONTH);
    }
  }

}
