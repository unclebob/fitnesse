// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

import fitnesse.socketservice.SocketServer;

import java.net.Socket;

public class FitNesseServer implements SocketServer
{
	private FitNesseContext context;

	public FitNesseServer(FitNesseContext context)
	{
		this.context = context;
	}

	public void serve(Socket s)
	{
		serve(s, 10000);
	}

	public void serve(Socket s, long requestTimeout)
	{
		try
		{
			FitNesseExpediter sender = new FitNesseExpediter(s, context);
			sender.setRequestParsingTimeLimit(requestTimeout);
			sender.start();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}