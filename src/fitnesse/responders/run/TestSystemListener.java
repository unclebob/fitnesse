// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

public interface TestSystemListener
{
	public void acceptOutputFirst(String output) throws Exception;

	public void acceptResultsLast(TestSummary testSummary) throws Exception;

	public void exceptionOccurred(Throwable e);
}
