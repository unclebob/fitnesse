package fit;

public class RubyFitServerTest extends FitServerTest
{
  protected String command()
  {
    return "ruby ruby/bin/FitServer.rb -v";
  }

	protected String simpleTable(String fixtureName)
	{
		return "<table>" +
      "<tr><td>fat." + fixtureName + "</td></tr>" +
      "</table>";
	}

	public void testBadConnection() throws Exception
	{
		super.testBadConnection();
	}
}
