package fitnesse.fixtures;

import fit.*;

public class Sleep extends Fixture
{
	public void doTable(Parse table)
	{
		String args[] = getArgs();
		long millis = Long.parseLong(args[0]);
		try
		{
			Thread.sleep(millis);
		}
		catch(InterruptedException e)
		{
		}
	}
}
