// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
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
