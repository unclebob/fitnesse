// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (C) 2003,2004 by Robert C. Martin and Micah D. Martin. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import java.io.*;
import java.net.Socket;
import fitnesse.util.StreamReader;
import fitnesse.components.*;
import fit.exception.FitParseException;

public class FitServer
{
	public String input;
	public Parse tables;
	public Fixture fixture = new Fixture();
	public FixtureListener fixtureListener = new TablePrintingFixtureListener();
	private Counts counts = new Counts();
	private OutputStream socketOutput;
	private StreamReader socketReader;
	private boolean verbose = false;
  private String host;
  private int port;
  private int socketToken;;
  private Socket socket;

  public FitServer(String host, int port, boolean verbose)
	{
		this.host = host;
		this.port = port;
		this.verbose = verbose;
	}

	public FitServer()
	{
	}

	public static void main(String argv[]) throws Exception
	{
		FitServer fitServer = new FitServer();
		fitServer.run(argv);
		System.exit(fitServer.exitCode());
	}

	public void run(String argv[]) throws Exception
	{
		args(argv);
		establishConnection();
		validateConnection();
		process();
		closeConnection();
    exit();
	}

  public void closeConnection() throws IOException
  {
    socket.close();
  }

  public void process()
	{
		fixture.listener = fixtureListener;
		try
		{
			int size = 1;
			while((size = FitProtocol.readSize(socketReader)) != 0)
			{
				try
				{
					print("processing document of size: " + size + "\n");
					String document = FitProtocol.readDocument(socketReader, size);
					//TODO MDM if the page name was always the first line of the body, it could be printed here.
					tables = new Parse(document);
					newFixture().doTables(tables);
					print("\tresults: " + fixture.counts() + "\n");
					counts.tally(fixture.counts);
				}
				catch(FitParseException e)
				{
					exception(e);
				}
			}
			print("completion signal recieved" + "\n");
		}
		catch(Exception e)
		{
			exception(e);
		}
	}

	public String readDocument() throws Exception
	{
		int size = FitProtocol.readSize(socketReader);
		String document = FitProtocol.readDocument(socketReader, size);
		return document;
	}

	private Fixture newFixture()
	{
		fixture = new Fixture();
		fixture.listener = fixtureListener;
		return fixture;
	}

	public void args(String[] argv)
	{
		CommandLine commandLine = new CommandLine("[-v] host port socketToken");
		if(commandLine.parse(argv))
		{
			host = commandLine.getArgument("host");
			port = Integer.parseInt(commandLine.getArgument("port"));
			socketToken = Integer.parseInt(commandLine.getArgument("socketToken"));
			verbose = commandLine.hasOption("v");
		}
		else
			usage();
	}

  private void usage()
  {
    System.out.println("usage: java fit.FitServer [-v] host port socketTicket");
    System.out.println("\t-v\tverbose");
    System.exit(-1);
  }

	protected void exception(Exception e)
	{
		print("Exception occurred!" + "\n");
		print("\t" + e.getMessage() + "\n");
		tables = new Parse("span", "Exception occurred: ", null, null);
		fixture.exception(tables, e);
		counts.exceptions += 1;
		fixture.listener.tableFinished(tables);
		fixture.listener.tablesFinished(counts); //TODO shouldn't this be fixture.counts
	}

	public void exit() throws Exception
	{
		print("exiting" + "\n");
		print("\tend results: " + counts.toString() + "\n");
	}

	public int exitCode()
	{
		return counts.wrong + counts.exceptions;
	}

	public void establishConnection() throws Exception
	{
		establishConnection(makeHttpRequest());
	}

	public void establishConnection(String httpRequest) throws Exception
	{
    socket = new Socket(host, port);
		socketOutput = socket.getOutputStream();
		socketReader = new StreamReader(socket.getInputStream());
		byte[] bytes = httpRequest.getBytes("UTF-8");
		socketOutput.write(bytes);
		socketOutput.flush();
		print("http request sent" + "\n");
	}

	private String makeHttpRequest()
  {
		return "GET /?responder=socketCatcher&ticket=" + socketToken + " HTTP/1.1\r\n\r\n";
  }

  public void validateConnection() throws Exception
	{
		print("validating connection...");
		int statusSize = FitProtocol.readSize(socketReader);
		if(statusSize == 0)
			print("...ok" + "\n");
		else
		{
			String errorMessage = FitProtocol.readDocument(socketReader, statusSize);
			print("...failed bacuase: " + errorMessage + "\n");
			System.out.println("An error occured while connecting to client.");
			System.out.println(errorMessage);
			System.exit(-1);
		}
	}

	public Counts getCounts()
	{
		return counts;
	}

	private void print(String message)
	{
		if(verbose)
			System.out.print(message);
	}

	public static byte[] readTable(Parse table) throws Exception
	{
		ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
		OutputStreamWriter streamWriter = new OutputStreamWriter(byteBuffer, "UTF-8");
		PrintWriter writer = new PrintWriter(streamWriter);
		Parse more = table.more;
		table.more = null;
		if(table.trailer == null)
			table.trailer = "";
		table.print(writer);
		table.more = more;
		writer.close();
		return byteBuffer.toByteArray();
	}

	public void writeCounts(Counts count) throws IOException
	{
		//TODO This can't be right.... which counts should be used?
		FitProtocol.writeCounts(counts, socketOutput);
	}

	class TablePrintingFixtureListener implements FixtureListener
	{
		public void tableFinished(Parse table)
		{
			try
			{
				byte[] bytes = readTable(table);
				if(bytes.length > 0)
					FitProtocol.writeData(bytes, socketOutput);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}

		public void tablesFinished(Counts count)
		{
			try
			{
				FitProtocol.writeCounts(count, socketOutput);
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}

