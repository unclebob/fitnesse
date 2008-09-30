package fitnesse.util;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

public class ListUtility {
  public static List<Object> list(Object... objects) {
    List<Object> list = new ArrayList<Object>();
    for (Object object : objects)
      list.add(object);
    return list;
  }
}
