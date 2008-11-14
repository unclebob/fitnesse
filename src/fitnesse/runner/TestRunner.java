// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import fit.Counts;
import fit.FitServer;
import fitnesse.components.CommandLine;
import fitnesse.responders.run.TestSummary;
import fitnesse.util.StreamReader;
import fitnesse.util.XmlUtil;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.Socket;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class TestRunner {
  private String outputFileName;
  private String host;
  private int port;
  private String pageName;
  private PrintStream output;
  private String suiteFilter = null;
  private StreamReader socketReader;
  private Document testResultsDocument;
  private TestSummary counts;
  private boolean verbose;

  public TestRunner() throws Exception {
    this(System.out);
  }

  public TestRunner(PrintStream output) throws Exception {
    this.output = output;
  }

  public static void main(String[] args) throws Exception {
    TestRunner runner = new TestRunner();
    runner.run(args);
    System.exit(runner.exitCode());
  }

  public void args(String[] args) throws Exception {
    CommandLine commandLine = new CommandLine("[-v] [-xml file] [-suiteFilter filter] host port pageName");
    if (!commandLine.parse(args))
      usage();

    host = commandLine.getArgument("host");
    port = Integer.parseInt(commandLine.getArgument("port"));
    pageName = commandLine.getArgument("pageName");

    if (commandLine.hasOption("v"))
      verbose = true;
    if (commandLine.hasOption("xml"))
      outputFileName = commandLine.getOptionArgument("xml", "file");
    if (commandLine.hasOption("suiteFilter"))
      suiteFilter = commandLine.getOptionArgument("suiteFilter", "filter");
  }

  private void usage() {
    System.out.println("usage: java fitnesse.runner.TestRunner [options] host port page-name");
    System.out.println("\t-v\tPrint test results.");
    System.out.println("\t-suiteFilter <filter> \texecutes only tests which are flagged with the given filter");

    System.exit(-1);
  }

  public void run(String[] args) throws Exception {
    args(args);
    requestTest();
    discardHeaders();
    String xmlDocumentString = getXmlDocument();
    testResultsDocument = XmlUtil.newDocument(xmlDocumentString);
    gatherCounts();
    writeOutputFile();
    verboseOutput();
  }

  private void verboseOutput() throws Exception {
    if (verbose) {
      Element testResultsElement = testResultsDocument.getDocumentElement();
      String rootPath = XmlUtil.getTextValue(testResultsElement, "rootPath");
      output.println(String.format("Test Runner for Root Path: %s", rootPath));
      NodeList results = testResultsElement.getElementsByTagName("result");
      for (int i=0; i<results.getLength(); i++) {
        Element result = (Element) results.item(i);
        showResult(result);
      }
    }
  }

  private void showResult(Element result) throws Exception {
    String page = XmlUtil.getTextValue(result, "relativePageName");
    Element counts = XmlUtil.getElementByTagName(result, "counts");
    int right = Integer.parseInt(XmlUtil.getTextValue(counts, "right"));
    int wrong = Integer.parseInt(XmlUtil.getTextValue(counts, "wrong"));
    int ignores = Integer.parseInt(XmlUtil.getTextValue(counts, "ignores"));
    int exceptions = Integer.parseInt(XmlUtil.getTextValue(counts, "exceptions"));
    output.println(String.format("Page:%s right:%d, wrong:%d, ignored:%d, exceptions:%d", page, right, wrong, ignores, exceptions));
  }

  private void writeOutputFile() throws Exception {
    if (outputFileName != null) {
      String xmlDocument = XmlUtil.xmlAsString(testResultsDocument);
      OutputStream os = getOutputStream();
      os.write(xmlDocument.getBytes());
      os.close();
    }
  }

  private OutputStream getOutputStream() throws FileNotFoundException {
    if ("stdout".equalsIgnoreCase(outputFileName))
      return output;
    else
     return new FileOutputStream(outputFileName);
  }

  private void gatherCounts() throws Exception {
    Element testResults = testResultsDocument.getDocumentElement();
    Element finalCounts = XmlUtil.getElementByTagName(testResults, "finalCounts");
    String right = XmlUtil.getTextValue(finalCounts, "right");
    String wrong = XmlUtil.getTextValue(finalCounts, "wrong");
    String ignores = XmlUtil.getTextValue(finalCounts, "ignores");
    String exceptions = XmlUtil.getTextValue(finalCounts, "exceptions");
    counts = new TestSummary(Integer.parseInt(right), Integer.parseInt(wrong), Integer.parseInt(ignores), Integer.parseInt(exceptions));
  }

  public int exitCode() {
    int exitStatus = 0;
    if (counts.wrong > 0)
      exitStatus++;
    if (counts.exceptions > 0)
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
      if (line.equals(""))
        break;
    }
  }

  private void requestTest() throws IOException {
    Socket socket = new Socket(host, port);
    OutputStream socketOutput = socket.getOutputStream();
    socketReader = new StreamReader(socket.getInputStream());
    byte[] bytes = makeHttpRequest().getBytes("UTF-8");
    socketOutput.write(bytes);
    socketOutput.flush();
  }

  public String makeHttpRequest() {
    String request = "GET /" + pageName + "?responder=suite";
    if (suiteFilter != null)
      request += "&suiteFilter=" + suiteFilter;
    request += "&format=xml";

    return request + " HTTP/1.1\r\n\r\n";
  }

  public TestSummary getCounts() throws Exception {
    return counts;
  }

  public static void addItemsToClasspath(String classpathItems) throws Exception {
    final String separator = System.getProperty("path.separator");
    System.setProperty("java.class.path", System.getProperty("java.class.path") + separator + classpathItems);
    String[] items = classpathItems.split(separator);
    for (int i = 0; i < items.length; i++) {
      String item = items[i];
      addUrlToClasspath(new File(item).toURI().toURL());
    }
  }

  public static void addUrlToClasspath(URL u) throws Exception {
    URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
    Class<URLClassLoader> sysclass = URLClassLoader.class;
    Method method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
    method.setAccessible(true);
    method.invoke(sysloader, new Object[]{u});
  }
}
