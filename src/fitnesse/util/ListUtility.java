package fitnesse.util;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ListUtility {
  public static List<Object> list() {
    return new LinkedList<Object>();
  }

  public static List<Object> list(Object... objects) {
    List<Object> list = new ArrayList<Object>();
    for (Object object : objects)
      list.add(object);
    return list;
  }

  public static List<String> list(String... strings) {
    List<String> list = new ArrayList<String>();
    for (String string : strings)
      list.add(string);
    return list;
  }

  @SuppressWarnings("unchecked")
  public static <T> List<T> uncheckedCast(Class<?> destType, Object sourceList) {
    return (List<T>) sourceList;
  }
}
