// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.*;

public abstract class ExtendableWikiPage extends BaseWikiPage
{
	private Map extensions = new HashMap();

	public ExtendableWikiPage(String name, WikiPage parent)
	{
		super(name, parent);
	}

	protected void addExtention(Extension extension)
	{
		extensions.put(extension.getName(), extension);
	}

	public boolean hasExtension(String extensionName)
	{
		return extensions.containsKey(extensionName);
	}

	public Extension getExtension(String extensionName)
	{
		return (Extension) extensions.get(extensionName);
	}
}
