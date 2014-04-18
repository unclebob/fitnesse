// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import fitnesse.slim.SlimError;
import fitnesse.slim.SlimException;
import fitnesse.slim.instructions.*;
import fitnesse.slim.protocol.SlimDeserializer;
import fitnesse.slim.protocol.SlimSerializer;
import fitnesse.testsystems.CommandRunner;
import fitnesse.testsystems.CommandRunnerExecutionLog;
import fitnesse.testsystems.ExecutionLog;
import util.ListUtility;
import util.StreamReader;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static util.ListUtility.list;

public class SlimCommandRunningClient implements SlimClient {
  private static final Logger LOG = Logger.getLogger(SlimCommandRunningClient.class.getName());

  public static final int NO_SLIM_SERVER_CONNECTION_FLAG = -32000;
  public static double MINIMUM_REQUIRED_SLIM_VERSION = 0.3;

  private final CommandRunner slimRunner;
  private final int connectionTimeout;
  private Socket client;
  private StreamReader reader;
  private BufferedWriter writer;
  private String slimServerVersionMessage;
  private double slimServerVersion;
  private String hostName;
  private int port;


  public SlimCommandRunningClient(CommandRunner slimRunner, String hostName, int port, int connectionTimeout) {
    this.slimRunner = slimRunner;
    this.hostName = hostName;
    this.port = port;
    this.connectionTimeout = connectionTimeout;
  }

  public SlimCommandRunningClient(CommandRunner slimRunner, String hostName, int port) {
    this(slimRunner, hostName, port, 10);
  }

  @Override
  public void start() throws IOException {
    slimRunner.asynchronousStart();
    connect();
    checkForVersionMismatch();
  }

  private void checkForVersionMismatch() {
    double serverVersionNumber = getServerVersion();
    if (serverVersionNumber == NO_SLIM_SERVER_CONNECTION_FLAG) {
      throw new SlimError("Slim Protocol Version Error: Server did not respond with a valid version number.");
    }
    else if (serverVersionNumber < MINIMUM_REQUIRED_SLIM_VERSION) {
      throw new SlimError(String.format("Slim Protocol Version Error: Expected V%s but was V%s", MINIMUM_REQUIRED_SLIM_VERSION, serverVersionNumber));
    }
  }

  @Override
  public void kill() throws IOException {
    if (slimRunner != null)
      slimRunner.kill();
    if (reader != null)
      reader.close();
    if (writer != null)
      writer.close();
    if (client != null)
      client.close();
  }

  @Override
  public void connect() throws IOException {
    int maxTries = connectionTimeout * 20; // wait time is 50 ms
    while (client == null) {
      client = tryConnect(maxTries--);
    }
    reader = new StreamReader(client.getInputStream());
    writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8"));
    slimServerVersionMessage = reader.readLine();
    validateConnection();
  }

  private void validateConnection() {
    if (isConnected()) {
      slimServerVersion = Double.parseDouble(slimServerVersionMessage.replace("Slim -- V", ""));
    }
    else {
      slimServerVersion =  NO_SLIM_SERVER_CONNECTION_FLAG;
      LOG.warning("Error reading Slim Version. Read the following: " + slimServerVersionMessage);
    }
  }

  private Socket tryConnect(int maxTries) throws IOException {
    try {
      return new Socket(hostName, port);
    } catch (IOException e) {
      if (maxTries <= 1) {
        throw new SlimError("Error connecting to SLiM server on " + hostName + ":" + port, e);
      } else {
        try {
          Thread.sleep(50);
        } catch (InterruptedException i) {
          throw new SlimError("Wait for connection interrupted.");
        }
        return null;
      }
    }
  }

  public double getServerVersion() {
    return slimServerVersion;
  }

  public boolean isConnected() {
    return slimServerVersionMessage.startsWith("Slim -- V");
  }

  @Override
  public Map<String, Object> invokeAndGetResponse(List<Instruction> statements) throws IOException {
    if (statements.size() == 0)
      return new HashMap<String, Object>();
    String instructions = SlimSerializer.serialize(toList(statements));
    writeString(instructions);
    int resultLength = getLengthToRead();
    String results = reader.read(resultLength);
    // resultList is a list: [tag, resultValue]
    List<Object> resultList = SlimDeserializer.deserialize(results);
    return resultToMap(resultList);
  }

  @Override
  public ExecutionLog getExecutionLog() {
    return new CommandRunnerExecutionLog(slimRunner);
  }

  private interface ToListExecutor extends InstructionExecutor {

  }

  private List<Object> toList(List<Instruction> instructions) {
    final List<Object> statementsAsList = new ArrayList<Object>(instructions.size());
    for (final Instruction instruction: instructions) {
      ToListExecutor executor = new ToListExecutor() {
        @Override
        public void addPath(String path) throws SlimException {
          statementsAsList.add(list(instruction.getId(), ImportInstruction.INSTRUCTION, path));
        }

        @Override
        public Object callAndAssign(String symbolName, String instanceName, String methodsName, Object... arguments) throws SlimException {
          List<Object> list = ListUtility.list((Object) instruction.getId(), CallAndAssignInstruction.INSTRUCTION, symbolName, instanceName, methodsName);
          addArguments(list, arguments);
          statementsAsList.add(list);
          return null;
        }

        @Override
        public Object call(String instanceName, String methodName, Object... arguments) throws SlimException {
          List<Object> list = ListUtility.list((Object) instruction.getId(), CallInstruction.INSTRUCTION, instanceName, methodName);
          addArguments(list, arguments);
          statementsAsList.add(list);
          return null;
        }

        @Override
        public void create(String instanceName, String className, Object... constructorArgs) throws SlimException {
          List<Object> list = ListUtility.list((Object) instruction.getId(), MakeInstruction.INSTRUCTION, instanceName, className);
          addArguments(list, constructorArgs);
          statementsAsList.add(list);
        }
      };

      instruction.execute(executor);
    }
    return statementsAsList;
  }

  private static void addArguments(List<Object> list, Object[] arguments) {
    for (Object arg: arguments) {
      list.add(arg);
    }
  }

  private int getLengthToRead() throws IOException  {
	String resultLength = reader.read(6);
    reader.read(1);
    int length = 0;
    try {
    	length = Integer.parseInt(resultLength);
    }
    catch (NumberFormatException e){
    	throw new IOException("Steam Read Failure. Can't read length of message from the server.  Possibly test aborted.  Last thing read: " + resultLength);
    }
	return length;
  }

  protected void writeString(String string) throws IOException {
    String packet = String.format("%06d:%s", string.getBytes("UTF-8").length, string);
    writer.write(packet);
    writer.flush();
  }

  @Override
  public void bye() throws IOException {
    writeString("bye");
    slimRunner.join();
    kill();
  }

  public static Map<String, Object> resultToMap(List<?> slimResults) {
    Map<String, Object> map = new HashMap<String, Object>();
    for (Object aResult : slimResults) {
      List<Object> resultList = ListUtility.uncheckedCast(Object.class, aResult);
      map.put((String) resultList.get(0), resultList.get(1));
    }
    return map;
  }
}
