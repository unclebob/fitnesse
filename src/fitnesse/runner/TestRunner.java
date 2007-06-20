// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import fit.*;
import fitnesse.components.CommandLine;

import java.io.*;
import java.lang.reflect.Method;
import java.net.*;
import java.util.*;

public class TestRunner
{
	private String host;
	private int port;
	private String pageName;
	private FitServer fitServer;
	public TestRunnerFixtureListener fixtureListener;
	public CachingResultFormatter handler;
	private PrintStream output;
	public List<FormattingOption> formatters = new LinkedList<FormattingOption>();
	private boolean debug;
	public boolean verbose;
	public boolean usingDownloadedPaths = true;
	private String suiteFilter = null;

	public TestRunner() throws Exception
	{
		this(System.out);
	}

	public TestRunner(PrintStream output) throws Exception
	{
		this.output = output;
		handler = new CachingResultFormatter();
	}

	public static void main(String[] args) throws Exception
	{
		TestRunner runner = new TestRunner();
		runner.run(args);
		System.exit(runner.exitCode());
	}

	public void args(String[] args) throws Exception
	{
		CommandLine commandLine = new CommandLine("[-debug] [-v] [-results file] [-html file] [-xml file] [-nopath] [-suiteFilter filter] host port pageName");
		if(!commandLine.parse(args))
			usage();

		host = commandLine.getArgument("host");
		port = Integer.parseInt(commandLine.getArgument("port"));
		pageName = commandLine.getArgument("pageName");

		if(commandLine.hasOption("debug"))
			debug = true;
		if(commandLine.hasOption("v"))
		{
			verbose = true;
			handler.addHandler(new StandardResultHandler(output));
		}
		if(commandLine.hasOption("nopath"))
			usingDownloadedPaths = false;
		if(commandLine.hasOption("results"))
			formatters.add(new FormattingOption("raw", commandLine.getOptionArgument("results", "file"), output, host, port, pageName));
		if(commandLine.hasOption("html"))
			formatters.add(new FormattingOption("html", commandLine.getOptionArgument("html", "file"), output, host, port, pageName));
		if(commandLine.hasOption("xml"))
			formatters.add(new FormattingOption("xml", commandLine.getOptionArgument("xml", "file"), output, host, port, pageName));

		if(commandLine.hasOption("suiteFilter"))
			suiteFilter = commandLine.getOptionArgument("suiteFilter", "filter");
	}

	private void usage()
	{
		System.out.println("usage: java fitnesse.runner.TestRunner [options] host port page-name");
		System.out.println("\t-v \tverbose: prints test progress to stdout");
		System.out.println("\t-results <filename|'stdout'>\tsave raw test results to a file or dump to standard output");
		System.out.println("\t-html <filename|'stdout'>\tformat results as HTML and save to a file or dump to standard output");
		System.out.println("\t-debug \tprints FitServer protocol actions to stdout");
		System.out.println("\t-nopath \tprevents downloaded path elements from being added to classpath");
		System.exit(-1);
	}

	public void run(String[] args) throws Exception
	{
		args(args);
		fitServer = new FitServer(host, port, debug);
		fixtureListener = new TestRunnerFixtureListener(this);
		fitServer.fixtureListener = fixtureListener;
		fitServer.establishConnection(makeHttpRequest());
		fitServer.validateConnection();
		if(usingDownloadedPaths)
			processClasspathDocument();
		fitServer.process();
		finalCount();
		fitServer.closeConnection();
		fitServer.exit();
		doFormatting();
		handler.cleanUp();
	}

	private void processClasspathDocument() throws Exception
	{
		String classpathItems = fitServer.readDocument();
		if(verbose)
			output.println("Adding to classpath: " + classpathItems);
		addItemsToClasspath(classpathItems);
	}

	private void finalCount() throws Exception
	{
		handler.acceptFinalCount(fitServer.getCounts());
	}

	public int exitCode()
	{
		return fitServer == null ? -1 : fitServer.exitCode();
	}

	public String makeHttpRequest()
	{
		String request = "GET /" + pageName + "?responder=fitClient";
		if(usingDownloadedPaths)
			request += "&includePaths=yes";
		if(suiteFilter != null)
		{
			request += "&suiteFilter=" + suiteFilter;
		}
		return request + " HTTP/1.1\r\n\r\n";
	}

	public Counts getCounts()
	{
		return fitServer.getCounts();
	}

	public void acceptResults(PageResult results) throws Exception
	{
		Counts counts = results.counts();
		fitServer.writeCounts(counts);
		handler.acceptResult(results);
	}

	public void doFormatting() throws Exception
	{
		for(Iterator iterator = formatters.iterator(); iterator.hasNext();)
		{
			FormattingOption option = (FormattingOption) iterator.next();
			if(verbose)
				output.println("Formatting as " + option.format + " to " + option.filename);
			option.process(handler.getResultStream(), handler.getByteCount());
		}
	}

	public static void addItemsToClasspath(String classpathItems) throws Exception
	{
		String[] items = classpathItems.split(System.getProperty("path.separator"));
		for(int i = 0; i < items.length; i++)
		{
			String item = items[i];
			addUrlToClasspath(new File(item).toURL());
		}
	}

	public static void addUrlToClasspath(URL u) throws Exception
	{
		URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		Class sysclass = URLClassLoader.class;
		Method method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
		method.setAccessible(true);
		method.invoke(sysloader, new Object[]{u});
	}
}
