// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import fit.Counts;

public interface FitClientListener
{
	public void acceptOutput(String output) throws Exception;

	public void acceptResults(Counts counts) throws Exception;

	public void exceptionOccurred(Exception e);
}
