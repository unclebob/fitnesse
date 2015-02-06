// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.Request;

public abstract class Authenticator {
  public Authenticator() {
  }

  public Responder authenticate(FitNesseContext context, Request request, Responder privilegedResponder) {
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
    if (so.shouldAuthenticate(context, request))
      return unauthorizedResponder(context, request);
    else
      return privilegedResponder;
  }

  protected Responder unauthorizedResponder(FitNesseContext context, Request request) {
    return new UnauthorizedResponder();
  }

  private boolean isSecureResponder(Responder privilegedResponder) {
    return (privilegedResponder instanceof SecureResponder);
  }

  public abstract boolean isAuthenticated(String username, String password);

  public String toString() {
    return getClass().getName();
  }
}
