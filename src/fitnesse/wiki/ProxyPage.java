// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import fitnesse.http.ResponseParser;
import util.TimeMeasurement;

public class ProxyPage extends BaseWikiPage implements Serializable {
  private static final long serialVersionUID = 1L;

  public static int retrievalCount = 0;
  public static int cacheTime = 3000;

  private String host;
  private int hostPort;
  private WikiPagePath realPath;
  public ResponseParser parser;
  private Map<String, WikiPage> children = new HashMap<String, WikiPage>();
  private transient SoftReference<PageData> cachedData;
  private transient TimeMeasurement cachedTime;

  public ProxyPage(WikiPage original) {
    super(original.getName(), null, null);
    realPath = original.getPageCrawler().getFullPath(original);

    List<?> children = original.getChildren();
    for (Iterator<?> iterator = children.iterator(); iterator.hasNext();) {
      ProxyPage child = new ProxyPage((WikiPage) iterator.next());
      child.parent = this;
      this.children.put(child.getName(), child);
    }
  }

  protected ProxyPage(String name, WikiPage parent) {
    super(name, parent, null);
  }

  public ProxyPage(String name, WikiPage parent, String host, int port, WikiPagePath path) {
    super(name, parent, null);
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
    int port = url.getPort();
    page.setHostPort(url.getHost(), (port == -1) ? 80 : port);
    return page;
  }

  public WikiPage addChildPage(String name) {
    WikiPagePath childPath = realPath.copy().addNameToEnd(name);
    WikiPage page = new ProxyPage(name, this, host, getHostPort(), childPath);
    children.put(name, page);
    return page;
  }

  @Override
  public List<WikiPage> getNormalChildren() {
    loadChildren();
    return getCachedChildren();
  }

  public List<WikiPage> getCachedChildren() {
    return new ArrayList<WikiPage>(children.values());
  }

  @Override
  public WikiPage getNormalChildPage(String name) {
    if (hasChildPage(name))
      return children.get(name);
    else
      return null;
  }

  private void loadChildren() {
    if (cachedDataExpired()) {
      forceLoadChildren();
    }
  }

  private void forceLoadChildren() {
    ProxyPage page = retrievePage(getThisPageUrl());
    children.clear();
    for (Iterator<?> iterator = page.children.values().iterator(); iterator.hasNext();) {
      ProxyPage child = (ProxyPage) iterator.next();
      child.parent = this;
      children.put(child.getName(), child);
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
      forceLoadChildren();
      return children.containsKey(pageName);
    }
  }

  @Override
  public VersionInfo commit(PageData data) {
    setCachedData(makePageData());
    return null;
  }

  @Override
  public Collection<VersionInfo> getVersions() {
    return (Collection<VersionInfo>) getObjectFromUrl(getThisPageUrl() + "?responder=proxy&type=versions");
  }

  @Override
  public void removeChildPage(String name) {
    if (children.containsKey(name))
      children.remove(name);
  }

  private void setCachedData(PageData data) {
    if (cachedData != null)
      cachedData.clear();
    cachedData = new SoftReference<PageData>(data);
    cachedTime = new TimeMeasurement().start();
  }

  public PageData getData() {
    if (cachedDataExpired()) {
      reloadCache();
    }
    return new PageData(getCachedData());
  }

  public ReadOnlyPageData readOnlyData() {
    if (getCachedData() == null) {
      reloadCache();
    }
    return getCachedData();
  }

  private boolean cachedDataExpired() {
    return getCachedData() == null || cachedTime.elapsed() >= cacheTime;
  }

  public PageData getCachedData() {
    if (cachedData != null)
      return cachedData.get();
    else
      return null;
  }

  private void reloadCache() {
    PageData data = makePageData();
    setCachedData(data);
  }

  public String getHost() {
    return host;
  }

  public void setHostPort(String host, int port) {
    this.host = host;
    this.hostPort = port;
    for (Iterator<WikiPage> i = children.values().iterator(); i.hasNext();) {
      ProxyPage page = (ProxyPage) i.next();
      page.setHostPort(host, port);
    }
  }

  public int getHostPort() {
    return hostPort;
  }

  public PageData getMeat(String versionName) {
    StringBuffer urlString = new StringBuffer(getThisPageUrl());
    urlString.append("?responder=proxy&type=meat");
    if (versionName != null)
      urlString.append("&version=").append(versionName);
    PageData data = (PageData) getObjectFromUrl(urlString.toString());
    if (data != null)
      data.setWikiPage(this);
    return data;
  }

  private static Object getObjectFromUrl(String urlString) {
    URL url;
    try {
      url = new URL(urlString);
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
    return getObjectFromUrl(url);
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
    return getMeat(null);
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
