// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
namespace fit
{
	public class FailFixture : Fixture
	{
		public override void DoTable(Parse p)
		{
			Wrong(p);
		}
	}
}