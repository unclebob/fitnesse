// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import fit.Counts;

public interface ResultHandler
{
	void acceptResult(PageResult result) throws Exception;

	void acceptFinalCount(Counts count) throws Exception;
}
