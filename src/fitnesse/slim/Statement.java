// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import static util.ListUtility.list;

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
  private NameTranslator methodNameTranslator;
  
  public Statement(List<Object> statement, NameTranslator methodNameTranslator) {
    this.methodNameTranslator = methodNameTranslator;
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
      throw new SlimError(String.format("message:<<MALFORMED_INSTRUCTION %s.>>", toString()));
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

  public Object execute(StatementExecutorInterface executor) {
    Object retval;

    if (operationIs("make"))
      retval = createInstance(executor);
    else if (operationIs("import"))
      retval = addPath(executor);
    else if (operationIs("call"))
      retval = call(executor);
    else if (operationIs("callAndAssign"))
      retval = callAndAssign(executor);
    else
      retval = SlimServer.EXCEPTION_TAG + String.format("message:<<INVALID_STATEMENT: %s.>>", getOperation());
    return list(getWord(0), retval);
  }

  private Object addPath(StatementExecutorInterface caller) {
    return caller.addPath(getWord(2));
  }

  private Object createInstance(StatementExecutorInterface caller) {
    String instanceName = getWord(2);
    String className = getWord(3);
    Object[] args = makeArgsArray(4);
    return caller.create(instanceName, className, args);
  }

  private Object call(StatementExecutorInterface caller) {
    return callMethodAtIndex(caller, 2);
  }

  private Object callMethodAtIndex(StatementExecutorInterface caller, int methodIndex) {
    String instanceName = getWord(methodIndex + 0);
    String methodName = methodNameTranslator.translate(getWord(methodIndex + 1));
    Object[] args = makeArgsArray(methodIndex + 2);
    return caller.call(instanceName, methodName, args);
  }

  private Object[] makeArgsArray(int argsIndex) {
    List<Object> argList = words.subList(argsIndex, words.size());
    Object[] args = argList.toArray(new Object[argList.size()]);
    return args;
  }

  public Object callAndAssign(StatementExecutorInterface caller) {
    String instanceName = getWord(3);
    String methodName = methodNameTranslator.translate(getWord(4));
    Object[] args = makeArgsArray(5);
    return caller.callAndAssign(getWord(2), instanceName, methodName, args);
  }
}
