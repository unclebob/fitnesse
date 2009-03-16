// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class StringUtil {
  public static String join(List<?> strings, String delimiter) {
    if (strings.isEmpty())
      return "";

    Iterator<?> i = strings.iterator();
    StringBuffer joined = new StringBuffer((String) i.next());

    while (i.hasNext()) {
      String eachLine = (String) i.next();
      joined.append(delimiter);
      joined.append(eachLine);
    }

    return joined.toString();
  }

  public static String[] combineArrays(String[]... arrays) {
    List<String> combinedList = new LinkedList<String>();
    for (String[] array : arrays)
      combinedList.addAll(Arrays.asList(array));
    return combinedList.toArray(new String[combinedList.size()]);
  }

  public static boolean isBlank(String resource) {
    return "".equals(resource);
  }

  public static String trimNonNullString(String original) {
    return original != null ? original.trim() : original;
  }

  public static String replaceAll(String original, String target, String replacement) {
    StringBuffer result = new StringBuffer();
    int fromIndex = 0;
    while (true) {
      int foundIndex = original.indexOf(target, fromIndex);
      if (foundIndex == -1) {
        result.append(original.substring(fromIndex));
        break;
      }
      result.append(original.substring(fromIndex, foundIndex));
      result.append(replacement);
      fromIndex = foundIndex + target.length();
    }
    return result.toString();
  }
}
