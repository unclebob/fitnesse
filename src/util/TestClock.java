package util;

public class TestClock extends Clock {
  public long currentTime;
  @Override long currentClockTimeInMillis() {
    return currentTime;
  }
}
