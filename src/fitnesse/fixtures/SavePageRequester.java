// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

public class SavePageRequester extends ResponseRequester
{
	public String saveContents;

	protected void details()
	{
		request.addInput("responder", "saveData");
		request.addInput("saveId", "9999999999999");
		request.addInput("ticketId", "321");
		request.addInput("pageContent", saveContents);
	}
}
