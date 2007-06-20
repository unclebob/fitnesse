// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.schedule;

import junit.framework.TestCase;

import java.io.*;

public class ScheduleTest extends TestCase
{
	private ScheduleImpl schedule;
	private Counter counter;
	private boolean shouldAddCounters;

	public void setUp() throws Exception
	{
		schedule = new ScheduleImpl(250);
		counter = new Counter();
	}

	public void tearDown() throws Exception
	{
		if(schedule != null)
			schedule.stop();
	}

	public void testRunsAtIntervals() throws Exception
	{
		schedule.add(counter);
		schedule.start();
		Thread.sleep(700);
		schedule.stop();
		assertEquals(3, counter.count);
	}

	public void testAddingWhileRunning() throws Exception
	{
		Sleeper sleeper = new Sleeper();
		schedule.add(sleeper);
		schedule.start();
		schedule.add(counter);
		Thread.sleep(100);
		schedule.stop();
		assertEquals(1, counter.count);
	}

	public void testLotsOfAddingWhileRunning() throws Exception
	{
		Runnable adder = new Runnable()
		{
			public void run()
			{
				while(shouldAddCounters)
					schedule.add(new Counter());
			}
		};

		shouldAddCounters = true;
		Thread addingThread = new Thread(adder);
		addingThread.start();
		Thread.sleep(1);
		try
		{
			schedule.runScheduledItems();
			schedule.runScheduledItems();
			schedule.runScheduledItems();
		}
		catch(Exception e)
		{
			fail("too much!: " + e);
		}
		finally
		{
			shouldAddCounters = false;
			addingThread.join();
			schedule.stop();
		}
	}

	public void testExceptionDoesNotCrashRun() throws Exception
	{
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

	static class Counter implements ScheduleItem
	{
		public int count = 0;

		public void run(long time) throws Exception
		{
			count++;
		}

		public boolean shouldRun(long time) throws Exception
		{
			return true;
		}
	}

	static class Sleeper implements ScheduleItem
	{
		public void run(long time) throws Exception
		{
			Thread.sleep(100);
		}

		public boolean shouldRun(long time) throws Exception
		{
			return true;
		}
	}

	static class ExceptionThrower implements ScheduleItem
	{
		public void run(long time) throws Exception
		{
			throw new Exception("ScheduleTest.ExceptionThrower throwing a test exception");
		}

		public boolean shouldRun(long time) throws Exception
		{
			return true;
		}
	}
}
