// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.

package fitnesse.wiki;

import java.io.Serializable;
import java.util.List;

public interface WikiPage extends Serializable, Comparable
{
	public static final String SECURE_READ = "secure-read";
	public static final String SECURE_WRITE = "secure-write";
	public static final String SECURE_TEST = "secure-test";
	public static final String LAST_MODIFYING_USER = "LastModifyingUser";
	public String[] STANDARD_ATTRIBUTES = {"Test", "Search", "Edit", "Properties", "Suite", "Versions", "Refactor", "WhereUsed", "Files"};
	public String[] SECURITY_ATTRIBUTES = {SECURE_READ, SECURE_WRITE, SECURE_TEST};

	public WikiPage getParent() throws Exception;

	public WikiPage addChildPage(String name) throws Exception;

	public boolean hasChildPage(String name) throws Exception;

	public WikiPage getChildPage(String name) throws Exception;

	public void removeChildPage(String name) throws Exception;

	public List getChildren() throws Exception;

	public String getName() throws Exception;

	public PageData getData() throws Exception;

	public PageData getDataVersion(String versionName) throws Exception;

	public VersionInfo commit(PageData data) throws Exception;

	public PageCrawler getPageCrawler();

	//TODO Delete these method alone with ProxyPage when the time is right.
	public boolean hasExtension(String extensionName);

	public Extension getExtension(String extensionName);
}



