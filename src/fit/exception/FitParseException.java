// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fit.exception;

import java.text.ParseException;

public class FitParseException extends ParseException
{
	public FitParseException(String s, int i)
	{
		super(s, i);
	}
}
