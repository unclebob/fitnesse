// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import fitnesse.slim.protocol.SlimDeserializer;
import fitnesse.slim.protocol.SlimSerializer;
import fitnesse.socketservice.SocketServer;
import util.StreamReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

public class SlimServer implements SocketServer {
  public static final String MALFORMED_INSTRUCTION = "MALFORMED_INSTRUCTION";
  public static final String NO_CLASS = "NO_CLASS";
  public static final String NO_INSTANCE = "NO_INSTANCE";
  public static final String NO_CONVERTER_FOR_ARGUMENT_NUMBER = "NO_CONVERTER_FOR_ARGUMENT_NUMBER";
  public static final String NO_CONSTRUCTOR = "NO_CONSTRUCTOR";
  public static final String NO_METHOD_IN_CLASS = "NO_METHOD_IN_CLASS";
  public static final String COULD_NOT_INVOKE_CONSTRUCTOR = "COULD_NOT_INVOKE_CONSTRUCTOR";
  public static final String EXCEPTION_TAG = "__EXCEPTION__:";
  public static final String EXCEPTION_STOP_TEST_TAG = "__EXCEPTION__:ABORT_SLIM_TEST:";

  private StreamReader reader;
  private BufferedWriter writer;
  private ListExecutor executor;
  private boolean verbose;
  private SlimFactory slimFactory;

  public SlimServer(boolean verbose, SlimFactory slimFactory) {
    this.verbose = verbose;
    this.slimFactory = slimFactory;
  }

  public void serve(Socket s) {
    try {
      tryProcessInstructions(s);
    } catch (Throwable e) {
      System.err.println("Error while executing SLIM instructions: " + e.getMessage());
      e.printStackTrace(System.err);
    } finally {
      slimFactory.stop();
      close();
    }
  }

  private void tryProcessInstructions(Socket s) throws IOException {
    initialize(s);
    boolean more = true;
    while (more)
      more = processOneSetOfInstructions();
  }

  private void initialize(Socket s) throws IOException {
    executor = slimFactory.getListExecutor(verbose);
    reader = new StreamReader(s.getInputStream());
    writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream(), "UTF-8"));
    writer.write(String.format("Slim -- V%s\n", SlimVersion.VERSION));
    writer.flush();
  }

  private boolean processOneSetOfInstructions() throws IOException {
    String instructions = getInstructionsFromClient();
    if (instructions != null) {
      return processTheInstructions(instructions);
    }
    return true;
  }

  private String getInstructionsFromClient() throws IOException {
    String data = reader.read(6);
    int instructionLength;
    try {
      instructionLength = Integer.parseInt(data);
    } catch (NumberFormatException e) {
      System.err.println("Number format error: " + data);
      throw e;
    }
    String colon = reader.read(1);
    if (!":".equals(colon)) {
      throw new SlimError("protocol error: expected colon after message length field: " + colon);
    }
    String instructions = reader.read(instructionLength);
    return instructions;
  }

  private boolean processTheInstructions(String instructions) throws IOException {
    if (instructions.equalsIgnoreCase("bye")) {
      return false;
    } else {
      List<Object> results = executeInstructions(instructions);
      // TODO: -AJM- Move sendResultsToClient() call to processOneSetOfInstructions()
      // Put I/O in one location.
      sendResultsToClient(results);
      return true;
    }
  }

  private List<Object> executeInstructions(String instructions) {
    List<Object> statements = SlimDeserializer.deserialize(instructions);
    // TODO: -AJM- Statements to instructions, then execute those.
    // Returns: List of InstructionResult (id, String/list<Object>)
    // ListExecutor becomes InstructionExecutor?
    List<Object> results = executor.execute(statements);
    return results;
  }

  private void sendResultsToClient(List<Object> results) throws IOException {
    String resultString = SlimSerializer.serialize(results);
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
