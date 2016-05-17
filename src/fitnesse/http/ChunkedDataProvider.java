package fitnesse.http;

import java.io.IOException;

public interface ChunkedDataProvider {

  void startSending() throws IOException;

}
