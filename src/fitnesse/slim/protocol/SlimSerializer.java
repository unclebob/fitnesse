// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.slim.protocol;

import fitnesse.slim.SlimVersion;

import java.util.List;

/**
 * Packs up a list into a serialized string using a special format.  The list items must be strings, or lists.
 * They will be recursively serialized.
 * <p>
 * Format:  [iiiiii:llllll:item...]
 * All lists (including lists within lists) begin with [ and end with ].  After the [ is the 6 digit number of items
 * in the list followed by a :.  Then comes each item which is composed of a 6 digit length a : and then the value
 * of the item followed by a :.
 */
public class SlimSerializer {
  private StringBuilder result;
  private List<Object> list;

  public SlimSerializer(List<Object> list) {
    this.list = list;
    result = new StringBuilder();
  }

  public static String serialize(List<Object> list) {
    return new SlimSerializer(list).serialize();
  }

  public String serialize() {
    result.append('[');
    appendLength(list.size());

    for (Object o : list) {
      String s = marshalObjectToString(o);
      appendLength(s.length());
      appendString(s);
    }
    result.append(']');
    return result.toString();
  }

  @SuppressWarnings("unchecked")
private String marshalObjectToString(Object o) {
    String s;
    if (o == null)
      s = "null";
    else if (o instanceof String)
      s = (String) o;
    else if (o instanceof List)
      s = SlimSerializer.serialize((List<Object>) o);
    else
      s = o.toString();
    return s;
  }

  private void appendString(String s) {
    result.append(s).append(':');
  }

  private void appendLength(int size) {
    result.append(String.format(SlimVersion.LENGTH_FORMAT, size));
  }

}
