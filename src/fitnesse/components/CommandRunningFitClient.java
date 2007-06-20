// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import fitnesse.responders.run.*;

public class CommandRunningFitClient extends FitClient implements SocketSeeker
{
	public static int TIMEOUT = 60000;
	private static final String SPACE = " ";

	private int ticketNumber;
	public CommandRunner commandRunner;
	private SocketDoner donor;
	private boolean connectionEstablished = false;

	private Thread timeoutThread;
	private Thread earlyTerminationThread;

	public CommandRunningFitClient(FitClientListener listener, String command, int port, SocketDealer dealer)
		throws Exception
	{
		super(listener);
		ticketNumber = dealer.seekingSocket(this);
		String hostName = java.net.InetAddress.getLocalHost().getHostName();
		commandRunner = new CommandRunner(command + SPACE + hostName + SPACE + port + SPACE + ticketNumber, "");
	}

	public void start() throws Exception
	{
		try
		{
			commandRunner.start();

			timeoutThread = new Thread(new TimeoutRunnable(), "FitClient timeout");
			timeoutThread.start();
			earlyTerminationThread = new Thread(new EarlyTerminationRunnable(), "FitClient early termination");
			earlyTerminationThread.start();
			waitForConnection();
		}
		catch(Exception e)
		{
			listener.exceptionOccurred(e);
		}
	}

	public void acceptSocketFrom(SocketDoner donor) throws Exception
	{
		this.donor = donor;
		acceptSocket(donor.donateSocket());
		connectionEstablished = true;

		synchronized(this)
		{
			notify();
		}
	}

	void setTicketNumber(int ticketNumber)
	{
		this.ticketNumber = ticketNumber;
	}

	public boolean isSuccessfullyStarted()
	{
		return fitSocket != null;
	}

	private void waitForConnection() throws InterruptedException
	{
		while(fitSocket == null)
		{
			Thread.sleep(100);
			checkForPulse();
		}
	}

	public void join() throws Exception
	{
		try
		{
			commandRunner.join();
			super.join();
			if(donor != null)
				donor.finishedWithSocket();
			killVigilantThreads();
		}
		catch(InterruptedException e)
		{
		}
	}

	public void kill() throws Exception
	{
		super.kill();
		killVigilantThreads();
		commandRunner.kill();
	}

	private void killVigilantThreads()
	{
		if(timeoutThread != null)
			timeoutThread.interrupt();
		if(earlyTerminationThread != null)
			earlyTerminationThread.interrupt();
	}

	public void exceptionOccurred(Exception e)
	{
		commandRunner.exceptionOccurred(e);
		super.exceptionOccurred(e);
	}

	private class TimeoutRunnable implements Runnable
	{
		long timeSlept = 0;

		public void run()
		{
			try
			{
				Thread.sleep(TIMEOUT);
				synchronized(CommandRunningFitClient.this)
				{
					if(fitSocket == null)
					{
						CommandRunningFitClient.this.notify();
						listener.exceptionOccurred(new Exception(
							"FitClient: communication socket was not received on time."));
					}
				}
			}
			catch(InterruptedException e)
			{
				// ok
			}
		}
	}

	private class EarlyTerminationRunnable implements Runnable
	{
		public void run()
		{
			try
			{
				commandRunner.process.waitFor();
				synchronized(CommandRunningFitClient.this)
				{
					if(!connectionEstablished)
					{
						CommandRunningFitClient.this.notify();
						listener.exceptionOccurred(new Exception(
							"FitClient: external process terminated before a connection could be established."));
					}
				}
			}
			catch(InterruptedException e)
			{
				// ok
			}
		}
	}
}