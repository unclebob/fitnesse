package fitnesse.testutil;

import java.io.File;
import java.io.IOException;

public class CreateFileAndWaitFixture {
  public CreateFileAndWaitFixture(String name) throws IOException, InterruptedException {
    File file = new File(name);
    file.createNewFile();
    Thread.sleep(2000);
  }
}
