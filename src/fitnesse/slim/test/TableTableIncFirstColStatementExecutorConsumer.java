package fitnesse.slim.test;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fitnesse.slim.StatementExecutorConsumer;
import fitnesse.slim.StatementExecutorInterface;

public class TableTableIncFirstColStatementExecutorConsumer implements StatementExecutorConsumer {

  private static final Pattern SYMBOL_ASSIGNMENT_PATTERN = Pattern.compile("\\A\\s*\\$(\\w+)\\s*=\\s*\\Z");
  private static final Pattern SYMBOL_PATTERN = Pattern.compile("\\$([A-Za-z]\\w*)");
  
  private StatementExecutorInterface context;

  /*
   * (non-Javadoc)
   * @see fitnesse.slim.StatementExecutorConsumer#setStatementExecutor(fitnesse.slim.StatementExecutorInterface)
   */
  public void setStatementExecutor(StatementExecutorInterface statementExecutor) {
    this.context = statementExecutor;
  }

  public List<List<String>> doTable(List<List<?>> table) {
    List<List<String>> ret = new ArrayList<List<String>>();

    for (List<?> line : table) {
      List<String> retLine = new ArrayList<String>();
      ret.add(retLine);

      retLine.add("no change");
      String oldValue = replaceSymbolsInString(line.get(0).toString());
	  int newValue = Integer.parseInt(oldValue) + 1;
	  assignSymbolIfApplicable(line.get(1).toString(), newValue);
	  retLine.add("pass:" + newValue);
    }

    return ret;
  }

  private String replaceSymbolsInString(String arg) {
    int startingPosition = 0;
    while (true) {
      if ("".equals(arg) || null == arg) {
        break;
      }
      Matcher symbolMatcher = SYMBOL_PATTERN.matcher(arg);
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
  
  private String replaceSymbolInArg(Matcher symbolMatcher, String arg, String symbolName) {
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
    String symbol = ifSymbolAssignment(text);
    if (symbol != null) {
      context.assign(symbol, value);
    }
  }
  
  private String ifSymbolAssignment(String expected) {
    Matcher matcher = SYMBOL_ASSIGNMENT_PATTERN.matcher(expected);
    return matcher.find() ? matcher.group(1) : null;
  }
  
}
