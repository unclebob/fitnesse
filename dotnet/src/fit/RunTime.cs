// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;

namespace fit
{
	public class RunTime
	{
		DateTime start = DateTime.Now;
		TimeSpan elapsed = new TimeSpan(0);

		public override string ToString() 
		{
			elapsed = (DateTime.Now - start);
			if (elapsed.TotalMilliseconds > 600000.0)
			{
				return d(3600000)+":"+d(600000)+d(60000)+":"+d(10000)+d(1000);
			} 
			else 
			{
				return d(60000)+":"+d(10000)+d(1000)+"."+d(100)+d(10);
			}
		}

		protected internal virtual string d(long scale)
		{
			long report = (long)Math.Floor(elapsed.TotalMilliseconds / (double)scale);
			long remaining = (long)Math.Floor(elapsed.TotalMilliseconds - (double)(report * scale));
			elapsed = new TimeSpan(remaining * 10000); // 1ms = 10000ticks
			return report.ToString();
		}
	}
}