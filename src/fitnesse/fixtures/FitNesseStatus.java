package fitnesse.fixtures;

import fit.ColumnFixture;

public class FitNesseStatus extends ColumnFixture
{
	public boolean isRunning()
	{
		return FitnesseFixtureContext.fitnesse.isRunning();
	}
}
