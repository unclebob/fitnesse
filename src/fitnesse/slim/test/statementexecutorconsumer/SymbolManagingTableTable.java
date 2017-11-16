package fitnesse.slim.test.statementexecutorconsumer;

import fitnesse.slim.SlimSymbol;
import fitnesse.slim.StatementExecutor;
import fitnesse.slim.StatementExecutorConsumer;
import fitnesse.slim.StatementExecutorInterface;

import java.util.List;
import java.util.regex.Matcher;

/**
 * Base class for TableTables wanting do their own symbol management.
 */
public abstract class SymbolManagingTableTable implements StatementExecutorConsumer {
  private StatementExecutorInterface context;

  @Override
  public void setStatementExecutor(StatementExecutorInterface statementExecutor) {
    context = statementExecutor;

    // Tell Slim Agent that the fixture takes care of symbol replacements in all "doTable" methods
    // IMPORTANT: Don't forget to set this back to null at the end of your fixture code
    context.assign(StatementExecutor.SLIM_AGENT_FIXTURE_HANDLES_SYMBOLS,"tableTable.*\\.doTable");
  }
  
  protected StatementExecutorInterface getStatementExecutor() {
    return context;
  }

  public List<List<String>> doTable(List<List<String>> table) {
    try {
      return doTableImpl(table);
    } finally {
      // IMPORTANT: Switch symbol replacement on again
      //            or you get bad surprises
      context.assign(StatementExecutor.SLIM_AGENT_FIXTURE_HANDLES_SYMBOLS, null);
    }
  }

  /**
   * Override this method to perform actual table table work.
   * @param table wiki content.
   * @return result table.
   */
  protected abstract List<List<String>> doTableImpl(List<List<String>> table);

  protected String replaceSymbolsInString(String arg) {
    int startingPosition = 0;
    while (true) {
      if ("".equals(arg) || null == arg) {
        break;
      }
      Matcher symbolMatcher = SlimSymbol.SYMBOL_PATTERN.matcher(arg);
      if (symbolMatcher.find(startingPosition)) {
        arg = replaceSymbolInArg(symbolMatcher, arg);
        startingPosition = symbolMatcher.start(1);
      } else {
        break;
      }
    }
    return arg;
  }

  protected String replaceSymbolInArg(Matcher symbolMatcher, String arg) {
    String replacement = "null";
    Object value = getSymbolValue(symbolMatcher);
    if (value != null) {
      replacement = value.toString();
    }
    String prefix = arg.substring(0, symbolMatcher.start());
    String postfix = arg.substring(symbolMatcher.end());
    arg = prefix + replacement + postfix;
    return arg;
  }

  protected Object getSymbolValue(Matcher symbolMatcher) {
    String symbolName = symbolMatcher.group(1);
    return context.getSymbolObject(symbolName);
  }

  protected boolean assignSymbolIfApplicable(String text, Object value) {
    String symbol = SlimSymbol.isSymbolAssignment(text);
    boolean result = symbol != null;
    if (result) {
      context.assign(symbol, value);
    }
    return result;
  }
}
