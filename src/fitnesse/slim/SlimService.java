// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.io.IOException;
import java.util.Arrays;

import fitnesse.slim.fixtureInteraction.DefaultInteraction;
import util.CommandLine;
import fitnesse.socketservice.SocketService;

public class SlimService extends SocketService {
  public static SlimService instance = null;
  public static boolean verbose;
  public static int port;

  protected static String interactionClassName = null;

  public static void main(String[] args) throws IOException {
    if (parseCommandLine(args)) {
      startWithFactory(args, new JavaSlimFactory());
    } else {
      parseCommandLineFailed(args);
    }
  }

  protected static void parseCommandLineFailed(String[] args) {
    System.err.println("Invalid command line arguments:" + Arrays.asList(args));
  }

  protected static void startWithFactory(String[] args, SlimFactory slimFactory) throws IOException {
    new SlimService(port, slimFactory.getSlimServer(verbose));
  }

  protected static boolean parseCommandLine(String[] args) {
    CommandLine commandLine = new CommandLine("[-v] [-i interactionClass] port ");
    if (commandLine.parse(args)) {
      verbose = commandLine.hasOption("v");
      interactionClassName = commandLine.getOptionArgument("i", "interactionClass");
      String portString = commandLine.getArgument("port");
      port = (portString == null) ? 8099 :Integer.parseInt(portString);
      return true;
    }
    return false;
  }

  public SlimService(int port, SlimServer slimServer) throws IOException  {
    super(port, slimServer);
    instance = this;
  }

  public static Class<DefaultInteraction> getInteractionClass() {
    if(interactionClassName==null){
      return DefaultInteraction.class;
    }
    try {
      return (Class<DefaultInteraction>) Class.forName(interactionClassName);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
