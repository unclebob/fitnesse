// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import fit.Counts;
import fitnesse.util.StreamReader;

import java.io.OutputStream;
import java.net.Socket;

public class FitClient
{

	protected FitClientListener listener;
	protected Socket fitSocket;
	private OutputStream fitInput;
	private StreamReader fitOutput;

	private volatile int sent = 0;
	private volatile int received = 0;
	private volatile boolean isDoneSending = false;
	protected volatile boolean killed = false;
	protected Thread fitListeningThread;

	public FitClient(FitClientListener listener) throws Exception
	{
		this.listener = listener;
	}

	public void acceptSocket(Socket socket) throws Exception
	{
		checkForPulse();
		fitSocket = socket;
		fitInput = fitSocket.getOutputStream();
		FitProtocol.writeData("", fitInput);
		fitOutput = new StreamReader(fitSocket.getInputStream());

		fitListeningThread = new Thread(new FitListeningRunnable(), "FitClient fitOutput");
		fitListeningThread.start();
	}

	public void send(String data) throws Exception
	{
		checkForPulse();
		FitProtocol.writeData(data, fitInput);
		sent++;
	}

	public void done() throws Exception
	{
		checkForPulse();
		FitProtocol.writeSize(0, fitInput);
		isDoneSending = true;
	}

	public void join() throws Exception
	{
		if(fitListeningThread != null)
			fitListeningThread.join();
	}

	public void kill() throws Exception
	{
		killed = true;
		if(fitListeningThread != null)
			fitListeningThread.interrupt();
	}

	public void exceptionOccurred(Exception e)
	{
		listener.exceptionOccurred(e);
	}

	protected void checkForPulse() throws InterruptedException
	{
		if(killed)
			throw new InterruptedException("FitClient was killed");
	}

	private void listenToFit()
	{
		try
		{
			while(!finishedReading())
			{
				int size;
				size = FitProtocol.readSize(fitOutput);
				if(size != 0)
				{
					String readValue = fitOutput.read(size);
					if(fitOutput.byteCount() < size)
						throw new Exception("I was expecting " + size + " bytes but I only got " + fitOutput.byteCount());
					listener.acceptOutput(readValue);
				}
				else
				{
					Counts counts = FitProtocol.readCounts(fitOutput);
					listener.acceptResults(counts);
					received++;
				}
			}
		}
		catch(Exception e)
		{
			exceptionOccurred(e);
		}
	}

	private boolean finishedReading()
	{
		while(stateIndeterminate())
			shortSleep();
		return isDoneSending && received == sent;
	}

	/**
	 * @return true if the current state of the transission is indeterminate.
	 *         <p/>
	 *         When the number of pages sent and recieved is the same, we may be done with the whole job,
	 *         or we may just be waiting for FitNesse to send the next page.  There's no way to know until
	 *         FitNesse either calls send, or done.
	 */
	private boolean stateIndeterminate()
	{
		return (received == sent) && !isDoneSending;
	}

	private void shortSleep()
	{
		try
		{
			Thread.sleep(10);
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		}
	}

	private class FitListeningRunnable implements Runnable
	{
		public void run()
		{
			listenToFit();
		}
	}
}
