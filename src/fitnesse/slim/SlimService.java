// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import fitnesse.slim.fixtureInteraction.DefaultInteraction;
import util.CommandLine;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class SlimService {
	static boolean verbose;
	static int port;
	static String interactionClassName = null;

	private final ServerSocket serverSocket;
	private final SlimServer slimServer;
  static Thread service;

  public static void main(String[] args) throws IOException {
		if (parseCommandLine(args)) {
			startWithFactory(new JavaSlimFactory());
		} else {
			parseCommandLineFailed(args);
		}
	}

	protected static void parseCommandLineFailed(String[] args) {
		System.err.println("Invalid command line arguments:"
				+ Arrays.asList(args));
	}

	public static void startWithFactory(SlimFactory slimFactory) throws IOException {
		SlimService slimservice = new SlimService(slimFactory.getSlimServer(verbose));
		slimservice.accept();
	}

	public static void startWithFactoryAsync(SlimFactory slimFactory) throws IOException {
      if (service != null && service.isAlive()) {
        System.err.println("Already an in-process server running: " + service.getName() + " (alive=" + service.isAlive() + ")");
        service.interrupt();
        throw new RuntimeException("Already an in-process server running: " + service.getName() + " (alive=" + service.isAlive() + ")");
      }
      final SlimService slimservice = new SlimService(slimFactory.getSlimServer(verbose));
      service = new Thread() {
          public void run() {
              try {
                  slimservice.accept();
              } catch (IOException e) {
                  throw new RuntimeException(e);
              }
          }
      };
      service.start();
	}

  // For testing, mainly.
  public static void waitForServiceToStopAsync() throws InterruptedException {
    // wait for service to close.
    for (int i = 0; i < 1000; i++) {
      if (!service.isAlive())
        break;
      Thread.sleep(50);
    }
  }

	public static boolean parseCommandLine(String[] args) {
		CommandLine commandLine = new CommandLine(
				"[-v] [-i interactionClass] port ");
		if (commandLine.parse(args)) {
			verbose = commandLine.hasOption("v");
			interactionClassName = commandLine.getOptionArgument("i",
					"interactionClass");
			String portString = commandLine.getArgument("port");
			port = (portString == null) ? 8099 : Integer.parseInt(portString);
			return true;
		}
		return false;
	}

	public SlimService(SlimServer slimServer) throws IOException {
		this.slimServer = slimServer;

		try {
			serverSocket = tryCreateServerSocket(port);
		} catch (java.lang.OutOfMemoryError e) {
			System.err.println("Out of Memory. Aborting");
			e.printStackTrace();
			System.exit(99);

			throw e;
		}
	}

	private ServerSocket tryCreateServerSocket(int port) throws IOException {
		try
		{
			return new ServerSocket(port);
		} catch (IOException e) {
			System.out.println("IO exception on port = " + port);
			e.printStackTrace();
			throw e;
		}
	}

	public void accept() throws IOException {
		Socket socket = null;
		try{
			socket = serverSocket.accept();
			slimServer.serve(socket);
		} catch (java.lang.OutOfMemoryError e) {
			System.err.println("Out of Memory. Aborting");
			e.printStackTrace();
			System.exit(99);
		} finally {
			if (socket != null) {
				socket.close();
			}
			serverSocket.close();
		}
	}

	@SuppressWarnings("unchecked")
	public static Class<DefaultInteraction> getInteractionClass() {
		if (interactionClassName == null) {
			return DefaultInteraction.class;
		}
		try {
			return (Class<DefaultInteraction>) Class
					.forName(interactionClassName);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
