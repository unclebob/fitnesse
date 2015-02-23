// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import fitnesse.slim.SlimError;
import fitnesse.slim.SlimException;
import fitnesse.slim.SlimStreamReader;
import fitnesse.slim.SlimVersion;
import fitnesse.slim.instructions.*;
import fitnesse.slim.protocol.SlimDeserializer;
import fitnesse.slim.protocol.SlimSerializer;
import fitnesse.socketservice.SocketFactory;
import fitnesse.testsystems.CommandRunner;
import org.apache.commons.lang.ArrayUtils;
import util.StreamReader;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import static java.util.Arrays.asList;

public class SlimCommandRunningClient implements SlimClient {
  private static final Logger LOG = Logger.getLogger(SlimCommandRunningClient.class.getName());

  public static final int NO_SLIM_SERVER_CONNECTION_FLAG = -32000;
  public static double MINIMUM_REQUIRED_SLIM_VERSION = 0.3;

  private final CommandRunner slimRunner;
  private final int connectionTimeout;
  private final double requiredSlimVersion;
  private Socket client;
  private SlimStreamReader reader;
  private OutputStream writer;
  private String slimServerVersionMessage;
  private double slimServerVersion;
  private String hostName;
  private int port;
  private boolean useSSL;
  private String sslParameterClassName;

  public SlimCommandRunningClient(CommandRunner slimRunner, String hostName, int port, int connectionTimeout, double requiredSlimVersion, boolean useSSL, String sslParameterClassName) {
    this.slimRunner = slimRunner;
    this.hostName = hostName;
    this.port = port;
    this.connectionTimeout = connectionTimeout;
    this.requiredSlimVersion = requiredSlimVersion;
    this.useSSL = useSSL;
    this.sslParameterClassName = sslParameterClassName;
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
    } else if (serverVersionNumber < requiredSlimVersion) {
      throw new SlimError(String.format("Slim Protocol Version Error: Expected V%s but was V%s", requiredSlimVersion, serverVersionNumber));
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
    final int sleepStep = 50; // milliseconds
    int maxTries = connectionTimeout * 1000 / sleepStep;
    while (client == null) {
      if (slimRunner != null && slimRunner.isDead()) {
        throw new SlimError("Error SLiM server died before a conection could be established.");
      }
      LOG.finest("Trying to connect to host: " + hostName + " on port: " + port + " SSL=" + useSSL + " timeout setting: " + connectionTimeout + " remaining retries: " + maxTries);
      try {
        maxTries--;
        client = SocketFactory.tryCreateClientSocket(hostName, port, useSSL, sslParameterClassName);
      } catch (IOException e) {
        if (maxTries <= 1) {
          throw new SlimError("Error connecting to SLiM server on " + hostName + ":" + port, e);
        } else {
          try {
            Thread.sleep(sleepStep);
          } catch (InterruptedException i) {
            throw new SlimError("Wait for connection interrupted.");
          }
        }
      }
    }
    LOG.fine("Connected to host: " + hostName + " on port: " + port + " SSL=" + useSSL + " timeout setting: " + connectionTimeout + " remaining retries: " + maxTries);

    reader = SlimStreamReader.getReader(client);
    writer = SlimStreamReader.getByteWriter(client);
    client.setSoTimeout(connectionTimeout * 1000);
    validateConnection(useSSL);
    client.setSoTimeout(connectionTimeout * 1000);
  }

  private void validateConnection(boolean isSslConnection) throws IOException {
    // Convert seconds to milliseconds
    int waittime = connectionTimeout * 1000;
    //SSL sockets don't support timeout :(
    if (isSslConnection) waittime = 0;
    reader.setTimeoutLimit(waittime);
    slimServerVersionMessage = reader.readLine();
    reader.setTimeoutLimit(0);

    LOG.finest("Read Slim Header: >" + slimServerVersionMessage + "<");
    if (reader.isTimeout()) {
      throw new SlimError("Timeout while reading slim header from client. Read the following: " + slimServerVersionMessage);
    }
    if (!isConnected()) {
      throw new SlimError("Got invalid slim header from client. Read the following: " + slimServerVersionMessage);
    }
    try {
      slimServerVersion = Double.parseDouble(slimServerVersionMessage.replace(SlimVersion.SLIM_HEADER, ""));
    } catch (Exception e) {
      slimServerVersion = NO_SLIM_SERVER_CONNECTION_FLAG;
      throw new SlimError("Got invalid slim version from Client. Read the following: " + slimServerVersionMessage);
    }
    LOG.fine("Got Slim Header: " + slimServerVersionMessage + ", and Version " + slimServerVersion);
  }


  public double getServerVersion() {
    return slimServerVersion;
  }

  public boolean isConnected() {
    return slimServerVersionMessage.startsWith(SlimVersion.SLIM_HEADER);
  }

  public String getPeerName() {
    return SocketFactory.peerName(client);
  }

  public String getMyName() {
    return SocketFactory.myName(client);
  }


  @Override
  public Map<String, Object> invokeAndGetResponse(List<Instruction> statements) throws IOException {
    if (statements.isEmpty())
      return new HashMap<String, Object>();
    String instructions = SlimSerializer.serialize(toList(statements));
    SlimStreamReader.sendSlimMessage(writer, instructions);
    String results = reader.getSlimMessage();
    List<Object> resultList = SlimDeserializer.deserialize(results);
    return resultToMap(resultList);
  }

  private interface ToListExecutor extends InstructionExecutor {

  }

  private List<Object> toList(List<Instruction> instructions) {
    final List<Object> statementsAsList = new ArrayList<Object>(instructions.size());
    for (final Instruction instruction : instructions) {
      ToListExecutor executor = new ToListExecutor() {
        @Override
        public void addPath(String path) throws SlimException {
          statementsAsList.add(asList(instruction.getId(), ImportInstruction.INSTRUCTION, path));
        }

        @Override
        public Object callAndAssign(String symbolName, String instanceName, String methodsName, Object... arguments) throws SlimException {
          Object[] list = new Object[]{instruction.getId(), CallAndAssignInstruction.INSTRUCTION, symbolName, instanceName, methodsName};
          statementsAsList.add(asList(ArrayUtils.addAll(list, arguments)));
          return null;
        }

        @Override
        public Object call(String instanceName, String methodName, Object... arguments) throws SlimException {
          Object[] list = new Object[]{instruction.getId(), CallInstruction.INSTRUCTION, instanceName, methodName};
          statementsAsList.add(asList(ArrayUtils.addAll(list, arguments)));
          return null;
        }

        @Override
        public void create(String instanceName, String className, Object... constructorArgs) throws SlimException {
          Object[] list = new Object[]{instruction.getId(), MakeInstruction.INSTRUCTION, instanceName, className};
          statementsAsList.add(asList(ArrayUtils.addAll(list, constructorArgs)));
        }

        @Override
        public void assign(String symbolName, Object value) {
          if (slimServerVersion < 0.4) {
            throw new SlimError("The assign instruction is available as of SLIM protocol version 0.4");
          }
          Object[] list = new Object[]{instruction.getId(), AssignInstruction.INSTRUCTION, symbolName, value};
          statementsAsList.add(asList(list));
        }
      };

      instruction.execute(executor);
    }
    return statementsAsList;
  }

  @Override
  public void bye() throws IOException {
    SlimStreamReader.sendSlimMessage(writer, SlimVersion.BYEMESSAGE);
    writer.close();
    reader.close();
    client.close();
    slimRunner.join();
    kill();
  }

  public static Map<String, Object> resultToMap(List<?> slimResults) {
    Map<String, Object> map = new HashMap<String, Object>();
    for (Object aResult : slimResults) {
      @SuppressWarnings("unchecked")
      List<Object> resultList = (List<Object>) aResult;
      map.put((String) resultList.get(0), resultList.get(1));
    }
    return map;
  }
}
