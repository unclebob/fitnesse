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
