package fitnesse.slim;

import fitnesse.slim.converters.VoidConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * executes a list of SLIM statements, and returns a list of return values.
 */
public class ListExecutor {
  private StatementExecutor executor;

  public ListExecutor() {
    this.executor = new StatementExecutor();
  }

  public List<Object> execute(List<Object> statements) {
    List<Object> result = new ArrayList<Object>();
    for (Object statement : statements) {
      Object retVal = new Statement((List<Object>)statement).execute(executor);
      //todo delete this if statement.
      if (retVal != null && !retVal.equals(VoidConverter.voidTag))
        result.add(retVal);
    }
    return result;
  }
}
