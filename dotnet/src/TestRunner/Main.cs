// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
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
