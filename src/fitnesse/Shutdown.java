// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import util.CommandLine;
import fitnesse.http.RequestBuilder;
import fitnesse.http.Response;
import fitnesse.http.ResponseParser;

public class Shutdown {
  public String hostname = "localhost";
  public int port = Arguments.DEFAULT_PORT;
  public String username;
  public String password;
  private CommandLine commandLine = new CommandLine("[-h hostname] [-p port] [-c username password]");

  public static void main(String[] args) throws Exception {
    Shutdown shutdown = new Shutdown();
    shutdown.run(args);
  }

  private void run(String[] args) throws Exception {
    if (!parseArgs(args))
      usage();

    ResponseParser response = buildAndSendRequest();

    String status = checkResponse(response);
    if (!"OK".equals(status)) {
      System.err.println("Failed to shutdown.");
      System.err.println(status);
      System.exit(response.getStatus());
    }
  }

  public ResponseParser buildAndSendRequest() throws Exception {
    RequestBuilder request = buildRequest();
    ResponseParser response = ResponseParser.performHttpRequest(hostname, port, request);
    return response;
  }

  public RequestBuilder buildRequest() throws Exception {
    RequestBuilder request = new RequestBuilder("/?responder=shutdown");
    if (username != null)
      request.addCredentials(username, password);
    return request;
  }

  public String checkResponse(ResponseParser response) {
    int status = response.getStatus();
    String serverHeader = response.getHeader("Server");
    if (serverHeader == null || serverHeader.indexOf("FitNesse") == -1)
      return "Not a FitNesse server";
    else if (status != 200)
      return status + " " + Response.getReasonPhrase(status);
    else
      return "OK";
  }

  public boolean parseArgs(String[] args) {
    if (!commandLine.parse(args))
      return false;

    if (commandLine.hasOption("h"))
      hostname = commandLine.getOptionArgument("h", "hostname");
    if (commandLine.hasOption("p"))
      port = Integer.parseInt(commandLine.getOptionArgument("p", "port"));
    if (commandLine.hasOption("c")) {
      username = commandLine.getOptionArgument("c", "username");
      password = commandLine.getOptionArgument("c", "password");
    }
    return true;
  }

  public void usage() {
    System.err.println("Usage: java fitnesse.Shutdown [-hpc]");
    System.err.println("\t-h <hostname> {localhost}");
    System.err.println("\t-p <port number> {" + Arguments.DEFAULT_PORT + "}");
    System.err.println("\t-c <username> <password> Supply user credentials.  Use when FitNesse has authentication activated.");
    System.exit(-1);
  }
}
