// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

import fitnesse.authentication.*;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.util.FileUtil;
import junit.framework.TestCase;
import junit.swingui.TestRunner;

import java.io.File;

public class FitNesseMainTest extends TestCase
{
	private FitNesseContext context;

	public static void main(String[] args)
	{
		TestRunner.main(new String[]{"FitNesseMainTest"});
	}

	public void setUp() throws Exception
	{
		context = new FitNesseContext();
	}

	public void tearDown() throws Exception
	{
		FileUtil.deleteFileSystemDirectory("testFitnesseRoot");
	}

	public void testDirCreations() throws Exception
	{
		context.port = 80;
		context.rootPagePath = "testFitnesseRoot";
		new FitNesse(context);

		assertTrue(new File("testFitnesseRoot").exists());
		assertTrue(new File("testFitnesseRoot/files").exists());
	}

	public void testMakeNullAuthenticator() throws Exception
	{
		Authenticator a = FitNesse.makeAuthenticator(null, new ComponentFactory("blah"));
		assertTrue(a instanceof PromiscuousAuthenticator);
	}

	public void testMakeOneUserAuthenticator() throws Exception
	{
		Authenticator a = FitNesse.makeAuthenticator("bob:uncle", new ComponentFactory("blah"));
		assertTrue(a instanceof OneUserAuthenticator);
		OneUserAuthenticator oua = (OneUserAuthenticator) a;
		assertEquals("bob", oua.getUser());
		assertEquals("uncle", oua.getPassword());
	}

	public void testMakeMultiUserAuthenticator() throws Exception
	{
		final String passwordFilename = "testpasswd";
		File passwd = new File(passwordFilename);
		passwd.createNewFile();
		Authenticator a = FitNesse.makeAuthenticator(passwordFilename, new ComponentFactory("blah"));
		assertTrue(a instanceof MultiUserAuthenticator);
		passwd.delete();
	}

	public void testContextFitNesseGetSet() throws Exception
	{
		FitNesse fitnesse = new FitNesse(context, false);
		assertSame(fitnesse, context.fitnesse);
	}

	public void testIsRunning() throws Exception
	{
		context.port = FitNesseUtil.port;
		FitNesse fitnesse = new FitNesse(context, false);

		assertFalse(fitnesse.isRunning());

		fitnesse.start();
		assertTrue(fitnesse.isRunning());

		fitnesse.stop();
		assertFalse(fitnesse.isRunning());
	}
}
