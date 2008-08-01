// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import fitnesse.wiki.WikiPage;

public interface SearchObserver
{
	public void hit(WikiPage page) throws Exception;
}
