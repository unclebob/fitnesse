// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fitnesse.wiki.WikiPage;
import fitnesse.http.*;
import fitnesse.responders.ResponderFactory;
import fitnesse.*;

public class FitnesseFixtureContext
{
	public static WikiPage root;
	public static WikiPage page;
	public static Response response;
	public static MockResponseSender sender;
	public static ResponderFactory responderFactory;
	public static String baseDir = "temp";
  public static FitNesseContext context;
	public static FitNesse fitnesse;
}
