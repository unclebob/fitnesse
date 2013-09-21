// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.schedule;

public interface ScheduleItem {
  public boolean shouldRun(long time) throws Exception;

  public void run(long time) throws Exception;
}
