package fitnesse.slim.instructions;

import fitnesse.slim.NameTranslator;

import java.util.Optional;

public class CallAndAssignInstruction extends CallAndOptionalAssignInstruction {
  public static final String INSTRUCTION = "callAndAssign";

  public CallAndAssignInstruction(String id, String symbolName, String instanceName, String methodName) {
    this(id, symbolName, instanceName, methodName, new Object[]{});
  }

  public CallAndAssignInstruction(String id, String symbolName, String instanceName, String methodName, Object[] args) {
    super(INSTRUCTION, id, Optional.of(symbolName), instanceName, methodName, args);
  }

  public CallAndAssignInstruction(String id, String symbolName, String instanceName, String methodName, Object[] args,
                                  NameTranslator methodNameTranslator) {
    this(id, symbolName, instanceName, methodNameTranslator.translate(methodName), args);
  }
}
