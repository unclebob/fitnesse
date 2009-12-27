// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.runner;

import fitnesse.http.RequestBuilder;
import fitnesse.responders.run.TestSummary;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import util.CommandLine;
import util.StreamReader;
import util.StringUtil;
import util.XmlUtil;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.Arrays;

public class TestRunner {
  private String outputFileName;
  private String host;
  private int port;
  private String pageName;
  private PrintStream output;
  private String suiteFilter = null;
  private String excludeSuiteFilter = null;
  private String credentials = null;
  private StreamReader socketReader;
  private Document testResultsDocument;
  private TestSummary counts;
  private boolean verbose;
  private boolean debug = false;
  private String request;
  private String xmlDocumentString;

  public TestRunner() throws Exception {
    this(System.out);
  }

  public TestRunner(PrintStream output) throws Exception {
    this.output = output;
  }

  public static void main(String[] args) throws Exception {
    System.out.println("***************************************");
    System.out.println("THIS TEST RUNNER HAS BEEN DEPRECATED!!!");
    System.out.println("Use java -jar fitnesse.jar -c \"REST-COMMAND\" instead.");
    System.out.println("***************************************");
    TestRunner runner = new TestRunner();
    runner.run(args);
    System.exit(runner.exitCode());
  }

  public void args(String[] args) throws Exception {
    CommandLine commandLine = new CommandLine("[-v] [-debug] [-xml file] [-suiteFilter filter] [-excludeSuiteFilter excludeFilter] [-credentials userpass] host port pageName");
    if (!commandLine.parse(args))
      usage();

    host = commandLine.getArgument("host");
    port = Integer.parseInt(commandLine.getArgument("port"));
    pageName = commandLine.getArgument("pageName");

    if (commandLine.hasOption("v"))
      verbose = true;
    if (commandLine.hasOption("debug"))
      debug = true;
    if (commandLine.hasOption("xml"))
      outputFileName = commandLine.getOptionArgument("xml", "file");
    if (commandLine.hasOption("suiteFilter"))
      suiteFilter = commandLine.getOptionArgument("suiteFilter", "filter");
    if (commandLine.hasOption("excludeSuiteFilter"))
      excludeSuiteFilter = commandLine.getOptionArgument("excludeSuiteFilter", "excludeFilter");
    if (commandLine.hasOption("credentials"))
      credentials = commandLine.getOptionArgument("credentials", "userpass");
  }

  private void usage() {
    System.out.println("usage: java fitnesse.runner.TestRunner [options] host port page-name");
    System.out.println("\t-v\tPrint test results.");
    System.out.println("\t-xml <file>\t Write XML test results to file.  If file is 'stdout' write to standard out");
    System.out.println("\t-suiteFilter <filter> \texecutes only tests which are flagged with the given filter");
    System.out.println("\t-credentials <user:pwd> \tAuthenticates with given credentials when sending commands to the server");

    System.exit(-1);
  }

  public void run(String[] args) throws Exception {
    args(args);
    debug(String.format("Args: %s", StringUtil.join(Arrays.asList(args), " ")));
    requestTest();
    debug(String.format("Sent request: %s", request));
    discardHeaders();
    xmlDocumentString = getXmlDocument();
    debug(String.format("Xml Document: %s", xmlDocumentString));
    testResultsDocument = XmlUtil.newDocument(xmlDocumentString);
    debug("Xml Document Parsed");
    gatherCounts();
    writeOutputFile();
    verboseOutput();
    debug(String.format("Exit Code: %d", exitCode()));
  }

  private void debug(String message) {
    if (debug) {
      output.println(message);
    }
  }

  private void verboseOutput() throws Exception {
    if (verbose) {
      TestSummary pageCounts = new TestSummary();
      TestSummary suiteSummary = new TestSummary();
      Element testResultsElement = testResultsDocument.getDocumentElement();
      String rootPath = XmlUtil.getTextValue(testResultsElement, "rootPath");
      output.println(String.format("Test Runner for Root Path: %s", rootPath));
      NodeList results = testResultsElement.getElementsByTagName("result");
      for (int i = 0; i < results.getLength(); i++) {
        Element result = (Element) results.item(i);
        TestSummary pageSummary = showResult(result);
        suiteSummary.add(pageSummary);
        pageCounts.tallyPageCounts(pageSummary);
      }
      output.println("Test Pages: " + pageCounts);
      output.println("Assertions: " + suiteSummary);
    }
  }

  private TestSummary showResult(Element result) throws Exception {
    String page = XmlUtil.getTextValue(result, "relativePageName");
    Element counts = XmlUtil.getElementByTagName(result, "counts");
    int right = Integer.parseInt(XmlUtil.getTextValue(counts, "right"));
    int wrong = Integer.parseInt(XmlUtil.getTextValue(counts, "wrong"));
    int ignores = Integer.parseInt(XmlUtil.getTextValue(counts, "ignores"));
    int exceptions = Integer.parseInt(XmlUtil.getTextValue(counts, "exceptions"));
    String pageHistoryLink = XmlUtil.getTextValue(result, "pageHistoryLink");
    TestSummary testSummary = new TestSummary(right, wrong, ignores, exceptions);
    String marker = (wrong > 0 || exceptions > 0) ? "*" : " ";
    output.println(String.format("%s Page:%s right:%d, wrong:%d, ignored:%d, exceptions:%d | %s",
      marker, page, right, wrong, ignores, exceptions, pageHistoryLink));
    return testSummary;
  }

  private void writeOutputFile() throws Exception {
    if (outputFileName != null) {
      debug(String.format("Writing: %s", outputFileName));
      ;
      OutputStream os = getOutputStream();
      os.write(xmlDocumentString.getBytes());
      os.close();
    } else {
      debug("No output file to write.");
    }
  }

  private OutputStream getOutputStream() throws FileNotFoundException {
    if ("stdout".equalsIgnoreCase(outputFileName))
      return output;
    else
      return new FileOutputStream(outputFileName);
  }

  private void gatherCounts() throws Exception {
    debug("Gathering Counts...");
    Element testResults = testResultsDocument.getDocumentElement();
    Element finalCounts = XmlUtil.getElementByTagName(testResults, "finalCounts");
    String right = XmlUtil.getTextValue(finalCounts, "right");
    String wrong = XmlUtil.getTextValue(finalCounts, "wrong");
    String ignores = XmlUtil.getTextValue(finalCounts, "ignores");
    String exceptions = XmlUtil.getTextValue(finalCounts, "exceptions");
    counts = new TestSummary(Integer.parseInt(right), Integer.parseInt(wrong), Integer.parseInt(ignores), Integer.parseInt(exceptions));
    debug(String.format("Counts: %s", counts.toString()));
  }

  public int exitCode() {
    int exitStatus = 0;
    if (counts.getWrong() > 0)
      exitStatus++;
    if (counts.getExceptions() > 0)
      exitStatus++;

    return exitStatus;
  }

  private String getXmlDocument() throws Exception {
    StringBuffer xmlDocumentBuffer = new StringBuffer();
    while (true) {
      String sizeLine = socketReader.readLine();
      if (sizeLine.equals(""))
        continue;
      int size = Integer.parseInt(sizeLine, 16);
      if (size == 0)
        break;
      String chunk = socketReader.read(size);
      xmlDocumentBuffer.append(chunk);
    }
    return xmlDocumentBuffer.toString();
  }

  private void discardHeaders() throws Exception {
    while (true) {
      String line = socketReader.readLine();
      debug("Discarding header: " + line);
      if (line.equals(""))
        break;
    }
  }

  private void requestTest() throws Exception {
    Socket socket = new Socket(host, port);
    OutputStream socketOutput = socket.getOutputStream();
    socketReader = new StreamReader(socket.getInputStream());
    request = makeHttpRequest();
    byte[] bytes = request.getBytes("UTF-8");
    socketOutput.write(bytes);
    socketOutput.flush();
  }

  public String makeHttpRequest() throws Exception {
    String requestUrl = makeRequestUrl();
    return buildRequestFromUrl(requestUrl);
  }

  private String buildRequestFromUrl(String requestUrl) throws Exception {
    RequestBuilder requestBuilder = new RequestBuilder(requestUrl);
    if (credentialsSpecified()) {
      String[] userPass = credentials.split(":");
      requestBuilder.addCredentials(userPass[0], userPass[1]);
    }
    return requestBuilder.getText();
  }

  private boolean credentialsSpecified() {
    return credentials != null;
  }

  private String makeRequestUrl() {
    String requestUrl = "/" + pageName + "?responder=suite";
    if (suiteFilter != null)
      requestUrl += "&suiteFilter=" + suiteFilter;
    if (excludeSuiteFilter != null)
      requestUrl += "&excludeSuiteFilter=" + excludeSuiteFilter;
    requestUrl += "&format=xml";
    return requestUrl;
  }

  public TestSummary getCounts() throws Exception {
    return counts;
  }

}
