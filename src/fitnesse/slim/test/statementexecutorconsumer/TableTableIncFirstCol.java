package fitnesse.slim.test.statementexecutorconsumer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import fitnesse.slim.SlimSymbol;
import fitnesse.slim.StatementExecutor;
import fitnesse.slim.StatementExecutorConsumer;
import fitnesse.slim.StatementExecutorInterface;

public class TableTableIncFirstCol implements StatementExecutorConsumer {


  private StatementExecutorInterface context;

  /*
   * (non-Javadoc)
   *
   * @see
   * fitnesse.slim.StatementExecutorConsumer#setStatementExecutor(fitnesse.slim
   * .StatementExecutorInterface)
   */
  @Override
  public void setStatementExecutor(StatementExecutorInterface statementExecutor) {
    this.context = statementExecutor;

    // Tell Slim Agent that the fixture takes care of symbol replacements in all "doTable" methods
    // IMPORTANT: Don't forget to set this back to null at the end of your fixture code
    this.context.assign(StatementExecutor.SLIM_AGENT_FIXTURE_HANDLES_SYMBOLS,"tableTable.*\\.doTable");
  }

  public List<List<String>> doTable(List<List<?>> table) {
    List<List<String>> ret = new ArrayList<>();
    try {


      for (List<?> line : table) {
        List<String> retLine = new ArrayList<>();
        ret.add(retLine);

        retLine.add("no change");
        String oldValue = replaceSymbolsInString(line.get(0).toString());
        int newValue = Integer.parseInt(oldValue) + 1;
        assignSymbolIfApplicable(line.get(1).toString(), newValue);
        retLine.add("pass:" + newValue);
      }

    } finally {
      // IMPORTANT: Switch symbol replacement on again
      //            or you get bad surprises
      this.context.assign(StatementExecutor.SLIM_AGENT_FIXTURE_HANDLES_SYMBOLS, null);

    }
    return ret;
  }

  private String replaceSymbolsInString(String arg) {
    int startingPosition = 0;
    while (true) {
      if ("".equals(arg) || null == arg) {
        break;
      }
      Matcher symbolMatcher = SlimSymbol.SYMBOL_PATTERN.matcher(arg);
      if (symbolMatcher.find(startingPosition)) {
        String symbolName = symbolMatcher.group(1);
        arg = replaceSymbolInArg(symbolMatcher, arg, symbolName);
        startingPosition = symbolMatcher.start(1);
      } else {
        break;
      }
    }
    return arg;
  }

  private String replaceSymbolInArg(Matcher symbolMatcher, String arg,
      String symbolName) {
    String replacement = "null";
    Object value = context.getSymbol(symbolName);
    if (value != null) {
      replacement = value.toString();
    }
    arg = arg.substring(0, symbolMatcher.start()) + replacement
        + arg.substring(symbolMatcher.end());
    return arg;
  }

  private void assignSymbolIfApplicable(String text, int value) {
    String symbol = SlimSymbol.isSymbolAssignment(text);
    if (symbol != null) {
      context.assign(symbol, value);
    }
  }

}
