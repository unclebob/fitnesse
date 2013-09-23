// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.schedule;

// Runs scheduled tasks.
public interface Schedule {
  public void add(ScheduleItem item) throws Exception;

  public void start() throws Exception;

  public void stop() throws Exception;
}
