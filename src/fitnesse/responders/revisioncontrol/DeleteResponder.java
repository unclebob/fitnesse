// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.revisioncontrol;

import static fitnesse.revisioncontrol.RevisionControlOperation.DELETE;

import java.util.List;

import fitnesse.html.HtmlUtil;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.WikiPage;

public class DeleteResponder extends RevisionControlResponder {
  public DeleteResponder() {
    super(DELETE);
  }

  @Override
  protected String responseMessage(String resource) throws Exception {
    String parentResource = "";
    int lastIndexOfDot = resource.lastIndexOf('.');
    if (lastIndexOfDot != -1)
      parentResource = resource.substring(0, lastIndexOfDot);
    return "Click " + HtmlUtil.makeLink(parentResource, "here").html() + " to view the parent page.";
  }

  @Override
  protected void beforeOperation(FileSystemPage page) throws Exception {
    List<WikiPage> children = page.getChildren();
    for (WikiPage child : children)
      if (child instanceof FileSystemPage)
        executeRevisionControlOperation((FileSystemPage) child);
  }

  @Override
  protected void performOperation(FileSystemPage page) throws Exception {
    page.execute(DELETE);
    WikiPage parent = page.getParent();
    if (parent instanceof FileSystemPage)
      ((FileSystemPage) parent).removeChildPage(page.getName());
  }
}
