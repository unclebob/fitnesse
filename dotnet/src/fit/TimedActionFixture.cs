// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002, 2003 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

using System;

namespace fit 
{
	public class TimedActionFixture : ActionFixture 
	{
		private string format = "hh:mm:ss fff";

		// Traversal ////////////////////////////////

		public override void DoTable(Parse table) 
		{
			base.DoTable(table);
			table.Parts.Parts.Last.More = td("start");
			table.Parts.Parts.Last.More = td("split");
		}

		public override void DoCells(Parse cells)
		{
			DateTime startTime = this.Time();
			base.DoCells(cells);
			DateTime endTime = this.Time();
			long splitMilliseconds = (endTime.Ticks - startTime.Ticks)/10000;
			decimal splitSeconds = new Decimal(splitMilliseconds)/1000;
			cells.Last.More = this.td(startTime.ToString(this.format));
			cells.Last.More = this.td(splitSeconds.ToString("0.000"));
		}

		// Utility //////////////////////////////////

		public virtual DateTime Time()
		{
			return DateTime.Now;
		}

		public virtual Parse td (String body)
		{
			return new Parse("td", Gray(body), null, null);
		}
	}
}