package fitnesse.slim;

import fitnesse.socketservice.SocketServer;
import fitnesse.util.StreamReader;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

public class SlimServer implements SocketServer {
  private StreamReader reader;
  private BufferedWriter writer;
  private ListExecutor executor;

  public void serve(Socket s) {
    try {
      tryProcessInstructions(s);
    } catch (Exception e) {
      close();
    }
  }

  private void tryProcessInstructions(Socket s) throws Exception {
    initialize(s);
    while (true)
      processOneSetOfInstructions();
  }

  private void initialize(Socket s) throws IOException {
    executor = new ListExecutor();
    reader = new StreamReader(s.getInputStream());
    writer = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
    writer.write(String.format("Slim -- %s\n", SlimVersion.VERSION));
    writer.flush();
  }

  private void processOneSetOfInstructions() throws Exception {
    String instructions = getInstructionsFromClient();
    if (instructions != null) {
      List<Object> results = executeInstructions(instructions);
      sendResultsToClient(results);
    }
  }

  private String getInstructionsFromClient() throws Exception {
    int instructionLength = Integer.parseInt(reader.read(6));
    String colon = reader.read(1);
    String instructions = reader.read(instructionLength);
    return instructions;
  }

  private List<Object> executeInstructions(String instructions) {
    List<Object> statements = ListDeserializer.deserialize(instructions);
    List<Object> results = executor.execute(statements);
    return results;
  }

  private void sendResultsToClient(List<Object> results) throws IOException {
    String resultString = ListSerializer.serialize(results);
    writer.write(String.format("%06d:%s", resultString.length(), resultString));
    writer.flush();
  }

  private void close(){
    try {
      reader.close();
      writer.close();
    } catch (Exception e) {

    }
  }
}
