// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import fitnesse.slim.protocol.SlimDeserializer;
import fitnesse.slim.protocol.SlimSerializer;
import fitnesse.socketservice.SocketServer;
import util.FileUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;


/**
 * Handle Slim requests.
 *
 * Note this class is re-entrant (do not keep instance state!)
 */
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
  public static final String EXCEPTION_IGNORE_SCRIPT_TEST_TAG = "__EXCEPTION__:IGNORE_SCRIPT_TEST:";
  public static final String EXCEPTION_IGNORE_ALL_TESTS_TAG = "__EXCEPTION__:IGNORE_ALL_TESTS:";
  public static final String EXCEPTION_STOP_SUITE_TAG = "__EXCEPTION__:ABORT_SLIM_SUITE:";

  private final SlimFactory slimFactory;

  public SlimServer(SlimFactory slimFactory) {
    this.slimFactory = slimFactory;
  }

  @Override
  public void serve(Socket s) throws IOException {
    SlimStreamReader reader = null;
    OutputStream writer = null;
    try {
      reader = SlimStreamReader.getReader(s);
      writer = SlimStreamReader.getByteWriter(s);
      tryProcessInstructions(reader, writer);
    } finally {
      slimFactory.stop();
      FileUtil.close(reader);
      FileUtil.close(writer);
    }
  }

  private void tryProcessInstructions(SlimStreamReader reader, OutputStream writer) throws IOException {
    ListExecutor executor = slimFactory.getListExecutor();
    String header = SlimVersion.SLIM_HEADER + SlimVersion.VERSION + "\n";
    SlimStreamReader.sendSlimHeader(writer, header);

    boolean more = true;
    while (more)
      more = processOneSetOfInstructions(reader, writer, executor);
  }

  private boolean processOneSetOfInstructions(SlimStreamReader reader, OutputStream writer, ListExecutor executor) throws IOException {
    String instructions = reader.getSlimMessage();
    // Not sure why this is need but we keep it.
    if (instructions == null) return true;
    // We are done Bye Bye message received
    if (instructions.equalsIgnoreCase(SlimVersion.BYEMESSAGE)) {
      return false;
    }

    // Do some real work
    String resultString = executeInstructions(executor, instructions);
    SlimStreamReader.sendSlimMessage(writer, resultString);
    return true;
  }

  private String executeInstructions(ListExecutor executor, String instructions) {
    List<Object> statements = SlimDeserializer.deserialize(instructions);
    List<Object> results = executor.execute(statements);
    return SlimSerializer.serialize(results);
  }

}
