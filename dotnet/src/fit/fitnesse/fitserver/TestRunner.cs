// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.IO;
using fit;

namespace fitnesse.fitserver
{
	public class TestRunner
	{
		public string pageName;
		public bool usingDownloadedPaths = true;
		private FitServer fitServer;
		private FixtureListener fixtureListener;
		public string host;
		public int port;
		public bool debug;
		public bool verbose;
		public string cacheFilename;
		public TextWriter cacheWriter;
		public bool deleteCacheOnExit;
		public Counts pageCounts = new Counts();
		public TextWriter output = Console.Out;

		public static int Main(string[] args)
		{
			TestRunner runner = new TestRunner();
			runner.Run(args);
			return runner.ExitCode();
		}

		public void Run(string[] args)
		{
			if(!ParseArgs(args))
			{
				PrintUsage();
				return;
			}
			fitServer = new FitServer(host, port, debug);
			fixtureListener = new TestRunnerFixtureListener(this);
			fitServer.fixtureListener = fixtureListener;
			fitServer.EstablishConnection(MakeHttpRequest());
			fitServer.ValidateConnection();
			if(usingDownloadedPaths)
				ProcessAssembliesDocument();
			fitServer.ProcessTestDocuments();
			HandleFinalCount(fitServer.Counts);
			fitServer.CloseConnection();
			fitServer.Exit();
			CleanResultCache();
		}

		public int ExitCode()
		{
			return fitServer == null ? -1 : fitServer.ExitCode();
		}

		public bool ParseArgs(string[] args)
		{
			int index = 0;
			try
			{
				while(args[index].StartsWith("-"))
				{
					string option = args[index++];
					if("-results".Equals(option))
						CreateCacheStream(args[index++]);
					else if("-v".Equals(option))
						verbose = true;
					else if("-debug".Equals(option))
						debug = true;
					else if("-nopaths".Equals(option))
						usingDownloadedPaths = false;
					else
						throw new Exception("Bad option: " + option);
				}
				host = args[index];
				port = Int32.Parse(args[index + 1]);
				pageName = args[index + 2];
				return true;
			}
			catch(Exception)
			{
				return false;
			}
		}

		private void ProcessAssembliesDocument()
		{
			String assemblyPaths = fitServer.ReceiveDocument();
			if(verbose)
				output.WriteLine("Adding assemblies: " + assemblyPaths);
			fitServer.ParseAssemblyList(assemblyPaths);	
		}

		private void PrintUsage()
		{
			Console.WriteLine("Usage: TestRunner [options] <host> <port> <page name>");
			Console.WriteLine("\t-v\tverbose: prints test progress to colsole");
			Console.WriteLine("\t-debug\tprints FitServer actions to console");
			Console.WriteLine("\t-nopaths\tprevents addition of assemblies from FitNesse");
			Console.WriteLine("\t-results <filename|'stdout'>\tsends test results data to the specified file or the console");
		}

		public string MakeHttpRequest()
		{
			string request = "GET /" + pageName + "?responder=fitClient";
			if(usingDownloadedPaths)
				request += "&includePaths=yes";
			return request + " HTTP/1.1\r\n\r\n";
		}

		public void AcceptResults(PageResult results)
		{
			Counts counts = results.Counts();
			pageCounts.TallyPageCounts(counts);
			fitServer.Transmit(Protocol.FormatCounts(counts));
			if(verbose)
			{
				for(int i = 0; i < counts.Right; i++)
					output.Write(".");
				if(counts.Wrong > 0)
				{
					output.WriteLine();
					output.WriteLine(PageDescription(results) + " has failures");
				}
				if(counts.Exceptions > 0)
				{
					output.WriteLine();
					output.WriteLine(PageDescription(results) + " has errors");
				}

			}
			CacheResults(results);
		}

		private string PageDescription(PageResult result)
		{
			String description = result.Title();
			if("".Equals(description))
				description = "The test";
			return description;
		}

		public void CreateCacheStream(string filename)
		{
			if("stdout".Equals(filename))
			{
				cacheFilename = "stdout";
				cacheWriter = Console.Out;
				deleteCacheOnExit = false;
			}
			else
			{
				cacheFilename = filename;
				cacheWriter = new StreamWriter(File.OpenWrite(cacheFilename));
				deleteCacheOnExit = false;
			}
		}

		public void HandleFinalCount(Counts counts)
		{
			if(verbose)
			{
				output.WriteLine();
				output.WriteLine("Test Pages: " + pageCounts);
				output.WriteLine("Assertions: " + counts);
			}
			CacheFinalCount(counts);
		}

		public void CacheResults(PageResult results)
		{
			if(cacheWriter != null)
			{
				string data = results.ToString() + "\n";
				cacheWriter.Write(Protocol.FormatDocument(data));
			}
		}

		public void CacheFinalCount(Counts counts)
		{
			if(cacheWriter != null)
				cacheWriter.Write(Protocol.FormatCounts(counts));
		}

		public void CleanResultCache()
		{
			if(cacheWriter != null)
			{
				cacheWriter.Close();
				if(deleteCacheOnExit)
					File.Delete(cacheFilename);
			}
		}
	}
}