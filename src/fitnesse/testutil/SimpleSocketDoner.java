// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.testutil;

import java.net.Socket;
import fitnesse.testutil.MockSocket;
import fitnesse.responders.run.SocketDoner;

public class SimpleSocketDoner implements SocketDoner
{
	public Socket socket;
	public boolean finished = false;

	public SimpleSocketDoner()
	{
		socket = new MockSocket("SimpleSocketDoner");
	}

	public SimpleSocketDoner(Socket socket)
	{
		this.socket = socket;
	}

	public Socket donateSocket()
	{
		return socket;
	}

	public void finishedWithSocket() throws Exception
	{
		finished = true;
		socket.close();
	}
}
