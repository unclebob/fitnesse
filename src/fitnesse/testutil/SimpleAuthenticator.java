// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.testutil;

import fitnesse.authentication.Authenticator;

import java.util.Properties;

public class SimpleAuthenticator extends Authenticator
{
	public boolean authenticated = false;

	public SimpleAuthenticator()
	{
	}

	public SimpleAuthenticator(Properties p)
	{
		p.propertyNames();
	}

	public boolean isAuthenticated(String username, String password)
	{
		return authenticated;
	}
}
