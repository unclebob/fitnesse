// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wikitext.WidgetBuilder;
import fitnesse.wikitext.WikiWidget;
import fitnesse.wikitext.widgets.ParentWidget;
import fitnesse.wikitext.widgets.VirtualWikiWidget;
import fitnesse.wikitext.widgets.WidgetRoot;

public class VirtualWikiDeprecationUpdate extends PageTraversingUpdate {
  @SuppressWarnings("unchecked")
  public static WidgetBuilder virtualWidgetBuilder = new WidgetBuilder(new Class[]{VirtualWikiWidget.class});

  public VirtualWikiDeprecationUpdate(UpdaterImplementation updater) {
    super(updater);
  }

  public String getMessage() {
    return "Updating pages with !virtualwiki widgets";
  }

  public String getName() {
    return "VirtualWikiDeprecationUpdate";
  }

  public void processPage(WikiPage page) throws Exception {
    PageData data = page.getData();
    ParentWidget widgetRoot = new WidgetRoot(data.getContent(), page, virtualWidgetBuilder);
    while (widgetRoot.hasNextChild()) {
      WikiWidget widget = widgetRoot.nextChild();
      if (widget instanceof VirtualWikiWidget) {
        VirtualWikiWidget vWidget = (VirtualWikiWidget) widget;
        String url = vWidget.getRemoteUrl();
        data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, url);
        page.commit(data);
        break;
      }
    }
  }
}
