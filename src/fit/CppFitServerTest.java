// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

public class CppFitServerTest extends FitServerTest
{
	protected String command()
	{
		return "C:\\tools\\fit\\cpp\\FitServer\\Debug\\FitServer.exe";
	}

  public void testSimpleStartUp() throws Exception
  {
    super.testSimpleStartUp();
  }

  public void testBadConnection() throws Exception
  {
    super.testBadConnection();
  }

  public void testNonTestInput() throws Exception
  {
    super.testNonTestInput();
  }

  public void testOneSimpleRun_Fail() throws Exception
  {
    super.testOneSimpleRun_Fail();
  }

  public void testOneSimpleRun_Pass() throws Exception
  {
    super.testOneSimpleRun_Pass();
  }

  public void testTwoSimpleRuns() throws Exception
  {
    super.testTwoSimpleRuns();
  }

  public void testOneMulitiTableRun() throws Exception
  {
    super.testOneMulitiTableRun();
  }

  public void testExtraTextIdPrinted() throws Exception
  {
    super.testExtraTextIdPrinted();
  }

	protected String simpleTable(String fixtureName)
	{
		return "<table>" +
		  "<tr><td>" + fixtureName + "</td></tr>" +
		  "</table>";
	}
}
