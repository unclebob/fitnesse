using fit;

namespace fitnesse.acceptanceTests
{
	public class SummaryInspectorFixture : ColumnFixture
	{
		public int NumberRight;
		public int NumberWrong;
		public int NumberIgnores;
		public int NumberExceptions;
		public string Name
		{
			get
			{
				Fixture fixture = Fixture.LastFixtureLoaded;
				NumberRight = fixture.Counts.Right;
				NumberWrong = fixture.Counts.Wrong;
				NumberIgnores = fixture.Counts.Ignores;
				NumberExceptions = fixture.Counts.Exceptions;
				return fixture.GetType().Name;
			}
		}
	}
}
