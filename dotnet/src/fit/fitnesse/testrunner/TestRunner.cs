// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.IO;

namespace fit
{
	public class TestRunner
	{
		public string pageName;
		public bool usingDownloadedPaths;
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
					if("-v".Equals(option))
						verbose = true;
					if("-debug".Equals(option))
						debug = true;
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
			if(verbose && (counts.Wrong > 0 || counts.Exceptions > 0))
			{
				output.WriteLine();
				if(counts.Wrong > 0)
					output.WriteLine(PageDescription(results) + " has failures");
				if(counts.Exceptions > 0)
					output.WriteLine(PageDescription(results) + " has errors");
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
			else if("".Equals(filename))
			{
				cacheFilename = "FitNesse" + new Random().Next() + ".results";
				cacheWriter = new StreamWriter(File.OpenWrite(cacheFilename));
				deleteCacheOnExit = true;
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
			cacheWriter.Write(results.ToString() + "\n");
		}

		public void CacheFinalCount(Counts counts)
		{
			if(cacheWriter != null)
				cacheWriter.Write(Protocol.FormatCounts(counts));
		}

		public void CleanResultCache()
		{
			cacheWriter.Close();
			if(deleteCacheOnExit)
				File.Delete(cacheFilename);
		}
	}
}