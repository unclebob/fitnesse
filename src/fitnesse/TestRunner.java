// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

import fitnesse.http.RequestBuilder;
import fitnesse.http.ResponseParser;

//TODO-MdM DELETE ME SOON
public class TestRunner {
  public URL url;
  public boolean verbose = false;
  public boolean showHtml = false;

  public static void main(String[] args) throws Exception {
    System.out.println("***************************************");
    System.out.println("THIS TEST RUNNER HAS BEEN DEPRECATED!!!");
    System.out.println("Use fitnesse.runner.TestRunner instead.");
    System.out.println("***************************************");
    TestRunner runner = new TestRunner();
    int exitCode = runner.run(args);

    System.exit(exitCode);
  }

  public int run(String[] args) throws Exception {
    int exitCode = -1;
    if (acceptAgrs(args)) {
      printMessage("Running tests at: " + url);
      ResponseParser response = getResponse(url);
      exitCode = getExitCode(response);
      printMessage(exitCode + " failure(s)");
      if (showHtml)
        System.out.println(response.getBody());
    } else
      printUsage();
    return exitCode;
  }

  public int getExitCode(ResponseParser response) throws Exception {
    int retValue = -1;
    String exitCodeString = response.getHeader("Exit-Code");
    if (exitCodeString == null)
      throw new Exception("The response did not contain the needed 'Exit-Code' header.  Was the URL correct?");
    else
      retValue = Integer.parseInt(exitCodeString);

    return retValue;
  }

  private void printMessage(String message) {
    if (verbose) {
      System.out.println(message);
    }
  }

  public ResponseParser getResponse(URL url) throws Exception {
    String resource = url.getPath() + "?" + url.getQuery();
    RequestBuilder request = new RequestBuilder(resource);
    int port = url.getPort() == -1 ? 80 : url.getPort();
    String host = url.getHost();

    Socket socket = null;
    OutputStream output = null;
    InputStream input = null;
    try {
      socket = new Socket(host, port);
      output = socket.getOutputStream();
      output.write(request.getText().getBytes());
      input = socket.getInputStream();
      ResponseParser response = new ResponseParser(input);
      return response;
    } finally {
      output.close();
      input.close();
      socket.close();
    }
  }

  public boolean acceptAgrs(String[] args) throws Exception {
    if (args.length < 1)
      return false;
    try {
      for (int i = 0; i < args.length; i++) {
        String arg = args[i];
        if (arg.startsWith("-")) {
          boolean isValid = setOption(arg.substring(1));
          if (!isValid)
            return false;
        } else {
          boolean isValid = setUrl(arg);
          if (!isValid)
            return false;
        }
      }
      return true;
    }
    catch (Exception e) {
      return false;
    }
  }

  private boolean setOption(String option) {
    boolean isValidOption = true;
    if ("v".equals(option))
      verbose = true;
    else if ("h".equals(option))
      showHtml = true;
    else
      isValidOption = false;

    return isValidOption;
  }

  private boolean setUrl(String urlString) {
    try {
      url = new URL(urlString);
      return true;
    }
    catch (MalformedURLException e) {
      printMessage(e.getMessage());
      return false;
    }
  }

  private void printUsage() {
    System.err.println("Usage: java fitnesse.TestRunner [-vh] <URL>");
    System.err.println("\t-v verbose");
    System.err.println("\t-h show html output");
    System.err.println("\tThe URL should be a FitNesse page test execution, ");
    System.err.println("\tending in either '?suite' or '?test");
  }
}
