// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fitnesse.testutil.*;
import fitnesse.http.*;
import fitnesse.FitNesseContext;

public class SocketCatchingResponderTest extends RegexTest
{
	private SocketDealer dealer;
	private SimpleSocketSeeker seeker;
	private MockResponseSender sender;
	private SocketCatchingResponder responder;
	private FitNesseContext context;
	private MockRequest request;

	public void setUp() throws Exception
	{
		dealer = new SocketDealer();
		seeker = new SimpleSocketSeeker();
		sender = new MockResponseSender();
		responder = new SocketCatchingResponder();
		context = new FitNesseContext();
		context.socketDealer = dealer;
		request = new MockRequest();
	}

	public void tearDown() throws Exception
	{
	}

	public void testSuccess() throws Exception
	{
		int ticket = dealer.seekingSocket(seeker);
		request.addInput("ticket", ticket + "");
		Response response = responder.makeResponse(context, request);
		response.readyToSend(sender);

		assertEquals("", sender.sentData());
	}

	public void testMissingSeeker() throws Exception
	{
		request.addInput("ticket", "123");
		Response response = responder.makeResponse(context, request);
		response.readyToSend(sender);

		assertHasRegexp("There are no clients waiting for a socket with ticketNumber 123", sender.sentData());
		assertTrue(sender.closed);
		assertEquals(404, response.getStatus());
	}


}
