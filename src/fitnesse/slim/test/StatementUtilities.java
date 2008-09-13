package fitnesse.slim.test;

import java.util.Arrays;
import java.util.List;

public class StatementUtilities {
  public static List<String> statement(String... strings) {
    return Arrays.asList(strings);
  }

  public static List<Object> list(Object... objects) {
    return Arrays.asList(objects);
  }
}
