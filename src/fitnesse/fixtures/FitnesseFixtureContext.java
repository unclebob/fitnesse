// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fitnesse.FitNesseContext;
import fitnesse.authentication.Authenticator;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.wiki.WikiPage;

public class FitnesseFixtureContext {
  static WikiPage page;
  static Response response;
  static MockResponseSender sender;
  static Authenticator authenticator;
  static FitNesseContext context;
}
