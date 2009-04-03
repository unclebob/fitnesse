// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.ListUtility;
import util.StreamReader;

public class SlimClient {
  private Socket client;
  private StreamReader reader;
  private BufferedWriter writer;
  private String slimServerVersion;
  private String hostName;
  private int port;

  public void close() throws Exception {
    reader.close();
    writer.close();
    client.close();
  }

  public SlimClient(String hostName, int port) {
    this.port = port;
    this.hostName = hostName;
  }

  public void connect() throws Exception {
    for (int tries = 0; tryConnect() == false; tries++) {
      if (tries > 100)
        throw new SlimError("Could not start Slim.");
      Thread.sleep(50);
    }
    reader = new StreamReader(client.getInputStream());
    writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream(), "UTF-8"));
    slimServerVersion = reader.readLine();
  }

  private boolean tryConnect() {
    try {
      client = new Socket(hostName, port);
      return true;
    } catch (IOException e) {
      return false;
    }
  }

  public String getVersion() {
    return slimServerVersion;
  }

  public boolean isConnected() {
    return slimServerVersion.startsWith("Slim -- V");
  }

  public Map<String, Object> invokeAndGetResponse(List<Object> statements) throws Exception {
    String instructions = ListSerializer.serialize(statements);
    writeString(instructions);
    String resultLength = reader.read(6);
    reader.read(1);
    String results = null;
    results = reader.read(Integer.parseInt(resultLength));
    List<Object> resultList = ListDeserializer.deserialize(results);
    return resultToMap(resultList);
  }

  private void writeString(String string) throws IOException {
    String packet = String.format("%06d:%s", string.getBytes("UTF-8").length, string);
    writer.write(packet);
    writer.flush();
  }

  public void sendBye() throws IOException {
    writeString("bye");
  }

  public static Map<String, Object> resultToMap(List<Object> slimResults) {
    Map<String, Object> map = new HashMap<String, Object>();
    for (Object aResult : slimResults) {
      List<Object> resultList = ListUtility.uncheckedCast(Object.class, aResult);
      map.put((String) resultList.get(0), resultList.get(1));
    }
    return map;
  }
}
