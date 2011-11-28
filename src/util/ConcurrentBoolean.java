package util;

import java.util.Date;

public class ConcurrentBoolean {
    public ConcurrentBoolean() {
        value = false;
    }

    public synchronized boolean isTrue() { return value; }

    public synchronized void set(boolean newValue) {
        value = newValue;
        notifyAll();
    }

    public synchronized void waitFor(boolean newValue) {
        while (value != newValue) {
            try { wait(); }
            catch (InterruptedException e) { /*ok*/ }
        }
    }

    public synchronized boolean waitFor(boolean newValue, long timeOutMilliseconds) {
        long start = new Date().getTime();
        long remainingTimeOut = timeOutMilliseconds;
        while (value != newValue && remainingTimeOut >= 0) {
            try { wait(remainingTimeOut); }
            catch (InterruptedException e) { /*ok*/ }
            if (timeOutMilliseconds > 0) {
                remainingTimeOut = timeOutMilliseconds - new Date().getTime() + start;
                if (remainingTimeOut == 0) remainingTimeOut = -1;
            }
        }
        return (value == newValue);
    }

    private volatile boolean value;
}
