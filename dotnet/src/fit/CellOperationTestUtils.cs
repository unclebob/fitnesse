// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;

namespace fit
{
	public class CellOperationTestUtils
	{
		public static object ThrowError()
		{
			throw new ApplicationException();
		}

		public static object ReturnNull()
		{
			return null;
		}

		public static object Return37()
		{
			return 37;
		}

		public static object ReturnSomeValue()
		{
			return "someValue";
		}

		public static object ReturnEmptyString()
		{
			return "";
		}
	}
}