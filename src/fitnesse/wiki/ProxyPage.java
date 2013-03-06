// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import util.Clock;

import fitnesse.http.ResponseParser;

public class ProxyPage extends CachingPage implements Serializable {
  private static final long serialVersionUID = 1L;

  public static int retrievalCount = 0;

  private String host;
  private int hostPort;
  private WikiPagePath realPath;
  public ResponseParser parser;
  private long lastLoadChildrenTime = 0;

  public ProxyPage(WikiPage original) {
    super(original.getName(), null);
    realPath = original.getPageCrawler().getFullPath(original);

    List<?> children = original.getChildren();
    for (Iterator<?> iterator = children.iterator(); iterator.hasNext();) {
      ProxyPage child = new ProxyPage((WikiPage) iterator.next());
      child.parent = this;
      this.children.put(child.getName(), child);
    }
  }

  protected ProxyPage(String name, WikiPage parent) {
    super(name, parent);
  }

  public ProxyPage(String name, WikiPage parent, String host, int port, WikiPagePath path) {
    super(name, parent);
    this.host = host;
    hostPort = port;
    realPath = path;
  }

  public static ProxyPage retrievePage(String urlString) {
    retrievalCount++;
    URL url;
    try {
      url = new URL(urlString + "?responder=proxy&type=bones");
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    ProxyPage page = (ProxyPage) getObjectFromUrl(url);
    page.setTransientValues(url.getHost(), Clock.currentTimeInMillis());
    int port = url.getPort();
    page.setHostPort((port == -1) ? 80 : port);
    page.lastLoadChildrenTime = Clock.currentTimeInMillis();
    return page;
  }

  protected WikiPage createChildPage(String name) {
    WikiPagePath childPath = realPath.copy().addNameToEnd(name);
    return new ProxyPage(name, this, host, getHostPort(), childPath);
  }

  protected void loadChildren() {
    if (cacheTime <= (Clock.currentTimeInMillis() - lastLoadChildrenTime)) {
      ProxyPage page = retrievePage(getThisPageUrl());
      children.clear();
      for (Iterator<?> iterator = page.children.values().iterator(); iterator.hasNext();) {
        ProxyPage child = (ProxyPage) iterator.next();
        child.parent = this;
        children.put(child.getName(), child);
      }
      lastLoadChildrenTime = Clock.currentTimeInMillis();
    }
  }

  public String getThisPageUrl() {
    StringBuffer url = new StringBuffer("http://");
    url.append(host);
    url.append(":").append(getHostPort());
    url.append("/").append(PathParser.render(realPath));
    return url.toString();
  }

  public boolean hasChildPage(String pageName) {
    if (children.containsKey(pageName))
      return true;
    else {
      loadChildren();
      return children.containsKey(pageName);
    }
  }

  public void setTransientValues(String host, long lastLoadTime) {
    this.host = host;
    lastLoadChildrenTime = lastLoadTime;
    for (Iterator<WikiPage> i = children.values().iterator(); i.hasNext();) {
      ProxyPage page = (ProxyPage) i.next();
      page.setTransientValues(host, lastLoadTime);
    }
  }

  public String getHost() {
    return host;
  }

  public void setHostPort(int port) {
    hostPort = port;
    for (Iterator<WikiPage> i = children.values().iterator(); i.hasNext();) {
      ProxyPage page = (ProxyPage) i.next();
      page.setHostPort(port);
    }
  }

  public int getHostPort() {
    return hostPort;
  }

  public PageData getMeat() {
    return getMeat(null);
  }

  public PageData getMeat(String versionName) {
    StringBuffer urlString = new StringBuffer(getThisPageUrl());
    urlString.append("?responder=proxy&type=meat");
    if (versionName != null)
      urlString.append("&version=").append(versionName);
    URL url;
    try {
      url = new URL(urlString.toString());
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    PageData data = (PageData) getObjectFromUrl(url);
    if (data != null)
      data.setWikiPage(this);
    return data;
  }

  private static Object getObjectFromUrl(URL url) {
    Object obj;
    InputStream is = null;
    ObjectInputStream ois = null;
    try {
      is = url.openStream();
      ois = new ObjectInputStream(is);
      obj = ois.readObject();
      return obj;
    } catch (IOException e) {
      throw new RuntimeException("An error occured reading data from " + url, e);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException("An error occured reading data from " + url, e);
    } finally {
      try {
      if (is != null)
        is.close();
      if (ois != null)
        ois.close();
      } catch (IOException e) {
        
      }
    }
  }

  protected PageData makePageData() {
    return getMeat();
  }

  public PageData getDataVersion(String versionName) {
    PageData data = getMeat(versionName);
    if (data == null)
      throw new NoSuchVersionException("There is no version '" + versionName + "'");
    return data;
  }

  public boolean isOpenInNewWindow() {
    return true;
  }
}
