// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;

namespace fitnesse.acceptanceTests
{
	public class ExecuteExampleFixture : ColumnFixture {
		public int IntField;

		public override void Execute() {
			IntField= IntField*2;
		}
	}
}