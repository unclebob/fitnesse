// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fitnesse.http.*;

public class PuppetResponse extends Response
{
	private ResponsePuppeteer puppeteer;

	public PuppetResponse(ResponsePuppeteer puppeteer)
	{
		this.puppeteer = puppeteer;
	}

	public void readyToSend(ResponseSender sender) throws Exception
	{
		puppeteer.readyToSend(sender);
	}

	protected void addSpecificHeaders()
	{
	}

	public int getContentSize()
	{
		return 0;
	}
}
