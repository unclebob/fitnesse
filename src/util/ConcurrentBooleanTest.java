package util;

import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConcurrentBooleanTest {
    @Test
    public void waitsForNewValue() {
        final ConcurrentBoolean waitFlag = new ConcurrentBoolean();
        final ConcurrentBoolean resultFlag = new ConcurrentBoolean();
        Thread waitingThread = new Thread(new Runnable() {
            public void run() {
                waitFlag.waitFor(true);
                resultFlag.set(true);
            }
        });
        waitingThread.start();
        waitFlag.set(true);
        resultFlag.waitFor(true);
        assertTrue(resultFlag.isTrue());
    }

    @Test
    public void timesOutWaitingForNewValue() {
        final ConcurrentBoolean waitFlag = new ConcurrentBoolean();
        final ConcurrentBoolean resultFlag = new ConcurrentBoolean();
        Thread waitingThread = new Thread(new Runnable() {
            public void run() {
                waitFlag.waitFor(true);
            }
        });
        waitingThread.start();
        waitFlag.set(true);
        assertFalse(resultFlag.waitFor(true, 100L));
    }

    @Test
    public void waitsForNewValueWithTimeOut() {
        final ConcurrentBoolean waitFlag = new ConcurrentBoolean();
        final ConcurrentBoolean resultFlag = new ConcurrentBoolean();
        Thread waitingThread = new Thread(new Runnable() {
            public void run() {
                waitFlag.waitFor(true);
                resultFlag.set(true);
            }
        });
        waitingThread.start();
        waitFlag.set(true);
        assertTrue(resultFlag.waitFor(true, 100L));
    }
}
