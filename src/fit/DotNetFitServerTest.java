// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

public class DotNetFitServerTest extends FitServerTest
{
  protected String command()
  {
    return "dotnet/FitServer.exe -v dotnet\\fit.dll";
  }

  protected String simpleTable(String fixtureName)
  {
    return "<table>" +
      "<tr><td>fit." + fixtureName + "</td></tr>" +
      "</table>";
  }
}
