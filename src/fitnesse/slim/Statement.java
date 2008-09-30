package fitnesse.slim;

import fitnesse.util.ListUtility;
import static fitnesse.util.ListUtility.list;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Specifies the syntactic operations for a Slim statement.  A Slim statement is a list of strings.
 * The first string is the operation name.  Other strings are arguments of the operation.  This class knows
 * that syntax, and knows how to decompose it into StatementExecutor calls.  This class DOES NOT know how
 * to do any actual execution.  
 */
public class Statement {
  private ArrayList<Object> words = new ArrayList<Object>();

  public Statement(List<Object> statement) {
    for (Object word : statement)
      words.add(word);
  }

  public boolean add(Object s) {
    return words.add(s);
  }

  public boolean addAll(Collection<Object> objects) {
    return words.addAll(objects);
  }

  private boolean operationIs(String operation) {
    return getOperation().equalsIgnoreCase(operation);
  }

  public String getOperation() {
    return getWord(1);
  }

  private String getWord(int word) {
    try {
      return (String) words.get(word);
    } catch (Exception e) {
      throw new SlimError(String.format("Statement missing arguments: %s", toString()));
    }
  }

  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("[");
    for (Object word : words) {
      result.append(word);
      result.append(",");
    }
    int end = result.length() - 1;
    if (result.charAt(end) == ',')
      result.deleteCharAt(end);
    result.append("]");
    return result.toString();
  }

  public Object execute(StatementExecutor executor) {
    Object retval;
    if (operationIs("make"))
      retval =  createInstance(executor);
    else if (operationIs("import"))
      retval =  addPath(executor);
    else if (operationIs("call"))
      retval =  call(executor);
    else if (operationIs("callAndAssign"))
      retval =  callAndAssign(executor);
    else
      retval = SlimServer.EXCEPTION_TAG +  String.format("Invalid statement operation: %s.", getOperation());
    return list(getWord(0), retval);
  }

  private Object addPath(StatementExecutor caller) {
    return caller.addPath(getWord(2));
  }

  private Object createInstance(StatementExecutor caller) {
    String instanceName = getWord(2);
    String className = getWord(3);
    return caller.create(instanceName, className);
  }

  private Object call(StatementExecutor caller) {
    return callMethodAtIndex(caller, 2);
  }

  private Object callMethodAtIndex(StatementExecutor caller, int methodIndex) {
    String instanceName = getWord(methodIndex + 0);
    String methodName = getWord(methodIndex + 1);
    List<Object> argList = words.subList(methodIndex + 2, words.size());
    Object[] args = argList.toArray(new Object[argList.size()]);
    return caller.call(instanceName, methodName, args);
  }

  public Object callAndAssign(StatementExecutor caller) {
    Object result = callMethodAtIndex(caller, 3);
    caller.setVariable(getWord(2), result);
    return result;
  }
}
