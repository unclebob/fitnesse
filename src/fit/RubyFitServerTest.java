// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
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
