package fitnesse.slim.instructions;

import fitnesse.slim.SlimException;

public class InstructionResult {
  private String id;
  private Object result;

  public InstructionResult(String id, Object result) {
    this.id = id;
    this.result = result;
  }

  public String getId() {
    return id;
  }

  public Object getResult() {
    return result;
  }

  public boolean hasResult() {
    return this.result != null;
  }

  public boolean hasError() {
    return false;
  }

  public static class Ok extends InstructionResult {
    public Ok(String id) {
      super(id, "OK");
    }

    @Override
    public boolean hasResult() {
      return true;
    }

    @Override
    public boolean hasError() {
      return false;
    }
  }

  public static class Void extends InstructionResult {
    public Void(String id) {
      super(id, "/__VOID__/");
    }

    @Override
    public boolean hasResult() {
      return false;
    }

    @Override
    public boolean hasError() {
      return false;
    }
  }

  public static class Error extends InstructionResult {
    public Error(String id, SlimException exception) {
      super(id, exception);
    }
    
    public Error(String id, SecurityException exception) {
      super(id, exception);
    }

    @Override
    public boolean hasResult() {
      return false;
    }

    @Override
    public boolean hasError() {
      return true;
    }
  }
}
