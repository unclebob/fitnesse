// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.templateUtilities;

public class HtmlPageFactory {
  public HtmlPage newPage() {
    return new HtmlPage("skeleton.vm");
  }

  public String toString() {
    return getClass().getName();
  }

}
