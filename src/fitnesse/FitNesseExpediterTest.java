// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

import fitnesse.testutil.*;
import fitnesse.http.*;
import fitnesse.wiki.InMemoryPage;
import fitnesse.responders.*;
import fitnesse.authentication.Authenticator;
import java.io.*;

public class FitNesseExpediterTest extends RegexTest
{
	private FitNesseExpediter expediter;
	private MockSocket socket;
	private FitNesseContext context;
	private InMemoryPage root;
	private PipedInputStream clientInput;
	private PipedOutputStream clientOutput;
	private ResponseParser response;

	public void setUp() throws Exception
	{
		root = (InMemoryPage)InMemoryPage.makeRoot("RooT");
		root.addChildPage("FrontPage");
		socket = new MockSocket();
		context = new FitNesseContext(root);
		context.responderFactory = new ResponderFactory(".");
		expediter = new FitNesseExpediter(socket, context);
	}

	public void tearDown() throws Exception
	{
	}

	public void testAuthenticationGetsCalled() throws Exception
	{
		context.authenticator = new StoneWallAuthenticator();
		MockRequest request = new MockRequest();
		Response response = expediter.createGoodResponse(request);
		assertEquals(401, response.getStatus());
	}

	public void testClosedSocketMidResponse() throws Exception
	{
		try
		{
			MockRequest request = new MockRequest();
			Response response = expediter.createGoodResponse(request);
			socket.close();
			response.readyToSend(expediter);
		}
		catch(IOException e)
		{
			fail("no IOException should be thrown");
		}
	}

  public void testIncompleteRequestsTimeOut() throws Exception
  {
	  final FitNesseExpediter sender = preparePipedFitNesseExpediter();

	  Thread senderThread = makeSendingThread(sender);
	  senderThread.start();
		Thread parseResponseThread = makeParsingThread();
		parseResponseThread.start();
    Thread.sleep(sender.requestParsingTimeLimit + 100);

	  parseResponseThread.join();

	  assertEquals(408, response.getStatus());
  }

	private FitNesseExpediter preparePipedFitNesseExpediter() throws Exception
	{
		PipedInputStream socketInput = new PipedInputStream();
		clientOutput = new PipedOutputStream(socketInput);
		clientInput = new PipedInputStream();
		PipedOutputStream socketOutput = new PipedOutputStream(clientInput);
		MockSocket socket = new MockSocket(socketInput, socketOutput);
		final FitNesseExpediter sender = new FitNesseExpediter(socket, context);
		sender.requestParsingTimeLimit = 200;
		return sender;
	}

	public void testCompleteRequest() throws Exception
	{
		final FitNesseExpediter sender = preparePipedFitNesseExpediter();

		Thread senderThread = makeSendingThread(sender);
		senderThread.start();
		Thread parseResponseThread = makeParsingThread();
		parseResponseThread.start();

		clientOutput.write("GET /root HTTP/1.1\r\n\r\n".getBytes());
		clientOutput.flush();

		parseResponseThread.join();

		assertEquals(200, response.getStatus());
	}

	public void testSlowButCompleteRequest() throws Exception
	{
		final FitNesseExpediter sender = preparePipedFitNesseExpediter();

		Thread senderThread = makeSendingThread(sender);
		senderThread.start();
		Thread parseResponseThread = makeParsingThread();
		parseResponseThread.start();

		byte[] bytes = "GET /root HTTP/1.1\r\n\r\n".getBytes();
		try
		{
			for(int i = 0; i < bytes.length; i++)
			{
				byte aByte = bytes[i];
				clientOutput.write(aByte);
				clientOutput.flush();
				Thread.sleep(20);
			}
		}
		catch(IOException pipedClosed)
		{
		}

		parseResponseThread.join();

		assertEquals(200, response.getStatus());
	}

	private Thread makeSendingThread(final FitNesseExpediter sender)
	{
		Thread senderThread = new Thread(new Runnable()
    {
      public void run()
      {
        try
        {
          sender.start();
        }
        catch(Exception e)
        {
          e.printStackTrace();
        }
      }
    });
		return senderThread;
	}

	private Thread makeParsingThread()
	{
		Thread parseResponseThread = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					response = new ResponseParser(clientInput);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		});
		return parseResponseThread;
	}

	class StoneWallAuthenticator extends Authenticator
	{
    public Responder authenticate(FitNesseContext context, Request request, Responder privilegedResponder) throws Exception
    {
      return new UnauthorizedResponder();
    }

		public boolean isAuthenticated(String username, String password)
		{
			return false;
		}
	}

}
