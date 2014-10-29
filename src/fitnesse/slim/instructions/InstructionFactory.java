package fitnesse.slim.instructions;

import java.util.List;

import fitnesse.slim.NameTranslator;
import fitnesse.slim.SlimError;
import fitnesse.slim.SlimServer;

import static java.lang.String.format;

public class InstructionFactory {
  private InstructionFactory() {
  }

  public static Instruction createInstruction(List<Object> words, NameTranslator methodNameTranslator) {
    String id = getWord(words, 0);
    String operation = getWord(words, 1);
    Instruction instruction;

    if (MakeInstruction.INSTRUCTION.equalsIgnoreCase(operation)) {
      instruction = createMakeInstruction(id, words);
    } else if (AssignInstruction.INSTRUCTION.equalsIgnoreCase(operation)) {
      instruction = createAssignInstruction(id, words);
    } else if (CallAndAssignInstruction.INSTRUCTION.equalsIgnoreCase(operation)) {
      instruction = createCallAndAssignInstruction(id, words, methodNameTranslator);
    } else if (CallInstruction.INSTRUCTION.equalsIgnoreCase(operation)) {
      instruction = createCallInstruction(id, words, methodNameTranslator);
    } else if (ImportInstruction.INSTRUCTION.equalsIgnoreCase(operation)) {
      instruction = createImportInstruction(id, words);
    } else {
      instruction = createInvalidInstruction(id, operation);
    }

    return instruction;
  }

  private static MakeInstruction createMakeInstruction(String id, List<Object> words) {
    String instanceName = getWord(words, 2);
    String className = getWord(words, 3);
    Object[] args = makeArgsArray(words, 4);
    return new MakeInstruction(id, instanceName, className, args);
  }

  private static AssignInstruction createAssignInstruction(String id, List<Object> words) {
    String symbolName = getWord(words, 2);
    String value = getWord(words, 3);
    return new AssignInstruction(id, symbolName, value);
  }

  private static CallAndAssignInstruction createCallAndAssignInstruction(String id,
                                                                         List<Object> words,
                                                                         NameTranslator methodNameTranslator) {
    String symbolName = getWord(words, 2);
    String instanceName = getWord(words, 3);
    String methodName = getWord(words, 4);
    Object[] args = makeArgsArray(words, 5);
    return new CallAndAssignInstruction(id, symbolName, instanceName, methodName, args, methodNameTranslator);
  }

  private static CallInstruction createCallInstruction(String id, List<Object> words,
                                                       NameTranslator methodNameTranslator) {
    String instanceName = getWord(words, 2);
    String methodName = getWord(words, 3);
    Object[] args = makeArgsArray(words, 4);
    return new CallInstruction(id, instanceName, methodName, args, methodNameTranslator);
  }

  private static ImportInstruction createImportInstruction(String id, List<Object> words) {
    String path = getWord(words, 2);
    return new ImportInstruction(id, path);
  }

  private static InvalidInstruction createInvalidInstruction(String id, String operation) {
    return new InvalidInstruction(id, operation);
  }

  private static String getWord(List<Object> words, int word) {
    try {
      return (String) words.get(word);
    } catch (Exception e) {
      throw new SlimError(format("message:<<%s %s.>>", SlimServer.MALFORMED_INSTRUCTION, wordsToString(words)));
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
