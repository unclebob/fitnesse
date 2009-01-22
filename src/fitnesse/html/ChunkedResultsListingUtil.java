// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

public class ChunkedResultsListingUtil {
  public static String getTableOpenHtml(String id) {
    return "<table id=\"" + id + "\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" class=\"dirListing\">";
  }

  public static String getTableCloseHtml() {
    return "</table>";
  }
}
