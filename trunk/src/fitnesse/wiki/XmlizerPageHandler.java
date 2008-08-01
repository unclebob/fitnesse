// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.Date;

public interface XmlizerPageHandler
{
	void enterChildPage(WikiPage newPage, Date lastModified) throws Exception;

	void exitPage();
}
