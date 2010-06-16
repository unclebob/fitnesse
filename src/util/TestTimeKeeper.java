package util;

import java.util.Date;

public class TestTimeKeeper implements TimeKeeper {
    private Date now;
    public TestTimeKeeper(Date now) { this.now = now; }
    public Date getNow() { return now; }
}
