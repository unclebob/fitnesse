package fitnesse.testsystems.slim;

import fitnesse.plugins.PluginFeatureFactoryBase;
import fitnesse.slim.test.MyFixture;
import fitnesse.testsystems.slim.tables.SlimTableFactory;
import fitnesse.wikitext.parser.Symbol;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.Table;
import fitnesse.wikitext.VariableSource;
import fitnesse.wikitext.parser.decorator.ParsedSymbolDecorator;

import java.util.logging.Logger;

import static fitnesse.wikitext.parser.decorator.SymbolClassPropertyAppender.classPropertyAppender;
import static fitnesse.wikitext.parser.decorator.SymbolInspector.inspect;

/**
 * Test plugin used in Acceptance tests suite showing how custom symbol rendering can be plugged in.
 */
public class CustomScriptTableRenderingPlugin extends PluginFeatureFactoryBase {

  private static final Logger LOG = Logger.getLogger(CustomScriptTableRenderingPlugin.class.getName());

  @Override
  public void registerSymbolTypes(SymbolProvider symbolProvider) {
    TableSymbolDecorator.install();
  }

  @Override
  public void registerSlimTables(SlimTableFactory slimTableFactory) {
    LOG.info("Creating alias from \"my use case\" to \"script:" + MyFixture.class.getSimpleName() + "\"");
    slimTableFactory.addAlias("my use case", MyFixture.class.getSimpleName());
  }

  private static class TableSymbolDecorator implements ParsedSymbolDecorator {
    static void install() {
      Table.symbolType.addDecorator(new TableSymbolDecorator());
    }

    @Override
    public void handleParsedSymbol(Symbol table, VariableSource variableSource) {
      Symbol firstCell = table.getChildren()
                              .get(0)
                              .getChildren()
                              .get(0);
      String firstCellContent = inspect(firstCell).getRawContent();
      if (firstCellContent.contains("script") && firstCellContent.contains("my use case")) {
        classPropertyAppender().addPropertyValue(table, "myUseCase");
      }
    }
  }
}
