// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import java.util.*;

public class LogData implements Cloneable
{
	public String host;
	public GregorianCalendar time;
	public String requestLine;
	public int status;
	public int size;

	public Object clone() throws CloneNotSupportedException
	{
		LogData newData = (LogData) super.clone();
		newData.time = (GregorianCalendar) time.clone();

		return newData;
	}
}
