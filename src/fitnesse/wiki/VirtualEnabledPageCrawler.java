// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

//TODO rename me
public class VirtualEnabledPageCrawler implements PageCrawlerDeadEndStrategy {
  public WikiPage getPageAfterDeadEnd(WikiPage context, WikiPagePath restOfPath, PageCrawler crawler) throws Exception {
    String name = restOfPath.getFirst();
    restOfPath = restOfPath.getRest();
    if (context.hasExtension(VirtualCouplingExtension.NAME)) {
      VirtualCouplingExtension extension = (VirtualCouplingExtension) context.getExtension(VirtualCouplingExtension.NAME);
      WikiPage coupling = extension.getVirtualCoupling();
      WikiPage child = coupling.getChildPage(name);
      if (child != null)
        return crawler.getPage(child, restOfPath);
      else
        return null;
    } else
      return null;

  }
}
