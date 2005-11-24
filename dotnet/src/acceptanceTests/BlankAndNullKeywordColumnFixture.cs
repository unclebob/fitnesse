// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using fit;

namespace fitnesse.acceptanceTests
{
	public class BlankAndNullKeywordRowFixture : RowFixture
	{
		public override object[] Query()
		{
			StringFixture fixture1 = new StringFixture();
			fixture1.Field = null;
			fixture1.Property = null;
			fixture1.Set(null);
			StringFixture fixture2 = new StringFixture();
			fixture2.Field = "";
			fixture2.Property = "";
			fixture2.Set("");
			StringFixture fixture3 = new StringFixture();
			fixture3.Field = "Joe";
			fixture3.Property = "Joe";
			fixture3.Set("Joe");
			return new object[]{fixture1, fixture2, fixture3};
		}

		public override Type GetTargetClass()
		{
			return typeof(StringFixture);
		}
	}
}
