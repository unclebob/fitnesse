package fitnesse.slim;

public class SlimError extends Error {
  public SlimError(String s) {
    super(s);
  }

  public SlimError(String s, Throwable throwable) {
    super(s, throwable);
  }

  public SlimError(Throwable e) {
    this(e.getClass().getName() + " " + e.getMessage());
  }
}
