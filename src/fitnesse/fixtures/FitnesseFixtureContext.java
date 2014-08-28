// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fitnesse.FitNesseContext;
import fitnesse.authentication.Authenticator;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.WikiPage;

public class FitnesseFixtureContext {
  public static WikiPage root;
  public static WikiPage page;
  public static Response response;
  public static MockResponseSender sender;
  public static String baseDir = FitNesseUtil.base;
  public static Authenticator authenticator;
  public static FitNesseContext context;
}
