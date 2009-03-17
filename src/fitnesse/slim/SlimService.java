// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.util.Arrays;

import util.CommandLine;
import fitnesse.socketservice.SocketService;

public class SlimService extends SocketService {
  public static SlimService instance = null;
  public static boolean verbose;
  public static int port;

  public static void main(String[] args) throws Exception {
    if (parseCommandLine(args)) {
      new SlimService(port, verbose);
    } else {
      System.err.println("Invalid command line arguments:" + Arrays.asList(args));
    }
  }

  static boolean parseCommandLine(String[] args) {
    CommandLine commandLine = new CommandLine("[-v] port");
    if (commandLine.parse(args)) {
      verbose = commandLine.hasOption("v");
      String portString = commandLine.getArgument("port");
      port = Integer.parseInt(portString);
      return true;
    }
    return false;
  }

  public SlimService(int port) throws Exception {
    this(port, false);
  }

  public SlimService(int port, boolean verbose) throws Exception {
    super(port, new SlimServer(verbose));
    instance = this;
  }
}
