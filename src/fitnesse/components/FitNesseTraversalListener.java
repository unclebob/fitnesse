// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import fitnesse.wiki.WikiPage;

//TODO rename me to TraversalListener
public interface FitNesseTraversalListener
{
	public void processPage(WikiPage page) throws Exception;

	public String getSearchPattern() throws Exception;
}
