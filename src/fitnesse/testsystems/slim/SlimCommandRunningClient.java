// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testsystems.slim;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import util.FileUtil;
import fitnesse.slim.SlimError;
import fitnesse.slim.SlimStreamReader;
import fitnesse.slim.SlimVersion;
import fitnesse.slim.instructions.Instruction;
import fitnesse.slim.protocol.SlimDeserializer;
import fitnesse.slim.protocol.SlimListBuilder;
import fitnesse.slim.protocol.SlimSerializer;
import fitnesse.socketservice.ClientSocketFactory;
import fitnesse.testsystems.CommandRunner;
import fitnesse.util.Clock;

public class SlimCommandRunningClient implements SlimClient {
  private static final Logger LOG = Logger.getLogger(SlimCommandRunningClient.class.getName());

  public static final int NO_SLIM_SERVER_CONNECTION_FLAG = -32000;
  public static double MINIMUM_REQUIRED_SLIM_VERSION = 0.3;

  protected final CommandRunner slimRunner;
  private final int connectionTimeout;
  private final double requiredSlimVersion;
  private final ClientSocketFactory clientSocketFactory;
  private Socket client;
  protected SlimStreamReader reader;
  protected OutputStream writer;
  private String slimServerVersionMessage;
  private double slimServerVersion;
  private String hostName;
  private int port;

  public SlimCommandRunningClient(CommandRunner slimRunner, String hostName, int port, int connectionTimeout, double requiredSlimVersion, ClientSocketFactory clientSocketFactory) {
    this.slimRunner = slimRunner;
    this.hostName = hostName;
    this.port = port;
    this.connectionTimeout = connectionTimeout;
    this.requiredSlimVersion = requiredSlimVersion;
    this.clientSocketFactory = clientSocketFactory;
  }

  @Override
  public void start() throws IOException, SlimVersionMismatch {
    try {
      slimRunner.asynchronousStart();
    } catch (Exception e) {
      final String slimErrorMessage = "Error SLiM server startup failed. "
          + slimRunner.getCommandErrorMessage();
      throw new SlimError(slimErrorMessage, e);

    }
    connect();
    checkForVersionMismatch();
  }

  private void checkForVersionMismatch() throws SlimVersionMismatch {
    double serverVersionNumber = getServerVersion();
    if (serverVersionNumber == NO_SLIM_SERVER_CONNECTION_FLAG) {
      throw new SlimVersionMismatch("Slim Protocol Version Error: Server did not respond with a valid version number.");
    } else if (serverVersionNumber < requiredSlimVersion) {
      throw new SlimVersionMismatch(String.format("Slim Protocol Version Error: Expected V%s but was V%s", requiredSlimVersion, serverVersionNumber));
    }
  }

  @Override
  public void kill() {
    if (slimRunner != null)
      slimRunner.kill();
    FileUtil.close(reader);
    FileUtil.close(writer);
    FileUtil.close(client);
  }

  @Override
  public void connect() throws IOException {
    final int sleepStep = 50; // milliseconds
    long timeOut = Clock.currentTimeInMillis() + connectionTimeout * 1000;
    LOG.finest("Trying to connect to host: " + hostName + " on port: " + port + " timeout setting: " + connectionTimeout);
    while (client == null) {
      if (slimRunner != null && slimRunner.isDead()) {
        final String slimErrorMessage = "Error SLiM server died before a connection could be established. " + slimRunner.getCommandErrorMessage();
      	throw new SlimError(slimErrorMessage);
      }
      try {
        client = clientSocketFactory.createSocket(hostName, port);
      } catch (IOException e) {
        if (Clock.currentTimeInMillis() > timeOut) {
          throw new SlimError("Error connecting to SLiM server on " + hostName + ":" + port, e);
        } else {
          try {
            Thread.sleep(sleepStep);
          } catch (InterruptedException e1) {
            Thread.currentThread().interrupt();
          }
        }
      }
    }
    LOG.fine("Connected to host: " + hostName + " on port: " + port + " timeout setting: " + connectionTimeout);

    reader = SlimStreamReader.getReader(client);
    writer = SlimStreamReader.getByteWriter(client);

    // Convert seconds to milliseconds
    int waittime = connectionTimeout * 1000;
    int oldTimeout = client.getSoTimeout();
    client.setSoTimeout(waittime);
    try {
      validateConnection();
    } finally {
      client.setSoTimeout(oldTimeout);
    }
  }

  protected void validateConnection() throws IOException {
    try{
    	slimServerVersionMessage = reader.readLine();
    }catch (SocketTimeoutException e){
    	throw new SlimError("Timeout while reading slim header from client. Check that you are connecting to the right port and that the slim client is running. You can increase the timeout limit by setting 'slim.timeout' in the fitnesse properties file.");
    }
    LOG.finest("Read Slim Header: >" + slimServerVersionMessage + "<");
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
    return slimServerVersionMessage != null && slimServerVersionMessage.startsWith(SlimVersion.SLIM_HEADER);
  }

  @Override
  public Map<String, Object> invokeAndGetResponse(List<Instruction> statements) throws SlimCommunicationException {
    if (statements.isEmpty())
      return Collections.emptyMap();
    String instructions = SlimSerializer.serialize(new SlimListBuilder(slimServerVersion).toList(statements));
    String results;
    try {
      SlimStreamReader.sendSlimMessage(writer, instructions);
      results = reader.getSlimMessage();
    } catch (IOException e) {
      throw new SlimCommunicationException("Could not send/receive data with SUT", e);
    }
    List<Object> resultList = SlimDeserializer.deserialize(results);
    return resultToMap(resultList);
  }

  @Override
  public void bye() throws IOException {
    SlimStreamReader.sendSlimMessage(writer, SlimVersion.BYEMESSAGE);
    slimRunner.join();
    kill();
  }

  public static Map<String, Object> resultToMap(List<?> slimResults) {
    Map<String, Object> map = new HashMap<>();
    for (Object aResult : slimResults) {
      @SuppressWarnings("unchecked")
      List<Object> resultList = (List<Object>) aResult;
      map.put((String) resultList.get(0), resultList.get(1));
    }
    return map;
  }
}
