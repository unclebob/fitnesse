package fitnesse.util;

import java.util.*;

public class StringUtil
{
  public static String join(List strings, String delimiter)
  {
    if (strings.isEmpty())
      return "";

    Iterator i = strings.iterator();
    StringBuffer joined = new StringBuffer((String) i.next());

    for (/* declared above */; i.hasNext();)
    {
      String eachLine = (String) i.next();
      joined.append(delimiter);
      joined.append(eachLine);
    }

    return joined.toString();
  }
}
