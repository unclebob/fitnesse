// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

import fitnesse.wiki.WikiPage;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.run.SocketDealer;
import fitnesse.components.Logger;
import fitnesse.html.HtmlPageFactory;
import fitnesse.authentication.*;

public class FitNesseContext
{
	public FitNesse fitnesse;
	public int port = 80;
	public String rootPath = ".";
	public String rootPageName = "FitNesseRoot";
	public String rootPagePath = "";
	public WikiPage root;
	public ResponderFactory responderFactory = new ResponderFactory(rootPagePath);
	public Logger logger;
	public SocketDealer socketDealer = new SocketDealer();
	public Authenticator authenticator = new PromiscuousAuthenticator();
	public HtmlPageFactory htmlPageFactory = new HtmlPageFactory();

	public FitNesseContext()
	{
	}

	public FitNesseContext(WikiPage root)
	{
		this.root = root;
	}

	public String toString()
	{
		String endl = System.getProperty("line.separator");
		StringBuffer buffer = new StringBuffer();
		buffer.append("\t").append("port:              ").append(port).append(endl);
		buffer.append("\t").append("root page:         ").append(root).append(endl);
		buffer.append("\t").append("logger:            ").append(logger == null ? "none" : logger.toString()).append(endl);
		buffer.append("\t").append("authenticator:     ").append(authenticator).append(endl);
		buffer.append("\t").append("html page factory: ").append(htmlPageFactory).append(endl);

		return buffer.toString();
	}
}
