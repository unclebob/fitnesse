package fitnesse.slim;

import fitnesse.util.StreamReader;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;

public class SlimClient {
  private Socket client;
  private StreamReader reader;
  private BufferedWriter writer;
  private String slimServerVersion;
  private String hostName;
  private int port;

  protected void close() throws Exception {
    reader.close();
    writer.close();
    client.close();
  }

  public SlimClient(String hostName, int port) {
    this.port = port;
    this.hostName = hostName;
  }

  protected void connect() throws Exception {
    client = new Socket(hostName, port);
    reader = new StreamReader(client.getInputStream());
    writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
    slimServerVersion = reader.readLine();
  }

  private String getVersion() {
    return slimServerVersion;
  }

  protected boolean isConnected() {
    return slimServerVersion.startsWith("Slim -- V");
  }

  protected List<Object> invokeAndGetResponse(List<Object> statements) throws Exception {
    String instructions = ListSerializer.serialize(statements);
    writer.write(String.format("%06d:", instructions.length()));
    writer.write(instructions);
    writer.flush();
    String resultLength = reader.read(6);
    String colon = reader.read(1);
    String results = reader.read(Integer.parseInt(resultLength));
    List<Object> resultList = ListDeserializer.deserialize(results);
    return resultList;
  }
}
