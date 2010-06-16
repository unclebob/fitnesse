package util;

import java.util.Date;

public class SystemTimeKeeper implements TimeKeeper {
    public static TimeKeeper instance = new SystemTimeKeeper();
    public static Date now() { return instance.getNow(); }
    public Date getNow() {  return new Date(); }
}
