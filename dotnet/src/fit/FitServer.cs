// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.IO;
using System.Text;
using System.Net;
using System.Net.Sockets;
using System.Collections;

namespace fit 
{
	public class FitServer : FixtureListener
	{
		private Socket clientSocket;
		private StreamReader stdinReader;
		private StreamWriter stdoutWriter;
		private bool verbose = false;
		private string host;
		private int port;
		private string socketToken;

		public static int Main(string[] CommandLineArguments)
		{
			return new FitServer().Run(CommandLineArguments);
		}


		private void ParseCommandLineArguments(string[] args)
		{
			const int ASSEMBLYLIST = 0;
			const int HOST = 1;
			const int PORT = 2;
			const int SOCKET_TOKEN = 3;
			const int DONE = 4;
			int argumentPosition = 0;

			for(int i = 0; i < args.Length; i++)
			{
				if (args[i].StartsWith("-")) 
				{
					if("-v".Equals(args[i]))
						verbose = true;
					else
						PrintUsageAndExit();
				}
				else
				{
					switch(argumentPosition)
					{
						case ASSEMBLYLIST: ParseAssemblyList(args[i]); break;
						case HOST: host = args[i]; break;
						case PORT: port = Int32.Parse(args[i]); break;
						case SOCKET_TOKEN: socketToken = args[i]; break;
					}
					argumentPosition++;
				}
			}
			if (argumentPosition != DONE)
				PrintUsageAndExit();
		}


		private void PrintUsageAndExit()
		{
			Console.Error.WriteLine("Program usage:");
			Console.Error.WriteLine("\tFitServer [-v] <assemblies>\n");
			Console.Error.WriteLine("\t-v\tverbose: print log messages to stdout");
			Console.Error.WriteLine("\tassemblies: ';' separated list of assembly filenames");
			Environment.Exit(1);
		}


		private void ParseAssemblyList(string assemblies)
		{
			foreach(string assembly in assemblies.Split(';'))
				Fixture.AddAssembly(assembly);
		}

		public int Run(string[] CommandLineArguments)
		{
			CreateStdInOutAccessors();
			ParseCommandLineArguments(CommandLineArguments);

			EstablishConnection();
			ValidateConnection();

			int errorCount = ProcessTestDocuments();
			return errorCount;
		}


		private void CreateStdInOutAccessors()
		{
			stdinReader = new StreamReader(Console.OpenStandardInput());
			stdoutWriter = new StreamWriter(Console.OpenStandardOutput());
		}


		private void EstablishConnection()
		{
			WriteLogMessage("Host:Port:\t" + host + ":" + port);

			string httpRequest = "GET /?responder=socketCatcher&ticket=" + socketToken + " HTTP/1.1\r\n\r\n";
			WriteLogMessage ("\tHTTP request: " + httpRequest);

			clientSocket = SocketConnectionTo(host, port);
			TransmitRawString(httpRequest);
		}


		private void ValidateConnection()
		{
			WriteLogMessage("Validating connection...");
			int StatusSize = ReceiveInteger();
			if(StatusSize == 0)
				WriteLogMessage("\t...ok\n");
			else 
			{
				String errorMessage = ReceiveStringOfLength(StatusSize);
				WriteLogMessage("\t...failed bacuase: " + errorMessage);
				Console.WriteLine("An error occured while connecting to client.");
				Console.WriteLine(errorMessage);
				Environment.Exit(-1);
			}
		}


		private int ProcessTestDocuments()
		{
			string document = "";
			Counts totalCounts = new Counts();

			while ( (document = ReceiveDocument()).Length > 0  )
			{
				WriteLogMessage("processing document of size: " + document.Length);
				Counts currentCounts = ProcessTestDocument(document);
				totalCounts.Tally(currentCounts);
				WriteLogMessage("\tresults: " + currentCounts);
			}
			WriteLogMessage("\ncompletion signal recieved");

			return totalCounts.wrong + totalCounts.exceptions;
		}


		private Counts ProcessTestDocument(string document)
		{
			Fixture fixture = RunTest(document);
			return fixture.counts;
		}

		private Fixture RunTest(string document)
		{
			Fixture fixture = new Fixture();
			try 
			{
				Parse parse = new Parse(document);
				fixture.listener = this;
				fixture.DoTables(parse);
				return fixture;
			}
			catch (Exception e)
			{
				Parse parse = new Parse("body","Unable to parse input. Input ignored.", null, null);
				fixture.Exception(parse, e);
				TableFinished(parse);
				return fixture;
			}
		}

		public void TableFinished(Parse finishedTable) 
		{
			string testResultDocument = FirstTableOf(finishedTable);
			WriteLogMessage ("\tTransmitting table of length " + testResultDocument.Length);
			TransmitDocument(testResultDocument);
		}

		private string FirstTableOf(Parse tables)
		{
			Parse more = tables.more;
			tables.more = null;
			StringWriter writer = new StringWriter();
			tables.Print(writer);
			string firstTable = writer.ToString();
			tables.more = more;
			return firstTable;
		}


		public void TablesFinished(Counts counts) 
		{
			WriteLogMessage ("\tTest Document finished");
			TransmitInteger(0);
			TransmitCounts(counts);
		}

		private void WriteLogMessage(string logMessage)
		{
			if (verbose)
				Console.WriteLine(logMessage);
		}

		private static Socket SocketConnectionTo(string hostName, int Port) 
		{
			Socket clientSocket = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);

			IPAddress hostAddress = Dns.GetHostByName(hostName).AddressList[0];
			IPEndPoint hostEndPoint = new IPEndPoint(hostAddress, Port);
			clientSocket.Connect(hostEndPoint);

			return clientSocket;		
		}


		private string ReceiveDocument()
		{
			int documentLength = ReceiveInteger();
			if (documentLength == 0) return "";
			return ReceiveStringOfLength(documentLength);
		}


		private int ReceiveInteger()
		{
			return DecodeInteger(ReceiveStringOfLength(10));
		}


		private string ReceiveStringOfLength(int stringLength)
		{
			byte[] stringBytes = new byte[stringLength];
			char[] stringCharacters = new char[stringLength];
			clientSocket.Receive(stringBytes);
			Encoding.ASCII.GetDecoder().GetChars(stringBytes, 0, stringLength, stringCharacters, 0);
			return new StringBuilder(stringLength).Append(stringCharacters).ToString();
		}


		private void TransmitDocument(string document) 
		{
			TransmitRawString(EncodeString(document));
		}


		private void TransmitInteger(int transmit) 
		{
			TransmitRawString(EncodeInteger(transmit));
		}


		private void TransmitRawString(string message)
		{
			byte[] messageBytes = Encoding.ASCII.GetBytes(message);
			clientSocket.Send(messageBytes);
		}


		private void TransmitCounts(Counts counts) 
		{
			TransmitInteger(counts.right);
			TransmitInteger(counts.wrong);
			TransmitInteger(counts.ignores);
			TransmitInteger(counts.exceptions);
		}


		public static string EncodeInteger(int encodeInteger)
		{
			string numberPartOfString = "" + encodeInteger;
			return new String('0', 10 - numberPartOfString.Length) + numberPartOfString;
		}


		public static int DecodeInteger(string encodedInteger)
		{
			return Convert.ToInt32(encodedInteger);
		}


		public static string EncodeString(string document)
		{
			return EncodeInteger(document.Length) + document;
		}

		private static string ReadFixedLengthString(StreamReader reader, int stringLength)
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
			writer.Write(EncodeString(writeContent));
			writer.Flush();
		}


		public string ReadFrom(StreamReader reader)
		{
			int contentLength = ReadIntegerFrom(reader);
			return ReadFixedLengthString(reader, contentLength);
		}
	}
}