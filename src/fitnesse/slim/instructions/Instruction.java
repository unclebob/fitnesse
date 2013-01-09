package fitnesse.slim.instructions;

public interface Instruction<T extends InstructionExecutor> {
  Object execute(T executor);
}
