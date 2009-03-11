// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import util.RegexTestCase;

public class ChunkedResultsListingUtilTest extends RegexTestCase {
  public void testOpeningTag() {
    assertEquals("<table id=\"myTable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" class=\"dirListing\">", ChunkedResultsListingUtil.getTableOpenHtml("myTable"));
  }

  public void testClosingTag() {
    assertEquals("</table>", ChunkedResultsListingUtil.getTableCloseHtml());
  }

}
