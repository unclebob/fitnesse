package fitnesse.slim;

import java.util.ArrayList;
import java.util.List;

/**
 * executes a list of SLIM statements, and returns a list of return values.
 */
public class ListExecutor {
  private List<Object> statements;
  private StatementExecutor caller;

  public static List<Object> execute(List<Object> list) {
    ListExecutor executor = new ListExecutor(list);
    return executor.execute();
  }

  public ListExecutor(List<Object> statements) {
    this.statements = statements;
    caller = new StatementExecutor();
  }

  public List<Object> execute() {
    List<Object> result = new ArrayList<Object>();
    for (Object statement : statements) {
      Object retVal = new Statement((List<String>)statement).execute(caller);
      if (retVal != null)
        result.add(retVal);
    }
    return result;
  }
}
