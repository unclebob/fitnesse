package fitnesse.slim;

import fitnesse.socketservice.SocketService;
import fitnesse.components.CommandLine;

public class SlimService extends SocketService {
  public static SlimService instance = null;
  public static void main(String[] args) throws Exception {
    CommandLine cl = new CommandLine("port");
    if (cl.parse(args)) {
      String portString = cl.getArgument("port");
      int port = Integer.parseInt(portString);
      new SlimService(port);
    }
  }
  public SlimService(int port) throws Exception {
    super(port, new SlimServer());
    instance = this;
  }
}
