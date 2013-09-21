// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.runner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import util.CommandLine;
import util.FileUtil;
import fitnesse.http.RequestBuilder;
import fitnesse.http.ResponseParser;

public class FormattingOption {
  public String format;
  public boolean usingStdout = false;
  public OutputStream output;
  public String host;
  public int port;
  public String rootPath;
  private int status;
  public String filename;
  private String resultFilename;

  public static void main(String[] args) throws Exception {
    FormattingOption option = new FormattingOption();
    option.args(args);
    File inputFile = new File(option.resultFilename);
    FileInputStream input = new FileInputStream(inputFile);
    int byteCount = (int) inputFile.length();
    option.process(input, byteCount);
  }

  private FormattingOption() {
  }

  private void args(String[] args) throws Exception {
    CommandLine commandLine = new CommandLine("resultFilename format outputFilename host port rootPath");
    if (!commandLine.parse(args))
      usage();
    resultFilename = commandLine.getArgument("resultFilename");
    format = commandLine.getArgument("format");
    filename = commandLine.getArgument("outputFilename");
    host = commandLine.getArgument("host");
    port = Integer.parseInt(commandLine.getArgument("port"));
    rootPath = commandLine.getArgument("rootPath");
    setOutput(System.out);
  }

  private void usage() {
    System.out.println("java fitnesse.runner.FormattingOption resultFilename format outputFilename host port rootPath");
    System.out.println("\tresultFilename:\tthe name of the file containing test results");
    System.out.println("\tformat:        \traw|html|xml|...");
    System.out.println("\toutputfilename:\tstdout|a filename where the formatted results are to be stored");
    System.out.println("\thost:          \tthe domain name of the hosting FitNesse server");
    System.out.println("\tport:          \tthe port on which the hosting FitNesse server is running");
    System.out.println("\trootPath:      \tname of the test page or suite page");
    System.exit(-1);
  }

  public FormattingOption(String format, String filename, OutputStream stdout, String host, int port, String rootPath) throws Exception {
    this.format = format;
    this.filename = filename;
    setOutput(stdout);
    this.host = host;
    this.port = port;
    this.rootPath = rootPath;
  }

  private void setOutput(OutputStream stdout) throws FileNotFoundException {
    if ("stdout".equals(filename)) {
      this.output = stdout;
      this.usingStdout = true;
    } else
      this.output = new FileOutputStream(filename);
  }

  public void process(InputStream inputStream, int size) throws Exception {
    try {
      if ("raw".equals(format))
        FileUtil.copyBytes(inputStream, output);
      else {
        RequestBuilder request = buildRequest(inputStream, size);
        ResponseParser response = ResponseParser.performHttpRequest(host, port, request);
        status = response.getStatus();
        output.write(response.getBody().getBytes("UTF-8"));
      }
    } finally {
      if (!usingStdout)
        output.close();
    }
  }

  public boolean wasSuccessful() {
    return status == 200;
  }

  public RequestBuilder buildRequest(InputStream inputStream, int size) throws Exception {
    RequestBuilder request = new RequestBuilder("/" + rootPath);
    request.setMethod("POST");
    request.setHostAndPort(host, port);
    request.addInput("responder", "format");
    request.addInput("format", format);
    request.addInputAsPart("results", inputStream, size, "text/plain");
    return request;
  }
}
