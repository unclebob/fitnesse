// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.files;

import fitnesse.FitNesseContext;
import fitnesse.authentication.*;
import fitnesse.http.*;
import fitnesse.responders.SecureResponder;

import java.io.File;

public class CreateDirectoryResponder implements SecureResponder
{
	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		SimpleResponse response = new SimpleResponse();

		String resource = request.getResource();
		String dirname = (String) request.getInput("dirname");
		String pathname = context.rootPagePath + "/" + resource + dirname;
		File file = new File(pathname);
		if(!file.exists())
			file.mkdir();

		response.redirect("/" + resource);
		return response;
	}

	public SecureOperation getSecureOperation()
	{
		return new AlwaysSecureOperation();
	}
}
