package fitnesse;

import java.io.IOException;

public interface Updater {
  boolean update() throws IOException;
}
