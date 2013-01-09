package fitnesse.slim.statement;

import fitnesse.slim.NameTranslator;
import fitnesse.slim.SlimError;
import fitnesse.slim.StatementExecutorInterface;

import java.util.List;

import static java.lang.String.format;

public interface Statement {

  Object execute(StatementExecutorInterface executor);

  public static class Factory {
    public static Statement createStatement(List<Object> words, NameTranslator methodNameTranslator) {
      String id = getWord(words, 0);
      String operation = getWord(words, 1);
      Statement statement = null;

      if (MakeStatement.INSTRUCTION.equalsIgnoreCase(operation)) {
        String instanceName = getWord(words, 2);
        String className = getWord(words, 3);
        Object[] args = makeArgsArray(words, 4);
        statement = new MakeStatement(id, instanceName, className, args);
      } else if (CallAndAssignStatement.INSTRUCTION.equalsIgnoreCase(operation)) {
        String symbolName = getWord(words, 2);
        String instanceName = getWord(words, 3);
        String methodName = getWord(words, 4);
        Object[] args = makeArgsArray(words, 5);
        statement = new CallAndAssignStatement(id, symbolName, instanceName, methodName, args, methodNameTranslator);
      } else if (CallStatement.INSTRUCTION.equalsIgnoreCase(operation)) {
        String instanceName = getWord(words, 2);
        String methodName = getWord(words, 3);
        Object[] args = makeArgsArray(words, 4);
        statement = new CallStatement(id, instanceName, methodName, args, methodNameTranslator);
      } else if (ImportStatement.INSTRUCTION.equalsIgnoreCase(operation)) {
        String path = getWord(words, 2);
        statement = new ImportStatement(id, path);
      } else {
        statement = new InvalidStatement(id, operation);
      }

      return statement;
    }

    private static String getWord(List<Object> words, int word) {
      try {
        return (String) words.get(word);
      } catch (Exception e) {
        throw new SlimError(format("message:<<MALFORMED_INSTRUCTION %s.>>", wordsToString(words)));
      }
    }

    private static Object[] makeArgsArray(List<Object> words, int argsIndex) {
      List<Object> argList = words.subList(argsIndex, words.size());
      Object[] args = argList.toArray(new Object[argList.size()]);
      return args;
    }

    private static String wordsToString(List<Object> words) {
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

  }

}
