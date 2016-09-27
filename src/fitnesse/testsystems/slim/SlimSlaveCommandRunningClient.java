package fitnesse.testsystems.slim;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;

import fitnesse.slim.SlimStreamReader;
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
    reader = new SlimStreamReader(new BufferedInputStream(
        slimRunner.getReader()));
    writer = new BufferedOutputStream(slimRunner.getWriter());
    validateConnection();
  }
}
