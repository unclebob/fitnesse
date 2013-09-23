// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import fitnesse.FitNesseContext;
import fitnesse.http.Request;

public class AlwaysSecureOperation implements SecureOperation {
  public boolean shouldAuthenticate(FitNesseContext context, Request request) {
    return true;
  }
}
