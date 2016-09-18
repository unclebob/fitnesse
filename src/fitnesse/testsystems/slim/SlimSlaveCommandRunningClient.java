package fitnesse.testsystems.slim;

import java.io.IOException;

import fitnesse.socketservice.ClientSocketFactory;
import fitnesse.testsystems.CommandRunner;

public class SlimSlaveCommandRunningClient extends SlimCommandRunningClient {

  public SlimSlaveCommandRunningClient(CommandRunner slimRunner,
      String hostName, int port, int connectionTimeout,
      double requiredSlimVersion, ClientSocketFactory clientSocketFactory) {
    super(slimRunner, hostName, port, connectionTimeout, requiredSlimVersion,
        clientSocketFactory);
  }

  @Override
  public void connect() throws IOException {
    reader = slimRunner.getReader();
    writer = slimRunner.getByteWriter();
    validateConnection();
  }
}
