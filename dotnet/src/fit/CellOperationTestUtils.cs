// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
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