// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.authentication;

import fitnesse.testutil.*;

public class PasswordTest extends RegexTest
{
	private Password password;

	public void setUp() throws Exception
	{
		password = new Password("testDir/password.txt");
	}

	public void testArgsJustUser() throws Exception
	{
		password = new Password();
		boolean valid = password.args(new String[] {"splinter"});
		assertTrue(valid);
		assertEquals("splinter", password.getUsername());
		assertEquals("passwords.txt", password.getFilename());
	}

	public void testArgsWithFilename() throws Exception
	{
		boolean valid = password.args(new String[] {"-f", "somefile.txt", "shredder"});
		assertTrue(valid);
		assertEquals("shredder", password.getUsername());
		assertEquals("somefile.txt", password.getFilename());
	}

	public void testbadArgs() throws Exception
	{
		boolean valid = password.args(new String[] {});
		assertFalse(valid);
		valid = password.args(new String[] {"-d", "filename", "beebop"});
		assertFalse(valid);
	}

	public void testArgsWithNewCipher() throws Exception
	{
		boolean valid = password.args(new String[] {"-c", "fitnesse.authentication.TransparentCipher", "shredder"});
		assertTrue(valid);
		assertEquals(TransparentCipher.class, password.getCipher().getClass());

	}
}
