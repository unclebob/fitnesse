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
