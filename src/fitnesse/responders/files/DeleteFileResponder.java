// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.files;

import fitnesse.FitNesseContext;
import fitnesse.authentication.*;
import fitnesse.http.*;
import fitnesse.responders.SecureResponder;
import fitnesse.util.FileUtil;

import java.io.File;

public class DeleteFileResponder implements SecureResponder
{
	public String resource;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		Response response = new SimpleResponse();
		resource = request.getResource();
		String filename = (String) request.getInput("filename");
		String pathname = context.rootPagePath + "/" + resource + filename;
		File file = new File(pathname);

		if(file.isDirectory())
			FileUtil.deleteFileSystemDirectory(file);
		else
			file.delete();

		response.redirect("/" + resource);
		return response;
	}

	public SecureOperation getSecureOperation()
	{
		return new AlwaysSecureOperation();
	}
}
