package util;

import java.util.Date;

public class TestTimeKeeper implements TimeKeeper {
  private Date now;

  public TestTimeKeeper(Date now) {
    this.now = new Date(now.getTime());
  }

  public Date getNow() {
    return new Date(now.getTime());
  }
}
