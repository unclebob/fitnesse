// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

import fitnesse.http.*;
import fitnesse.wiki.WikiPage;

public interface Responder
{
	public Response makeResponse(FitNesseContext context, Request request) throws Exception;
}
