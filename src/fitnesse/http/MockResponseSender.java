// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.http;

import java.net.Socket;
import fitnesse.testutil.MockSocket;

public class MockResponseSender implements ResponseSender
{
	public MockSocket socket;
	public boolean closed = false;

	public MockResponseSender()
	{
		socket = new MockSocket("Mock");
	}

	public MockResponseSender(Response response) throws Exception
	{
		this();
		doSending(response);
	}

	public void send(byte[] bytes) throws Exception
	{
		socket.getOutputStream().write(bytes);
	}

	public void close() throws Exception
	{
		closed = true;
	}

	public Socket getSocket() throws Exception
	{
		return socket;
	}

	public String sentData() throws Exception
	{
		return socket.getOutput();
	}

	public void doSending(Response response) throws Exception
	{
		response.readyToSend(this);
		waitForClose(10000);
	}

	// Utility method that returns when this.closed is true.  Throws an exception
	// if the timeout is reached.
	public void waitForClose(final long timeoutMillis) throws Exception
	{
		long start = System.currentTimeMillis();
		while(!closed)
		{
			Thread.yield();
			long now = System.currentTimeMillis();
			if(now - start > timeoutMillis)
				throw new Exception("MockResponseSender could not be closed");
		}
	}
}

