// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using fit;

namespace fitnesse.acceptanceTests
{
	public class ResetNamespacesFixture : Fixture
	{
		public override void DoTable(Parse table)
		{
			ResetNamespaces();
		}

	}
}
