// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.schedule;

// Runs scheduled tasks.
public interface Schedule
{
	public void add(ScheduleItem item) throws Exception;

	public void start() throws Exception;

	public void stop() throws Exception;
}
