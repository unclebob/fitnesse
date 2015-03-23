// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import fitnesse.slim.protocol.SlimDeserializer;
import fitnesse.slim.protocol.SlimSerializer;
import fitnesse.socketservice.SocketFactory;
import fitnesse.socketservice.SocketServer;

import java.io.IOException;
import java.io.OutputStream;
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
  public static final String TIMED_OUT = "TIMED_OUT";
  public static final String EXCEPTION_TAG = "__EXCEPTION__:";
  public static final String EXCEPTION_STOP_TEST_TAG = "__EXCEPTION__:ABORT_SLIM_TEST:";
  public static final String EXCEPTION_STOP_SUITE_TAG = "__EXCEPTION__:ABORT_SLIM_SUITE:";

  private SlimStreamReader reader;
  private OutputStream writer;
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
    SocketFactory.printSocketInfo(s);
    reader = SlimStreamReader.getReader(s);
    writer = SlimStreamReader.getByteWriter(s);
    executor = slimFactory.getListExecutor(verbose);
    SlimStreamReader.sendSlimHeader(writer, String.format(SlimVersion.SLIM_HEADER + SlimVersion.VERSION + "\n"));
  }

  private boolean processOneSetOfInstructions() throws IOException {
    String instructions = reader.getSlimMessage();
    // Not sure why this is need but we keep it.
    if (instructions == null) return true;
    // We are done Bye Bye message received
    if (instructions.equalsIgnoreCase(SlimVersion.BYEMESSAGE)) {
      return false;
    }

    // Do some real work
    String resultString = executeInstructions(instructions);
    SlimStreamReader.sendSlimMessage(writer, resultString);
    return true;
  }

  private String executeInstructions(String instructions) {
    List<Object> statements = SlimDeserializer.deserialize(instructions);
    List<Object> results = executor.execute(statements);
    String resultString = SlimSerializer.serialize(results);
    return resultString;
  }

  private void close() {
    try {
      reader.close();
      writer.close();
    } catch (Exception e) {

    }
  }

}
