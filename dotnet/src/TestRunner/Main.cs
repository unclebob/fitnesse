// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
namespace TestRunner
{
	public class TestRunnerMain
	{
		public static int Main(string[] args)
		{
			fitnesse.fitserver.TestRunner runner = new fitnesse.fitserver.TestRunner();
			runner.Run(args);
			return runner.ExitCode();
		}
	}
}
