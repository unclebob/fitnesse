// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.socketservice;

import java.io.IOException;
import java.net.*;
import java.util.LinkedList;

public class SocketService
{
	private ServerSocket serverSocket = null;
	private Thread serviceThread = null;
	private boolean running = false;
	private SocketServer server = null;
	private LinkedList threads = new LinkedList();

	public SocketService(int port, SocketServer server) throws Exception
	{
		this.server = server;
		serverSocket = new ServerSocket(port);
		serviceThread = new Thread(
			new Runnable()
			{
				public void run()
				{
					serviceThread();
				}
			}
		);
		serviceThread.start();
	}

	public void close() throws Exception
	{
		waitForServiceThreadToStart();
		running = false;
		serverSocket.close();
		serviceThread.join();
		waitForServerThreads();
	}

	private void waitForServiceThreadToStart()
	{
		while(running == false) Thread.yield();
	}

	private void serviceThread()
	{
		running = true;
		while(running)
		{
			try
			{
				Socket s = serverSocket.accept();
				startServerThread(s);
			}
			catch(IOException e)
			{
			}
		}
	}

	private void startServerThread(Socket s)
	{
		Thread serverThread = new Thread(new ServerRunner(s));
		synchronized(threads)
		{
			threads.add(serverThread);
		}
		serverThread.start();
	}

	private void waitForServerThreads() throws InterruptedException
	{
		while(threads.size() > 0)
		{
			Thread t;
			synchronized(threads)
			{
				t = (Thread) threads.getFirst();
			}
			t.join();
		}
	}

	private class ServerRunner implements Runnable
	{
		private Socket socket;

		ServerRunner(Socket s)
		{
			socket = s;
		}

		public void run()
		{
			try
			{
				server.serve(socket);
				synchronized(threads)
				{
					threads.remove(Thread.currentThread());
				}
			}
			catch(Exception e)
			{
			}
		}
	}
}
