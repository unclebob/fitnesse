// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.authentication;

import fitnesse.*;
import fitnesse.http.Request;
import fitnesse.responders.*;

public abstract class Authenticator
{
	public Authenticator()
	{
	}

	public Responder authenticate(FitNesseContext context, Request request, Responder privilegedResponder) throws Exception
	{
		request.getCredentials();
		String username = request.getAuthorizationUsername();
		String password = request.getAuthorizationPassword();

		if(isAuthenticated(username, password))
			return privilegedResponder;
		else if(!isSecureResponder(privilegedResponder))
			return privilegedResponder;
		else
			return verifyOperationIsSecure(privilegedResponder, context, request);
	}

	private Responder verifyOperationIsSecure(Responder privilegedResponder, FitNesseContext context, Request request)
	{
		SecureOperation so = ((SecureResponder) privilegedResponder).getSecureOperation();
		try
		{
			if(so.shouldAuthenticate(context, request))
				return new UnauthorizedResponder();
			else
				return privilegedResponder;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new UnauthorizedResponder();
		}
	}

	private boolean isSecureResponder(Responder privilegedResponder)
	{
		return (privilegedResponder instanceof SecureResponder);
	}

	public abstract boolean isAuthenticated(String username, String password) throws Exception;

	public String toString()
	{
		return getClass().getName();
	}
}
