// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.testutil;

import fitnesse.*;
import fitnesse.responders.ResponderFactory;
import fitnesse.util.FileUtil;
import fitnesse.wiki.*;

public class FitNesseUtil
{
	private static FitNesse instance = null;
	public static final int port = 1999;
	public static FitNesseContext context;
	public static final String URL = "http://localhost:" + port + "/";

	public static void startFitnesse(WikiPage root) throws Exception
	{
		context = new FitNesseContext();
		context.root = root;
		context.port = port;
		context.rootPath = "TestDir";
		context.rootPageName = root.getName();
		context.rootPagePath = context.rootPath + "/" + context.rootPageName;
		context.responderFactory = new ResponderFactory(context.rootPagePath);
		instance = new FitNesse(context);
		instance.start();
	}

	public static void stopFitnesse() throws Exception
	{
		instance.stop();
		FileUtil.deleteFileSystemDirectory("TestDir");
	}

	public static void bindVirtualLinkToPage(WikiPage host, WikiPage proxy) throws Exception
	{
		VirtualCouplingPage coupling = new VirtualCouplingPage(host, proxy);
		((VirtualCouplingExtension) host.getExtension(VirtualCouplingExtension.NAME)).setVirtualCoupling(coupling);
	}
}
