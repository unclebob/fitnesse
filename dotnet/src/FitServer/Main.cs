// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
namespace FitServer
{
	public class FitServerMain
	{
		public static int Main(string[] CommandLineArguments)
		{
			fitnesse.fitserver.FitServer fitServer = new fitnesse.fitserver.FitServer();
			fitServer.Run(CommandLineArguments);
			return fitServer.ExitCode();
		}
	}
}
