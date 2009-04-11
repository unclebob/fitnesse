package fitnesse.testutil;

import java.io.File;

public class CreateFileAndWaitFixture {
  public CreateFileAndWaitFixture(String name) {
    File file = new File(name);
    try {
      file.createNewFile();
      Thread.sleep(2000);
    } catch (Exception e) {
      e.printStackTrace();
    }

  }
}
