package util;

import java.util.Date;

public class TestClock implements Clock {
    private Date now;
    public TestClock(Date now) { this.now = now; }
    public Date getNow() { return now; }
}
