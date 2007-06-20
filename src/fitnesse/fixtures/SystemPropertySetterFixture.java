// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.ColumnFixture;

public class SystemPropertySetterFixture extends ColumnFixture
{
	public String key;
	public String value;

	public void execute()
	{
		System.getProperties().setProperty(key, value);
	}
}
