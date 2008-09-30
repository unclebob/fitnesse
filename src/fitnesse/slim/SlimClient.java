package fitnesse.slim;

import fitnesse.util.StreamReader;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

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
    client = new Socket(hostName, port);
    reader = new StreamReader(client.getInputStream());
    writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
    slimServerVersion = reader.readLine();
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
    String colon = reader.read(1);
    String results = reader.read(Integer.parseInt(resultLength));
    List<Object> resultList = ListDeserializer.deserialize(results);
    return resultToMap(resultList);
  }

  private void writeString(String string) throws IOException {
    writer.write(String.format("%06d:", string.length()));
    writer.write(string);
    writer.flush();
  }

  public void sendBye() throws IOException {
    writeString("bye");
  }

  public static Map<String, Object> resultToMap(List<Object> slimResults) {
    Map<String, Object> map = new HashMap<String, Object>();
    for (Object aResult : slimResults) {
      List<Object> resultList = (List<Object>) aResult;
      map.put((String)resultList.get(0), resultList.get(1));
    }
    return map;
  }
}
