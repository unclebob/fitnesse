package fitnesse.slim;

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
  private ArrayList<String> words = new ArrayList<String>();

  public Statement(List<String> statement) {
    for (String word : statement)
      words.add(word);
  }

  public boolean add(String s) {
    return words.add(s);
  }

  public boolean addAll(Collection<String> strings) {
    return words.addAll(strings);
  }

  private boolean operationIs(String operation) {
    return getOperation().equalsIgnoreCase(operation);
  }

  public String getOperation() {
    return getWord(0);
  }

  private String getWord(int word) {
    try {
      return words.get(word);
    } catch (IndexOutOfBoundsException e) {
      throw new SlimError(String.format("Statement missing arguments: %s", toString()));
    }
  }

  public String toString() {
    StringBuffer result = new StringBuffer();
    result.append("[");
    for (String word : words) {
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
    if (operationIs("make"))
      return createInstance(executor);
    if (operationIs("import"))
      return addPath(executor);
    if (operationIs("call"))
      return call(executor);
    if (operationIs("callAndAssign"))
      return callAndAssign(executor);
    if (operationIs("describeClass"))
      return describeClass(executor);
    if (operationIs("set"))
      return set(executor);
    if (operationIs("get"))
      return get(executor);
    else
      throw new SlimError(String.format("Invalid statement operation: %s.", getOperation()));
  }

  private Object get(StatementExecutor caller) {
    String instanceName = getWord(1);
    String variableName = getWord(2);
    return caller.get(instanceName, variableName);
  }

  private Object set(StatementExecutor caller) {
    String instanceName = getWord(1);
    String variableName = getWord(2);
    String value = getWord(3);
    return caller.set(instanceName, variableName, value);
  }

  private List<Object> describeClass(StatementExecutor caller) {
    return caller.describeClass(getWord(1));
  }

  private String addPath(StatementExecutor caller) {
    caller.addPath(getWord(1));
    return null;
  }

  private String createInstance(StatementExecutor caller) {
    String instanceName = getWord(1);
    String className = getWord(2);
    caller.create(instanceName, className);
    return null;
  }

  private String call(StatementExecutor caller) {
    return callMethodAtIndex(caller, 1);
  }

  private String callMethodAtIndex(StatementExecutor caller, int methodIndex) {
    String instanceName = getWord(methodIndex + 0);
    String methodName = getWord(methodIndex + 1);
    List<String> argList = words.subList(methodIndex + 2, words.size());
    String[] args = argList.toArray(new String[argList.size()]);
    return caller.call(instanceName, methodName, args);
  }

  public String callAndAssign(StatementExecutor caller) {
    String result = callMethodAtIndex(caller, 2);
    caller.setVariable(getWord(1), result);
    return result;
  }
}
