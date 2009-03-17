// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.schedule;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import junit.framework.TestCase;

public class ScheduleTest extends TestCase {
  private boolean shouldAddCounters;

  public void testRunsAtIntervals() throws Exception {
    ScheduleImpl schedule = new ScheduleImpl(250);
    Counter counter = new Counter();
    schedule.add(counter);
    schedule.start();
    Thread.sleep(700);
    schedule.stop();
    assertTrue(counter.count >= 3);
  }

  public void testAddingWhileRunning() throws Exception {
    ScheduleImpl schedule = new ScheduleImpl(250);
    Counter counter = new Counter();
    Sleeper sleeper = new Sleeper();
    schedule.add(sleeper);
    schedule.start();
    schedule.add(counter);
    Thread.sleep(400);
    schedule.stop();
    assertTrue(counter.count >= 1);
  }

  public void testLotsOfAddingWhileRunning() throws Exception {
    final ScheduleImpl schedule = new ScheduleImpl(250);
    Runnable adder = new Runnable() {
      public void run() {
        while (shouldAddCounters)
          schedule.add(new Counter());
      }
    };

    shouldAddCounters = true;
    Thread addingThread = new Thread(adder);
    addingThread.start();
    Thread.sleep(1);
    try {
      schedule.runScheduledItems();
      schedule.runScheduledItems();
      schedule.runScheduledItems();
    }
    catch (Exception e) {
      fail("too much!: " + e);
    }
    finally {
      shouldAddCounters = false;
      addingThread.join();
      schedule.stop();
    }
  }

  public void testExceptionDoesNotCrashRun() throws Exception {
    ScheduleImpl schedule = new ScheduleImpl(250);
    Counter counter = new Counter();
    schedule.add(new ExceptionThrower());
    schedule.add(counter);
    PrintStream err = System.err;
    System.setErr(new PrintStream(new ByteArrayOutputStream()));
    schedule.start();
    Thread.sleep(50);
    schedule.stop();
    System.setErr(err);
    assertEquals(1, counter.count);
  }

  static class Counter implements ScheduleItem {
    public int count = 0;

    public void run(long time) throws Exception {
      count++;
    }

    public boolean shouldRun(long time) throws Exception {
      return true;
    }
  }

  static class Sleeper implements ScheduleItem {
    public void run(long time) throws Exception {
      Thread.sleep(100);
    }

    public boolean shouldRun(long time) throws Exception {
      return true;
    }
  }

  static class ExceptionThrower implements ScheduleItem {
    public void run(long time) throws Exception {
      throw new Exception("ScheduleTest.ExceptionThrower throwing a test exception");
    }

    public boolean shouldRun(long time) throws Exception {
      return true;
    }
  }
}
