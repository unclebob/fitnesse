// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import fitnesse.slim.instructions.*;
import fitnesse.slim.protocol.SlimDeserializer;
import fitnesse.slim.protocol.SlimSerializer;
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

import static util.ListUtility.list;

public class SlimClient {
  public static double MINIMUM_REQUIRED_SLIM_VERSION = 0.3;
  public static int NO_SLIM_SERVER_CONNECTION_FLAG = -32000;
  private Socket client;
  private StreamReader reader;
  private BufferedWriter writer;
  private String slimServerVersionMessage;
  private double slimServerVersion;
  private String hostName;
  private int port;

  public void close() throws IOException {
    reader.close();
    writer.close();
    client.close();
  }

  public SlimClient(String hostName, int port) {
    this.port = port;
    this.hostName = hostName;
  }

  public void connect() throws IOException {
    for (int tries = 0; tryConnect() == false; tries++) {
      if (tries > 100)
        throw new SlimError("Could not start Slim.");
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        throw new SlimError("Wait for connection interrupted.");
      }
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
		System.out.println("Error reading Slim Version. Read the following: " + slimServerVersionMessage);
	}
}

  private boolean tryConnect() {
    try {
      client = new Socket(hostName, port);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public double getServerVersion() {
    return slimServerVersion;
  }

  public boolean isConnected() {
    return slimServerVersionMessage.startsWith("Slim -- V");
  }

  public Map<String, Object> invokeAndGetResponse(List<Instruction> statements) throws IOException {
    if (statements.size() == 0)
      return new HashMap<String, Object>();
    String instructions = SlimSerializer.serialize(toList(statements));
    writeString(instructions);
    int resultLength = getLengthToRead();
    String results = null;
    results = reader.read(resultLength);
    // resultList is a list: [tag, resultValue]
    List<Object> resultList = SlimDeserializer.deserialize(results);
    return resultToMap(resultList);
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
    	throw new RuntimeException("Steam Read Failure. Can't read length of message from the server.  Possibly test aborted.  Last thing read: " + resultLength);
    }
	return length;
  }

  private void writeString(String string) throws IOException {
    String packet = String.format("%06d:%s", string.getBytes("UTF-8").length, string);
    writer.write(packet);
    writer.flush();
  }

  public void sendBye() throws IOException {
    writeString("bye");
  }

  public static Map<String, Object> resultToMap(List<? extends Object> slimResults) {
    Map<String, Object> map = new HashMap<String, Object>();
    for (Object aResult : slimResults) {
      List<Object> resultList = ListUtility.uncheckedCast(Object.class, aResult);
      map.put((String) resultList.get(0), resultList.get(1));
    }
    return map;
  }
}
