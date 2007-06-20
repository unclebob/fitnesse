// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

import fitnesse.http.ResponseParser;
import fitnesse.testutil.RegexTest;

import java.net.UnknownHostException;

public class ShutdownTest extends RegexTest
{
	private Shutdown shutdown;

	public void setUp() throws Exception
	{
		shutdown = new Shutdown();
	}

	public void tearDown() throws Exception
	{
	}

	public void testArgs() throws Exception
	{
		assertTrue(shutdown.parseArgs(new String[]{}));
		assertEquals("localhost", shutdown.hostname);
		assertEquals(80, shutdown.port);
		assertEquals(null, shutdown.username);
		assertEquals(null, shutdown.password);

		assertTrue(shutdown.parseArgs(new String[]{"-h", "host.com", "-p", "1234", "-c", "user", "pass"}));
		assertEquals("host.com", shutdown.hostname);
		assertEquals(1234, shutdown.port);
		assertEquals("user", shutdown.username);
		assertEquals("pass", shutdown.password);
	}

	public void testBuildRequest() throws Exception
	{
		String request = shutdown.buildRequest().getText();
		assertSubString("GET /?responder=shutdown", request);
		assertNotSubString("Authorization: ", request);

		shutdown.username = "user";
		shutdown.password = "pass";
		request = shutdown.buildRequest().getText();
		assertSubString("Authorization: ", request);
	}

	public void testBadServer() throws Exception
	{
		try
		{
			shutdown.hostname = "google.com";
			ResponseParser response = shutdown.buildAndSendRequest();
			String status = shutdown.checkResponse(response);
			assertEquals("Not a FitNesse server", status);
		}
		catch(UnknownHostException e)
		{
		}
	}
}
