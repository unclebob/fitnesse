// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

using System;
using System.IO;

namespace fit 
{
	public class FileRunnerExe : FileRunner 
	{
		public static void Main (String[] argv) 
		{
			new FileRunnerExe().Run(argv);
		}
	}
}
