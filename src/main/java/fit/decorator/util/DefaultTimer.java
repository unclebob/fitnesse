package fit.decorator.util;

public class DefaultTimer implements Timer {
  private long startTime;

  public long elapsed() {
    return (System.currentTimeMillis() - startTime);
  }

  public void start() {
    startTime = System.currentTimeMillis();
  }
}
