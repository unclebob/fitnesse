// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Text;
using fit;

namespace fitnesse.fitserver
{
	public class FitServer
	{
		private Socket clientSocket;
		private bool verbose = false;
		private string host;
		private int port;
		private string socketToken;
		public FixtureListener fixtureListener;
		private Counts totalCounts = new Counts();

		private const int ASSEMBLYLIST = 0;
		private const int HOST = 1;
		private const int PORT = 2;
		private const int SOCKET_TOKEN = 3;
		private const int DONE = 4;

		public static int Main(string[] CommandLineArguments)
		{
			FitServer fitServer = new FitServer();
			fitServer.Run(CommandLineArguments);
			return fitServer.ExitCode();
		}

		public FitServer()
		{
			fixtureListener = new TablePrintingFixtureListener(this);
		}

		public FitServer(string host, int port, bool verbose) : this()
		{
			this.host = host;
			this.port = port;
			this.verbose = verbose;
		}

		private void ParseCommandLineArguments(string[] args)
		{
			int argumentPosition = 0;

			for (int i = 0; i < args.Length; i++)
			{
				if (args[i].StartsWith("-"))
				{
					if ("-v".Equals(args[i]))
						verbose = true;
					else
						PrintUsageAndExit();
				}
				else
				{
					switch (argumentPosition)
					{
						case ASSEMBLYLIST:
							ParseAssemblyList(args[i]);
							break;
						case HOST:
							host = args[i];
							break;
						case PORT:
							port = Int32.Parse(args[i]);
							break;
						case SOCKET_TOKEN:
							socketToken = args[i];
							break;
					}
					argumentPosition++;
				}
			}
			if (argumentPosition != DONE)
				PrintUsageAndExit();
		}

		private void PrintUsageAndExit()
		{
			Console.Error.WriteLine("Usage: FitServer [-v] <assemblies> <host> <port> <socket-token>");
			Console.Error.WriteLine("\t-v\tverbose: print log messages to stdout");
			Console.Error.WriteLine("\tassemblies:\t';' separated list of assembly filenames");
			Environment.Exit(1);
		}

		public void ParseAssemblyList(string path)
		{
			PathParser parser = new PathParser(path);
			foreach (string assemblyPath in parser.AssemblyPaths)
				ObjectFactory.AddAssembly(assemblyPath);
			if (parser.HasConfigFilePath())
				AppDomain.CurrentDomain.SetData("APP_CONFIG_FILE",parser.ConfigFilePath);
		}

		public int Run(string[] CommandLineArguments)
		{
			ParseCommandLineArguments(CommandLineArguments);

			EstablishConnection();
			ValidateConnection();

			int errorCount = ProcessTestDocuments();
			CloseConnection();
			Exit();
			return errorCount;
		}

		public void CloseConnection()
		{
			clientSocket.Close();
		}

		public void Exit()
		{
			WriteLogMessage("exiting...");
			WriteLogMessage("End results: " + totalCounts);
		}

		private void EstablishConnection()
		{
			WriteLogMessage("Host:Port:\t" + host + ":" + port);

			string httpRequest = "GET /?responder=socketCatcher&ticket=" + socketToken + " HTTP/1.1\r\n\r\n";
			EstablishConnection(httpRequest);
		}

		public void EstablishConnection(string request)
		{
			WriteLogMessage("\tHTTP request: " + request);

			clientSocket = SocketConnectionTo(host, port);
			Transmit(request);
		}

		public void ValidateConnection()
		{
			WriteLogMessage("Validating connection...");
			int StatusSize = ReceiveInteger();
			if (StatusSize == 0)
				WriteLogMessage("\t...ok\n");
			else
			{
				String errorMessage = SocketUtils.ReceiveStringOfLength(new SocketWrapper(clientSocket), StatusSize);
				WriteLogMessage("\t...failed bacuase: " + errorMessage);
				Console.WriteLine("An error occured while connecting to client.");
				Console.WriteLine(errorMessage);
				Environment.Exit(-1);
			}
		}

		public int ProcessTestDocuments()
		{
			string document = "";

			while ((document = ReceiveDocument()).Length > 0)
			{
				WriteLogMessage("processing document of size: " + document.Length);
				Counts currentCounts = ProcessTestDocument(document);
				totalCounts.Tally(currentCounts);
				WriteLogMessage("\tresults: " + currentCounts);
			}
			WriteLogMessage("\ncompletion signal recieved");

			return ExitCode();
		}

		public int ExitCode()
		{
			return totalCounts.Wrong + totalCounts.Exceptions;
		}

		private Counts ProcessTestDocument(string document)
		{
			Fixture fixture = RunTest(document);
			return fixture.Counts;
		}

		private Fixture RunTest(string document)
		{
			Fixture fixture = new Fixture();
			try
			{
				Parse parse = new Parse(document);
				fixture.Listener = fixtureListener;
				fixture.DoTables(parse);
				return fixture;
			}
			catch (Exception e)
			{
				Parse parse = new Parse("body", "Unable to parse input. Input ignored.", null, null);
				fixture.Exception(parse, e);
				fixtureListener.TableFinished(parse);
				return fixture;
			}
		}

		public static string FirstTableOf(Parse tables)
		{
			Parse more = tables.More;
			tables.More = null;
			StringWriter writer = new StringWriter();
			tables.Print(writer);
			string firstTable = writer.ToString();
			tables.More = more;
			return firstTable;
		}

		public void WriteLogMessage(string logMessage)
		{
			if (verbose)
				Console.WriteLine(logMessage);
		}

		private Socket SocketConnectionTo(string hostName, int port)
		{
			Socket clientSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
			IPAddress hostAddress = Dns.GetHostByName(hostName).AddressList[0];
			IPEndPoint hostEndPoint = new IPEndPoint(hostAddress, port);
			clientSocket.Connect(hostEndPoint);
			return clientSocket;
		}

		public string ReceiveDocument()
		{
			int documentLength = ReceiveInteger();
			if (documentLength == 0)
				return "";
			return SocketUtils.ReceiveStringOfLength(new SocketWrapper(clientSocket), documentLength);
		}

		public int ReceiveInteger()
		{
			return DecodeInteger(SocketUtils.ReceiveStringOfLength(new SocketWrapper(clientSocket), 10));
		}

		public void Transmit(string message)
		{
			byte[] messageBytes = Encoding.ASCII.GetBytes(message);
			clientSocket.Send(messageBytes);
		}

		public int DecodeInteger(string encodedInteger)
		{
			return Convert.ToInt32(encodedInteger);
		}

		private string ReadFixedLengthString(StreamReader reader, int stringLength)
		{
			char[] numberCharacters = new char[stringLength];
			reader.Read(numberCharacters, 0, stringLength);

			return new StringBuilder(stringLength).Append(numberCharacters).ToString();
		}

		public int ReadIntegerFrom(StreamReader reader)
		{
			return DecodeInteger(ReadFixedLengthString(reader, 10));
		}

		public void WriteTo(StreamWriter writer, string writeContent)
		{
			writer.Write(Protocol.FormatDocument(writeContent));
			writer.Flush();
		}

		public string ReadFrom(StreamReader reader)
		{
			int contentLength = ReadIntegerFrom(reader);
			return ReadFixedLengthString(reader, contentLength);
		}

		public Counts Counts
		{
			get
			{
				return totalCounts;
			}
		}
	}

	public class TablePrintingFixtureListener : FixtureListener
	{
		private FitServer fitServer;

		public TablePrintingFixtureListener(FitServer fitServer)
		{
			this.fitServer = fitServer;
		}

		public void TableFinished(Parse finishedTable)
		{
			string testResultDocument = FitServer.FirstTableOf(finishedTable);
			fitServer.WriteLogMessage("\tTransmitting table of length " + testResultDocument.Length);
			fitServer.Transmit(Protocol.FormatDocument(testResultDocument));
		}

		public void TablesFinished(Counts counts)
		{
			fitServer.WriteLogMessage("\tTest Document finished");
			fitServer.Transmit(Protocol.FormatCounts(counts));
		}
		
	}
}