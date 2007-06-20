// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import fit.Counts;
import fitnesse.responders.run.SocketDealer;
import fitnesse.testutil.*;

import java.util.*;

public class FitClientTest extends RegexTest implements FitClientListener
{
	private List<String> outputs = new ArrayList<String>();
	private List<Counts> counts = new ArrayList<Counts>();
	private CommandRunningFitClient client;
	private boolean exceptionOccurred = false;
	private int port = 9080;
	private FitSocketReceiver receiver;
	private SimpleSocketDoner doner;

	public void setUp() throws Exception
	{
		CommandRunningFitClient.TIMEOUT = 5000;
		client = new CommandRunningFitClient(this, "java -cp classes fit.FitServer -v", port, new SocketDealer());
		receiver = new CustomFitSocketReceiver(port);
	}

	private class CustomFitSocketReceiver extends FitSocketReceiver
	{
		public CustomFitSocketReceiver(int port)
		{
			super(port, null);
		}

		protected void dealSocket(int ticket) throws Exception
		{
			doner = new SimpleSocketDoner(socket);
			client.acceptSocketFrom(doner);
		}
	}

	public void tearDown() throws Exception
	{
		receiver.close();
	}

	public void acceptOutput(String output)
	{
		outputs.add(output);
	}

	public void acceptResults(Counts counts)
	{
		this.counts.add(counts);
	}

	public void exceptionOccurred(Exception e)
	{
		exceptionOccurred = true;
		try
		{
			client.kill();
		}
		catch(Exception e1)
		{
			e1.printStackTrace();
		}
	}

	public void testOneRunUsage() throws Exception
	{
		doSimpleRun();
		assertFalse(exceptionOccurred);
		assertEquals(1, outputs.size());
		assertEquals(1, counts.size());
		assertSubString("class", (String) outputs.get(0));
		assertEquals(1, ((Counts) counts.get(0)).right);
	}

	private void doSimpleRun() throws Exception
	{
		receiver.receiveSocket();
		client.start();
		client.send("<html><table><tr><td>fitnesse.testutil.PassFixture</td></tr></table></html>");
		client.done();
		client.join();
	}

	public void testStandardError() throws Exception
	{
		client = new CommandRunningFitClient(this, "java blah", port, new SocketDealer());
		client.start();
		client.join();
		assertTrue(exceptionOccurred);
		assertSubString("Exception", client.commandRunner.getError());
	}

	public void testDosntwaitForTimeoutOnBadCommand() throws Exception
	{
		CommandRunningFitClient.TIMEOUT = 5000;
		long startTime = System.currentTimeMillis();
		client = new CommandRunningFitClient(this, "java blah", port, new SocketDealer());
		client.start();
		client.join();
		assertTrue(exceptionOccurred);
		assertTrue(System.currentTimeMillis() - startTime < CommandRunningFitClient.TIMEOUT);

	}

	public void testOneRunWithManyTables() throws Exception
	{
		receiver.receiveSocket();
		client.start();
		client.send("<html><table><tr><td>fitnesse.testutil.PassFixture</td></tr></table>" +
			"<table><tr><td>fitnesse.testutil.FailFixture</td></tr></table>" +
			"<table><tr><td>fitnesse.testutil.ErrorFixture</td></tr></table></html>");
		client.done();
		client.join();
		assertFalse(exceptionOccurred);
		assertEquals(3, outputs.size());
		assertEquals(1, counts.size());
		Counts count = (Counts) counts.get(0);
		assertEquals(1, count.right);
		assertEquals(1, count.wrong);
		assertEquals(1, count.exceptions);
	}

	public void testManyRuns() throws Exception
	{
		receiver.receiveSocket();
		client.start();
		client.send("<html><table><tr><td>fitnesse.testutil.PassFixture</td></tr></table></html>");
		client.send("<html><table><tr><td>fitnesse.testutil.FailFixture</td></tr></table></html>");
		client.send("<html><table><tr><td>fitnesse.testutil.ErrorFixture</td></tr></table></html>");
		client.done();
		client.join();

		assertFalse(exceptionOccurred);
		assertEquals(3, outputs.size());
		assertEquals(3, counts.size());
		assertEquals(1, ((Counts) counts.get(0)).right);
		assertEquals(1, ((Counts) counts.get(1)).wrong);
		assertEquals(1, ((Counts) counts.get(2)).exceptions);
	}

	public void testDonerIsNotifiedWhenFinished_success() throws Exception
	{
		doSimpleRun();
		assertTrue(doner.finished);
	}

	public void testReadyForSending() throws Exception
	{
		CommandRunningFitClient.TIMEOUT = 5000;
		Thread startThread = new Thread()
		{
			public void run()
			{
				try
				{
					client.start();
				}
				catch(InterruptedException ie)
				{
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		startThread.start();
		Thread.sleep(100);
		assertFalse(client.isSuccessfullyStarted());

		client.acceptSocketFrom(new SimpleSocketDoner(new MockSocket("")));
		Thread.sleep(100);
		assertTrue(client.isSuccessfullyStarted());

		startThread.interrupt();
	}

	public void testUnicodeCharacters() throws Exception
	{
		receiver.receiveSocket();
		client.start();
		client.send("<html><table><tr><td>fitnesse.testutil.EchoFixture</td><td>\uba80\uba81\uba82\uba83</td></tr></table></html>");
		client.done();
		client.join();

		assertFalse(exceptionOccurred);
		StringBuffer buffer = new StringBuffer();
		for(Iterator iterator = outputs.iterator(); iterator.hasNext();)
			buffer.append(iterator.next());

		assertSubString("\uba80\uba81\uba82\uba83", buffer.toString());
	}
}
