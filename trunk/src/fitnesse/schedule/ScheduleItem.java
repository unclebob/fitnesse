// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.schedule;

public interface ScheduleItem
{
	public boolean shouldRun(long time) throws Exception;

	public void run(long time) throws Exception;
}
