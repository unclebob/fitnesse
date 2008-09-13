package fitnesse.slim;

import fitnesse.socketservice.SocketService;

public class SlimService extends SocketService {
  public SlimService(int port) throws Exception {
    super(port, new SlimServer());
  }
}
