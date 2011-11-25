// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext;

import fitnesse.wikitext.widgets.AliasLinkWidget;
import fitnesse.wikitext.widgets.WikiWordWidget;

public interface WidgetVisitor {
  public void visit(WikiWidget widget);

  public void visit(WikiWordWidget widget);

  public void visit(AliasLinkWidget widget);
}
