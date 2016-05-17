package fit.decorator.util;

public class DefaultTimer implements Timer {
  private long startTime;

  @Override
  public long elapsed() {
    return (System.currentTimeMillis() - startTime);
  }

  @Override
  public void start() {
    startTime = System.currentTimeMillis();
  }
}
