package fitnesse.testsystems.slim;

import fitnesse.slim.instructions.Instruction;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SlimClient {
  void start() throws IOException;

  Map<String, Object> invokeAndGetResponse(List<Instruction> statements) throws IOException;

  void connect() throws IOException;

  void bye() throws IOException;

  void kill() throws IOException;
}
