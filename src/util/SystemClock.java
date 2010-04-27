package util;

import java.util.Date;

public class SystemClock implements Clock {
    public static Clock instance = new SystemClock();
    public static Date now() { return instance.getNow(); }
    public Date getNow() {  return new Date(); }
}
