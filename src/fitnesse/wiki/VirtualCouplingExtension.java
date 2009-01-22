// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.responders.ErrorResponder;

public class VirtualCouplingExtension implements Extension {
  private static final long serialVersionUID = 1L;

  public static final String NAME = "VirtualCoupling";

  private WikiPage hostPage;
  protected VirtualCouplingPage virtualCoupling;

  public String getName() {
    return NAME;
  }

  public VirtualCouplingExtension(WikiPage page) throws Exception {
    hostPage = page;
    resetVirtualCoupling();
  }

  public void setVirtualCoupling(VirtualCouplingPage coupling) {
    virtualCoupling = coupling;
  }

  public void resetVirtualCoupling() throws Exception {
    virtualCoupling = new NullVirtualCouplingPage(hostPage);
  }

  public WikiPage getVirtualCoupling() throws Exception {
    detectAndLoadVirtualChildren();
    return virtualCoupling;
  }

  protected void detectAndLoadVirtualChildren() throws Exception {
    PageData data = hostPage.getData();
    if (data.hasAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE))
      loadVirtualChildren(data.getAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE));
  }

  public void loadVirtualChildren(String url) throws Exception {
    try {
      ProxyPage proxy = ProxyPage.retrievePage(url);
      virtualCoupling = new VirtualCouplingPage(hostPage, proxy);
    }
    catch (Exception e) {
      WikiPage page = hostPage.getChildPage("VirtualWikiNetworkError");
      if (page == null)
        page = hostPage.addChildPage("VirtualWikiNetworkError");
      PageData data = page.getData();
      data.setContent("{{{" + ErrorResponder.makeExceptionString(e) + "}}}");
      page.commit(data);
    }
  }
}
