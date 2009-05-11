// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;

public abstract class Authenticator {
  public Authenticator() {
  }

  public Responder authenticate(FitNesseContext context, Request request, Responder privilegedResponder) throws Exception {
    request.getCredentials();
    String username = request.getAuthorizationUsername();
    String password = request.getAuthorizationPassword();

    if (isAuthenticated(username, password))
      return privilegedResponder;
    else if (!isSecureResponder(privilegedResponder))
      return privilegedResponder;
    else
      return verifyOperationIsSecure(privilegedResponder, context, request);
  }

  private Responder verifyOperationIsSecure(Responder privilegedResponder, FitNesseContext context, Request request) {
    SecureOperation so = ((SecureResponder) privilegedResponder).getSecureOperation();
    try {
      if (so.shouldAuthenticate(context, request))
        return unauthorizedResponder(context, request);
      else
        return privilegedResponder;
    }
    catch (Exception e) {
      e.printStackTrace();
      return unauthorizedResponder(context, request);
    }
  }

  protected Responder unauthorizedResponder(FitNesseContext context, Request request) {
    return new UnauthorizedResponder();
  }

  private boolean isSecureResponder(Responder privilegedResponder) {
    return (privilegedResponder instanceof SecureResponder);
  }

  public abstract boolean isAuthenticated(String username, String password) throws Exception;

  public String toString() {
    return getClass().getName();
  }
}
