// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

import java.io.*;
import java.net.Socket;

import util.CommandLine;
import util.StreamReader;
import util.FileUtil;
import fit.exception.FitParseException;


public class FitServer {
  private final Dispatcher dispatcher;
  private final Counts overallCounts;
  private final FixtureListener fixtureListener;
  
  private OutputStream socketOutput;
  private StreamReader socketReader;
  private boolean verbose = false;
  private String host;
  private int port;
  private int socketToken;

  private Socket socket;
  private boolean noExit;
  private boolean sentinel;

  public FitServer(String host, int port, boolean verbose) {
    this();
    this.host = host;
    this.port = port;
    this.verbose = verbose;
  }

  public FitServer() {
    fixtureListener = new TablePrintingFixtureListener();
    dispatcher = new Dispatcher(fixtureListener);
    overallCounts = new Counts();
  }

  public static void main(String argv[]) throws Exception {
    FitServer fitServer = new FitServer();
    fitServer.run(argv);
    if (!fitServer.noExit)
      System.exit(fitServer.exitCode());
  }

  public void run(String argv[]) throws Exception {
    args(argv);
    File sentinelFile = null;
    if (sentinel) {
      String sentinelName = sentinelName(port);
      sentinelFile = new File(sentinelName);
      sentinelFile.createNewFile();
    }
    establishConnection();
    validateConnection();
    process();
    closeConnection();
    if (sentinel)
      FileUtil.deleteFile(sentinelFile);
    exit();
  }

  public void args(String[] argv) {
    CommandLine commandLine = new CommandLine("[-v][-x][-s] host port socketToken");
    if (commandLine.parse(argv)) {
      host = commandLine.getArgument("host");
      port = Integer.parseInt(commandLine.getArgument("port"));
      socketToken = Integer.parseInt(commandLine.getArgument("socketToken"));
      verbose = commandLine.hasOption("v");
      noExit = commandLine.hasOption("x");
      sentinel = commandLine.hasOption("s");
    } else
      usage();
  }

  private void usage() {
    System.out.println("usage: java fit.FitServer [-v] host port socketTicket");
    System.out.println("\t-v\tverbose");
    System.exit(-1);
  }

  public static String sentinelName(int thePort) {
    return String.format("fitserverSentinel%d", thePort);
  }

  public void closeConnection() throws IOException {
    socket.close();
  }

  public void process() {
    try {
      int size = 1;
      while ((size = FitProtocol.readSize(socketReader)) != 0) {
        try {
          print("processing document of size: " + size + "\n");
          String document = FitProtocol.readDocument(socketReader, size);
          //TODO MDM if the page name was always the first line of the body, it could be printed here.
          Parse tables = new Parse(document);
          dispatcher.doTables(tables);
          print("\tresults: " + dispatcher.counts.toString() + "\n");
          overallCounts.tally(dispatcher.counts);
        } catch (FitParseException e) {
          exception(e);
        }
      }
      print("completion signal recieved" + "\n");
    } catch (Exception e) {
      exception(e);
    }
  }

  protected void exception(Exception e) {
    print("Exception occurred!" + "\n");
    print("\t" + e.getMessage() + "\n");
    Parse tables = new Parse("span", "Exception occurred: ", null, null);
    dispatcher.exception(tables, e);
    overallCounts.exceptions += 1;
    fixtureListener.tableFinished(tables);
    fixtureListener.tablesFinished(dispatcher.counts);
  }

  public void exit() throws Exception {
    print("exiting" + "\n");
    print("\tend results: " + overallCounts.toString() + "\n");
  }

  public int exitCode() {
    return overallCounts.wrong + overallCounts.exceptions;
  }

  public void establishConnection() throws Exception {
    establishConnection(makeHttpRequest());
  }

  public void establishConnection(String httpRequest) throws Exception {
    socket = new Socket(host, port);
    socketOutput = socket.getOutputStream();
    socketReader = new StreamReader(socket.getInputStream());
    byte[] bytes = httpRequest.getBytes("UTF-8");
    socketOutput.write(bytes);
    socketOutput.flush();
    print("http request sent" + "\n");
  }

  private String makeHttpRequest() {
    return "GET /?responder=socketCatcher&ticket=" + socketToken + " HTTP/1.1\r\n\r\n";
  }

  public void validateConnection() throws Exception {
    print("validating connection...");
    int statusSize = FitProtocol.readSize(socketReader);
    if (statusSize == 0)
      print("...ok" + "\n");
    else {
      String errorMessage = FitProtocol.readDocument(socketReader, statusSize);
      print("...failed because: " + errorMessage + "\n");
      System.out.println("An error occurred while connecting to client.");
      System.out.println(errorMessage);
      System.exit(-1);
    }
  }

  private void print(String message) {
    if (verbose)
      System.out.print(message);
  }

  public static byte[] readTable(Parse table) throws Exception {
    ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
    OutputStreamWriter streamWriter = new OutputStreamWriter(byteBuffer, "UTF-8");
    PrintWriter writer = new PrintWriter(streamWriter);
    Parse more = table.more;
    table.more = null;
    if (table.trailer == null)
      table.trailer = "";
    table.print(writer);
    table.more = more;
    writer.close();
    return byteBuffer.toByteArray();
  }

  class TablePrintingFixtureListener implements FixtureListener {
    public void tableFinished(Parse table) {
      try {
        byte[] bytes = readTable(table);
        if (bytes.length > 0)
          FitProtocol.writeData(bytes, socketOutput);
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    public void tablesFinished(Counts count) {
      try {
        FitProtocol.writeCounts(count, socketOutput);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
