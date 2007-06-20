// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

import fitnesse.authentication.*;
import fitnesse.components.*;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.*;
import fitnesse.socketservice.SocketService;
import fitnesse.updates.Updater;
import fitnesse.wiki.*;

import java.io.File;
import java.net.BindException;

public class FitNesse
{
	public static final String VERSION = "20070619";

	private FitNesseContext context = new FitNesseContext();
	private SocketService theService;
	private static String extraOutput;

	public static void main(String[] args) throws Exception
	{
		Arguments arguments = parseCommandLine(args);
		if(arguments != null)
		{
			FitNesseContext context = loadContext(arguments);
			PageVersionPruner.daysTillVersionsExpire = arguments.getDaysTillVersionsExpire();
			FitNesse fitnesse = new FitNesse(context);
			if(!arguments.isOmittingUpdates())
				fitnesse.applyUpdates();
			boolean started = fitnesse.start();
			if(started)
				printStartMessage(arguments, context);
		}
		else
		{
			printUsage();
			System.exit(1);
		}
	}

	private static FitNesseContext loadContext(Arguments arguments) throws Exception
	{
		FitNesseContext context = new FitNesseContext();
		ComponentFactory componentFactory = new ComponentFactory(context.rootPath);
		context.port = arguments.getPort();
		context.rootPath = arguments.getRootPath();
		context.rootPageName = arguments.getRootDirectory();
		context.rootPagePath = context.rootPath + "/" + context.rootPageName;
		context.root = componentFactory.getRootPage(FileSystemPage.makeRoot(context.rootPath, context.rootPageName));
		context.responderFactory = new ResponderFactory(context.rootPagePath);
		context.logger = makeLogger(arguments);
		context.authenticator = makeAuthenticator(arguments.getUserpass(), componentFactory);
		context.htmlPageFactory = componentFactory.getHtmlPageFactory(new HtmlPageFactory());

		extraOutput = componentFactory.loadResponderPlugins(context.responderFactory);
		extraOutput += componentFactory.loadWikiWidgetPlugins();
		extraOutput += componentFactory.loadWikiWidgetInterceptors();
		extraOutput += componentFactory.loadContentFilter();

		WikiImportTestEventListener.register();

		return context;
	}

	public static Arguments parseCommandLine(String[] args)
	{
		CommandLine commandLine = new CommandLine("[-p port][-d dir][-r root][-l logDir][-e days][-o][-a userpass]");
		Arguments arguments = null;
		if(commandLine.parse(args))
		{
			arguments = new Arguments();
			if(commandLine.hasOption("p"))
				arguments.setPort(commandLine.getOptionArgument("p", "port"));
			if(commandLine.hasOption("d"))
				arguments.setRootPath(commandLine.getOptionArgument("d", "dir"));
			if(commandLine.hasOption("r"))
				arguments.setRootDirectory(commandLine.getOptionArgument("r", "root"));
			if(commandLine.hasOption("l"))
				arguments.setLogDirectory(commandLine.getOptionArgument("l", "logDir"));
			if(commandLine.hasOption("e"))
				arguments.setDaysTillVersionsExpire(commandLine.getOptionArgument("e", "days"));
			if(commandLine.hasOption("a"))
				arguments.setUserpass(commandLine.getOptionArgument("a", "userpass"));
			arguments.setOmitUpdates(commandLine.hasOption("o"));
		}
		return arguments;
	}

	private static Logger makeLogger(Arguments arguments)
	{
		String logDirectory = arguments.getLogDirectory();
		return logDirectory != null ? new Logger(logDirectory) : null;
	}

	public static Authenticator makeAuthenticator(String authenticationParameter, ComponentFactory componentFactory) throws Exception
	{
		Authenticator authenticator = new PromiscuousAuthenticator();
		if(authenticationParameter != null)
		{
			if(new File(authenticationParameter).exists())
				authenticator = new MultiUserAuthenticator(authenticationParameter);
			else
			{
				String[] values = authenticationParameter.split(":");
				authenticator = new OneUserAuthenticator(values[0], values[1]);
			}
		}

		return componentFactory.getAuthenticator(authenticator);
	}

	private static void printUsage()
	{
		System.err.println("Usage: java fitnesse.FitNesse [-pdrleoa]");
		System.err.println("\t-p <port number> {" + Arguments.DEFAULT_PORT + "}");
		System.err.println("\t-d <working directory> {" + Arguments.DEFAULT_PATH + "}");
		System.err.println("\t-r <page root directory> {" + Arguments.DEFAULT_ROOT + "}");
		System.err.println("\t-l <log directory> {no logging}");
		System.err.println("\t-e <days> {" + Arguments.DEFAULT_VERSION_DAYS + "} Number of days before page versions expire");
		System.err.println("\t-o omit updates");
		System.err.println("\t-a {user:pwd | user-file-name} enable authentication.");
	}

	private static void printStartMessage(Arguments args, FitNesseContext context)
	{
		System.out.println("FitNesse (" + VERSION + ") Started...");
		System.out.print(context.toString());
		System.out.println("\tpage version expiration set to " + args.getDaysTillVersionsExpire() + " days.");
		System.out.print(extraOutput);
	}

	private static void printBadPortMessage(int port)
	{
		System.err.println("FitNesse cannot be started...");
		System.err.println("Port " + port + " is already in use.");
		System.err.println("Use the -p <port#> command line argument to use a different port.");
	}

	private static void establishDirectory(String path)
	{
		File filesDir = new File(path);
		if(!filesDir.exists())
			filesDir.mkdir();
	}

	public FitNesse(FitNesseContext context) throws Exception
	{
		this(context, true);
	}

	//TODO MdM. This boolean agument is annoying... please fix.
	public FitNesse(FitNesseContext context, boolean makeDirs)
	{
		this.context = context;
		context.fitnesse = this;
		if(makeDirs)
			establishRequiredDirectories();
	}

	public boolean start()
	{
		try
		{
			theService = new SocketService(context.port, new FitNesseServer(context));
			return true;
		}
		catch(BindException e)
		{
			printBadPortMessage(context.port);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		return false;
	}

	public void stop() throws Exception
	{
		if(theService != null)
		{
			theService.close();
			theService = null;
		}
	}

	private void establishRequiredDirectories()
	{
		establishDirectory(context.rootPagePath);
		establishDirectory(context.rootPagePath + "/files");
	}

	public void applyUpdates() throws Exception
	{
		Updater updater = new Updater(context);
		updater.update();
	}

	public boolean isRunning()
	{
		return theService != null;
	}
}
