// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import fitnesse.http.Request;
import fitnesse.http.Response;

public interface Responder {
  public Response makeResponse(FitNesseContext context, Request request) throws Exception;
}
