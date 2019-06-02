package fitnesse.slim.instructions;

import fitnesse.slim.NameTranslator;

import java.util.Optional;

public class CallInstruction extends CallAndOptionalAssignInstruction {
  public static final String INSTRUCTION = "call";

  public CallInstruction(String id, String instanceName, String methodName) {
    this(id, instanceName, methodName, new Object[]{});
  }

  public CallInstruction(String id, String instanceName, String methodName, Object[] args) {
    super(INSTRUCTION, id, Optional.empty(), instanceName, methodName, args);
  }

  public CallInstruction(String id, String instanceName, String methodName, Object[] args,
                         NameTranslator methodNameTranslator) {
    this(id, instanceName, methodNameTranslator.translate(methodName), args);
  }
}
