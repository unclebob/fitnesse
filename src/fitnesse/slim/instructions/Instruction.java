package fitnesse.slim.instructions;

import fitnesse.slim.SlimException;
import fitnesse.slim.instructions.SystemExitSecurityManager.SystemExitException;

public abstract class Instruction {
  public static final Instruction NOOP_INSTRUCTION = new Instruction("NOOP") {
    @Override
    protected InstructionResult executeInternal(InstructionExecutor executor) throws SlimException {
      return new InstructionResult.Void(getId());
    }
  };

  private String id;

  public Instruction(String id) {
    this.id = id;
  }

  public String getId() {
    return this.id;
  }

  public final InstructionResult execute(InstructionExecutor executor) {
    SecurityManager oldSecurityManager = System.getSecurityManager();
    System.setSecurityManager(new SystemExitSecurityManager(oldSecurityManager));
    
    InstructionResult result;
    try {
      result = executeInternal(executor);
    } catch (SlimException e) {
      result = new InstructionResult.Error(getId(), e);
    } catch (SystemExitException e) {
      result = new InstructionResult.Error(getId(), e);
    } finally {
      System.setSecurityManager(oldSecurityManager);
    }
    return result;
  }

  protected abstract InstructionResult executeInternal(InstructionExecutor executor) throws SlimException;

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

    return id.equals(that.id);

  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
