package fitnesse.slim.statement;

import java.util.List;

import fitnesse.slim.NameTranslator;
import fitnesse.slim.SlimError;

import static java.lang.String.format;

public class StatementFactory {
  private StatementFactory() {
  }

  public static Statement createStatement(List<Object> words, NameTranslator methodNameTranslator) {
    String id = getWord(words, 0);
    String operation = getWord(words, 1);
    Statement statement;

    if (MakeStatement.INSTRUCTION.equalsIgnoreCase(operation)) {
      statement = createMakeStatement(id, operation, words);
    } else if (CallAndAssignStatement.INSTRUCTION.equalsIgnoreCase(operation)) {
      statement = createCallAndAssignStatement(id, operation, words, methodNameTranslator);
    } else if (CallStatement.INSTRUCTION.equalsIgnoreCase(operation)) {
      statement = createCallStatement(id, operation, words, methodNameTranslator);
    } else if (ImportStatement.INSTRUCTION.equalsIgnoreCase(operation)) {
      statement = createImportStatement(id, operation, words);
    } else {
      statement = createInvalidStatement(id, operation, words);
    }

    return statement;
  }

  private static MakeStatement createMakeStatement(String id, String operation, List<Object> words) {
    String instanceName = getWord(words, 2);
    String className = getWord(words, 3);
    Object[] args = makeArgsArray(words, 4);
    return new MakeStatement(id, instanceName, className, args);
  }

  private static CallAndAssignStatement createCallAndAssignStatement(String id, String operation, List<Object> words,
                                                                     NameTranslator methodNameTranslator) {
    String symbolName = getWord(words, 2);
    String instanceName = getWord(words, 3);
    String methodName = getWord(words, 4);
    Object[] args = makeArgsArray(words, 5);
    return new CallAndAssignStatement(id, symbolName, instanceName, methodName, args, methodNameTranslator);
  }

  private static CallStatement createCallStatement(String id, String operation, List<Object> words,
                                                   NameTranslator methodNameTranslator) {
    String instanceName = getWord(words, 2);
    String methodName = getWord(words, 3);
    Object[] args = makeArgsArray(words, 4);
    return new CallStatement(id, instanceName, methodName, args, methodNameTranslator);
  }

  private static ImportStatement createImportStatement(String id, String operation, List<Object> words) {
    String path = getWord(words, 2);
    return new ImportStatement(id, path);
  }

  private static InvalidStatement createInvalidStatement(String id, String operation, List<Object> words) {
    return new InvalidStatement(id, operation);
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
    if (result.charAt(end) == ',') {
      result.deleteCharAt(end);
    }
    result.append("]");
    return result.toString();
  }
}
