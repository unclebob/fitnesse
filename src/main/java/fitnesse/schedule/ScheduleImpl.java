// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.schedule;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import util.Clock;

public class ScheduleImpl implements Schedule, Runnable {
  private long delay;
  private Thread thread;
  private boolean running;
  private List<ScheduleItem> scheduleItems = Collections.synchronizedList(new LinkedList<ScheduleItem>());

  public ScheduleImpl(long delay) {
    this.delay = delay;
  }

  public void add(ScheduleItem item) {
    scheduleItems.add(item);
  }

  public void start() {
    running = true;
    thread = new Thread(this);
    thread.start();
  }

  public void stop() throws Exception {
    running = false;
    if (thread != null) {
      thread.join();
    }
    thread = null;
  }

  public void run() {
    try {
      while (running) {
        runScheduledItems();
        Thread.sleep(delay);
      }
    }
    catch (Exception e) {
    }
  }

  public void runScheduledItems() throws Exception {
    long time = Clock.currentTimeInMillis();
    synchronized (scheduleItems) {
      for (ScheduleItem item : scheduleItems) {
        runItem(item, time);
      }
    }
  }

  private void runItem(ScheduleItem item, long time) throws Exception {
    try {
      if (item.shouldRun(time))
        item.run(time);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }
}
