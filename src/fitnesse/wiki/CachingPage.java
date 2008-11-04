// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.

package fitnesse.wiki;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CachingPage extends CommitingPage {
    private static final long serialVersionUID = 1L;

    public static int cacheTime = 3000;

    protected Map<String, WikiPage> children = new HashMap<String, WikiPage>();
    private transient SoftReference<PageData> cachedData;
    private transient long cachedDataCreationTime = 0;

    public CachingPage(String name, WikiPage parent) throws Exception {
        super(name, parent);
        addExtention(new VirtualCouplingExtension(this));
    }

    public abstract boolean hasChildPage(String pageName) throws Exception;

    protected abstract WikiPage createChildPage(String name) throws Exception;

    protected abstract void loadChildren() throws Exception;

    protected abstract PageData makePageData() throws Exception;

    public WikiPage addChildPage(String name) throws Exception {
        WikiPage page = createChildPage(name);
        children.put(name, page);
        return page;
    }

    @Override
    public List<WikiPage> getNormalChildren() throws Exception {
        loadChildren();
        return getCachedChildren();
    }

    public List<WikiPage> getCachedChildren() throws Exception {
        return new ArrayList<WikiPage>(children.values());
    }

    public void removeChildPage(String name) throws Exception {
        if (hasCachedSubpage(name))
            children.remove(name);
    }

    @Override
    public WikiPage getNormalChildPage(String name) throws Exception {
        if (hasCachedSubpage(name) || hasChildPage(name))
            return children.get(name);
        else
            return null;
    }

    protected boolean hasCachedSubpage(String name) {
        return children.containsKey(name);
    }

    public PageData getData() throws Exception {
        if (cachedDataExpired()) {
            PageData data = makePageData();
            setCachedData(data);
        }
        return new PageData(getCachedData());
    }

    private boolean cachedDataExpired() throws Exception {
        long now = System.currentTimeMillis();
        return getCachedData() == null || now >= cachedDataCreationTime + cacheTime;
    }

    public void dumpExpiredCachedData() throws Exception {
        if (cachedDataExpired())
            clearCache();
    }

    @Override
    public VersionInfo commit(PageData data) throws Exception {
        VersionInfo versionInfo = super.commit(data);
        setCachedData(makePageData());
        return versionInfo;
    }

    private void setCachedData(PageData data) throws Exception {
        if (cachedData != null)
            cachedData.clear();
        cachedData = new SoftReference<PageData>(data);
        cachedDataCreationTime = System.currentTimeMillis();
    }

    public PageData getCachedData() throws Exception {
        if (cachedData != null)
            return cachedData.get();
        else
            return null;
    }

    public void clearCache() {
        cachedData.clear();
        cachedData = null;
    }
}