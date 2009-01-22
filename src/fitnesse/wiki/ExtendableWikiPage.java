// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.util.HashMap;
import java.util.Map;

public abstract class ExtendableWikiPage extends BaseWikiPage {
  private static final long serialVersionUID = 1L;

  private Map<String, Extension> extensions = new HashMap<String, Extension>();

  public ExtendableWikiPage(String name, WikiPage parent) {
    super(name, parent);
  }

  protected void addExtention(Extension extension) {
    extensions.put(extension.getName(), extension);
  }

  public boolean hasExtension(String extensionName) {
    return extensions.containsKey(extensionName);
  }

  public Extension getExtension(String extensionName) {
    return extensions.get(extensionName);
  }
}
