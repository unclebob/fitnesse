// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext;

public class EchoWidgetInterceptor implements WidgetInterceptor {
  public void intercept(WikiWidget widget) throws Exception {
    System.out.println(widget.getClass() + ": " + widget.asWikiText() + " -> " + widget.render());
  }
}
