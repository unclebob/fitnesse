// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.socketservice;

import junit.framework.TestCase;
import junit.swingui.TestRunner;

import java.io.*;
import java.net.Socket;

public class SocketServiceTest extends TestCase
{
	private int connections = 0;
	private SocketServer connectionCounter;
	private SocketService ss;
	private final static int portNumber = 1999;

	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"com.objectmentor.SocketService.SocketServiceTest"});
	}

	public SocketServiceTest()
	{
		connectionCounter = new SocketServer()
		{
			public void serve(Socket s)
			{
				connections++;
			}
		};
	}

	public void setUp() throws Exception
	{
		connections = 0;
	}

	public void tearDown() throws Exception
	{
	}

	public void testNoConnections() throws Exception
	{
		ss = new SocketService(portNumber, connectionCounter);
		ss.close();
		assertEquals(0, connections);
	}

	public void testOneConnection() throws Exception
	{
		ss = new SocketService(portNumber, connectionCounter);
		connect(portNumber);
		ss.close();
		assertEquals(1, connections);
	}

	public void testManyConnections() throws Exception
	{
		ss = new SocketService(portNumber, connectionCounter);
		for(int i = 0; i < 10; i++)
			connect(portNumber);
		ss.close();
		assertEquals(10, connections);
	}

	public void testSendMessage() throws Exception
	{
		ss = new SocketService(portNumber, new HelloService());
		Socket s = new Socket("localhost", portNumber);
		BufferedReader br = TestUtility.GetBufferedReader(s);
		String answer = br.readLine();
		s.close();
		ss.close();
		assertEquals("Hello", answer);
	}

	public void testReceiveMessage() throws Exception
	{
		ss = new SocketService(portNumber, new EchoService());
		Socket s = new Socket("localhost", portNumber);
		BufferedReader br = TestUtility.GetBufferedReader(s);
		PrintStream ps = TestUtility.GetPrintStream(s);
		ps.println("MyMessage");
		String answer = br.readLine();
		s.close();
		ss.close();
		assertEquals("MyMessage", answer);
	}

	public void testMultiThreaded() throws Exception
	{
		ss = new SocketService(portNumber, new EchoService());
		Socket s = new Socket("localhost", portNumber);
		BufferedReader br = TestUtility.GetBufferedReader(s);
		PrintStream ps = TestUtility.GetPrintStream(s);

		Socket s2 = new Socket("localhost", portNumber);
		BufferedReader br2 = TestUtility.GetBufferedReader(s2);
		PrintStream ps2 = TestUtility.GetPrintStream(s2);

		ps2.println("MyMessage2");
		String answer2 = br2.readLine();
		s2.close();

		ps.println("MyMessage1");
		String answer = br.readLine();
		s.close();

		ss.close();
		assertEquals("MyMessage2", answer2);
		assertEquals("MyMessage1", answer);
	}

	private void connect(int port)
	{
		try
		{
			Socket s = new Socket("localhost", port);
			try
			{
				Thread.sleep(10);
			}
			catch(InterruptedException e)
			{
			}
			s.close();
		}
		catch(IOException e)
		{
			fail("could not connect");
		}
	}
}

class TestUtility
{
	public static PrintStream GetPrintStream(Socket s) throws IOException
	{
		OutputStream os = s.getOutputStream();
		PrintStream ps = new PrintStream(os);
		return ps;
	}

	public static BufferedReader GetBufferedReader(Socket s) throws IOException
	{
		InputStream is = s.getInputStream();
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		return br;
	}
}

class HelloService implements SocketServer
{
	public void serve(Socket s)
	{
		try
		{
			PrintStream ps = TestUtility.GetPrintStream(s);
			ps.println("Hello");
		}
		catch(IOException e)
		{
		}
	}
}

class EchoService implements SocketServer
{
	public void serve(Socket s)
	{
		try
		{
			PrintStream ps = TestUtility.GetPrintStream(s);
			BufferedReader br = TestUtility.GetBufferedReader(s);
			String token = br.readLine();
			ps.println(token);
		}
		catch(IOException e)
		{
		}
	}
}
