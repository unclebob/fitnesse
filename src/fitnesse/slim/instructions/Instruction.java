package fitnesse.slim.instructions;

import fitnesse.slim.SlimException;
import fitnesse.slim.SlimServer;

import static java.lang.String.format;
import static util.ListUtility.list;

public abstract class Instruction<T extends InstructionExecutor> {
  public static Instruction NOOP_INSTRUCTION = new Instruction("NOOP") {
    @Override
    protected Object executeInternal(InstructionExecutor executor) throws SlimException {
      return null;
    }
  };

  private String id;

  public Instruction(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public Object execute(T executor) {
    Object result;
    try {
      result = executeInternal(executor);
    } catch (SlimException e) {
      result = format("%s%s", SlimServer.EXCEPTION_TAG, e.getMessage());
    }
    return list(id, result);
  }

  protected abstract Object executeInternal(T executor) throws SlimException;


  @Override
  public String toString() {
    final StringBuffer sb = new StringBuffer();
    sb.append("Instruction");
    sb.append("{id='").append(id).append('\'');
    sb.append('}');
    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Instruction)) return false;

    Instruction that = (Instruction) o;

    if (!id.equals(that.id)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
