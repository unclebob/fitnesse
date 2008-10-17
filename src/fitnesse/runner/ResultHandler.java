// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.runner;

import fit.Counts;
import fitnesse.responders.run.TestSystem;

public interface ResultHandler
{
	void acceptResult(PageResult result) throws Exception;

	void acceptFinalCount(TestSystem.TestSummary testSummary) throws Exception;
}
