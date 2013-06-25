package fitnesse.slim;

import fitnesse.slim.instructions.Instruction;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SlimClient {
  void start() throws IOException;

  void close() throws IOException;

  Map<String, Object> invokeAndGetResponse(List<Instruction> statements) throws IOException;

  void sendBye() throws IOException;

  String getTestRunner();
}
