package fitnesse.slim;

import java.io.OutputStream;

public interface SlimSocket {

  int getLocalPort();

  void close();

  SlimSocket accept();

  SlimStreamReader getReader();

  OutputStream getByteWriter();

}
