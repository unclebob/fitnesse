package fitnesse.testsystems.slim;

import fitnesse.slim.instructions.Instruction;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SlimClient {
  void start() throws IOException, SlimVersionMismatch;

  Map<String, Object> invokeAndGetResponse(List<Instruction> statements) throws SlimCommunicationException;

  void connect() throws IOException, SlimVersionMismatch;

  void bye() throws IOException;

  void kill();
}
