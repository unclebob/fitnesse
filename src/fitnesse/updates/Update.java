// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.updates;

public interface Update
{
	public String getName();

	public String getMessage();

	public boolean shouldBeApplied() throws Exception;

	public void doUpdate() throws Exception;
}
