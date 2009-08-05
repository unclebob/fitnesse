// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fitnesse.authentication.OneUserAuthenticator;

public class AuthenticatorSetup
{
  private String username;
  private String password;

  public String status() {
    FitnesseFixtureContext.context.authenticator = new OneUserAuthenticator(username, password);
    return "ok";
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }
}
