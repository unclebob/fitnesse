// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

import util.StreamReader;
import fitnesse.socketservice.SocketServer;

public class SlimServer implements SocketServer {
  private StreamReader reader;
  private BufferedWriter writer;
  private ListExecutor executor;
  public static final String EXCEPTION_TAG = "__EXCEPTION__:";
  private boolean verbose;

  public SlimServer() {
    this(false);
  }

  public SlimServer(boolean verbose) {
    this.verbose = verbose;
  }

  public void serve(Socket s) {
    try {
      tryProcessInstructions(s);
    } catch (Throwable e) {
    } finally {
      close();
      closeEnclosingServiceInSeperateThread();
    }
  }

  private void closeEnclosingServiceInSeperateThread() {
    new Thread(new Runnable() {
      public void run() {
        try {
          SlimService.instance.close();
        } catch (Exception e) {
        }
      }
    }
    ).start();
  }

  private void tryProcessInstructions(Socket s) throws Exception {
    initialize(s);
    boolean more = true;
    while (more)
      more = processOneSetOfInstructions();
  }

  private void initialize(Socket s) throws IOException {
    executor = new ListExecutor(verbose);
    reader = new StreamReader(s.getInputStream());
    writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"));
    writer.write(String.format("Slim -- %s\n", SlimVersion.VERSION));
    writer.flush();
  }

  private boolean processOneSetOfInstructions() throws Exception {
    String instructions = getInstructionsFromClient();
    if (instructions != null) {
      return processTheInstructions(instructions);
    }
    return true;
  }

  private boolean processTheInstructions(String instructions) throws IOException {
    if (instructions.equalsIgnoreCase("bye")) {
      return false;
    } else {
      List<Object> results = executeInstructions(instructions);
      sendResultsToClient(results);
      return true;
    }
  }

  private String getInstructionsFromClient() throws Exception {
    int instructionLength = Integer.parseInt(reader.read(6));
    reader.read(1);
    String instructions = reader.read(instructionLength);
    return instructions;
  }

  private List<Object> executeInstructions(String instructions) {
    List<Object> statements = ListDeserializer.deserialize(instructions);
    List<Object> results = executor.execute(statements);
    return results;
  }

  private void sendResultsToClient(List<Object> results) throws IOException {
    String resultString = ListSerializer.serialize(results);
    writer.write(String.format("%06d:%s", resultString.getBytes("UTF-8").length, resultString));
    writer.flush();
  }

  private void close() {
    try {
      reader.close();
      writer.close();
    } catch (Exception e) {

    }
  }
}
