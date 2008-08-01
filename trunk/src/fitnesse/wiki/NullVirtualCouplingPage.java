// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.util.*;

public class NullVirtualCouplingPage extends VirtualCouplingPage
{
	public NullVirtualCouplingPage(WikiPage hostPage) throws Exception
	{
		super(hostPage);
	}

	public List getChildren() throws Exception
	{
		return new ArrayList();
	}
}
