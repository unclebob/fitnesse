// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.authentication;

public class OneUserAuthenticator extends Authenticator
{
	private String theUsername;
	private String thePassword;

	public OneUserAuthenticator(String theUsername, String thePassword)
	{
		this.theUsername = theUsername;
		this.thePassword = thePassword;
	}

	public boolean isAuthenticated(String username, String password)
	{
		return (theUsername.equals(username) && thePassword.equals(password));
	}

	public String getUser()
	{
		return theUsername;
	}

	public String getPassword()
	{
		return thePassword;
	}
}
