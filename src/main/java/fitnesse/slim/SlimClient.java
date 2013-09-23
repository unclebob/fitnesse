package fitnesse.slim;

import fitnesse.slim.instructions.Instruction;
import fitnesse.testsystems.ExecutionLog;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface SlimClient {
  void start() throws IOException;

  Map<String, Object> invokeAndGetResponse(List<Instruction> statements) throws IOException;

  void bye() throws IOException;

  void kill() throws IOException;

  String getTestRunner();

  ExecutionLog getExecutionLog();
}
