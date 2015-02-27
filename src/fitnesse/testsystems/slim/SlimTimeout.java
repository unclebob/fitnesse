package fitnesse.testsystems.slim;

import java.io.IOException;

public class SlimTimeout extends IOException {

  public SlimTimeout(String s, Throwable throwable) {
    super(s, throwable);
  }
}
