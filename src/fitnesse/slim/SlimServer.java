package fitnesse.slim;

import fitnesse.socketservice.SocketServer;

import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.Socket;
import java.util.List;

public class SlimServer implements SocketServer {
  public void serve(Socket s) {
    try {
      BufferedReader reader = StreamUtility.GetBufferedReader(s);
      PrintStream writer = StreamUtility.GetPrintStream(s);

      writer.println("Slim -- " + SlimVersion.VERSION);
      String instructions = reader.readLine();
      if (instructions != null) {
        List<Object> statements = ListDeserializer.deserialize(instructions);
        List<Object> results = ListExecutor.execute(statements);
        writer.println(ListSerializer.serialize(results));
        s.close();
        reader.close();
        writer.close();
      }
    } catch (Exception e) {

    }
  }
}
