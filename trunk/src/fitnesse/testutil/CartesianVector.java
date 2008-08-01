// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.testutil;

import java.util.regex.*;

public class CartesianVector
{
	private double x = 0;
	private double y = 0;

	public CartesianVector()
	{
	}

	public CartesianVector(double x, double y)
	{
		this.x = x;
		this.y = y;
	}

	public static CartesianVector parse(String s)
	{
		Pattern vectorPattern = Pattern.compile("\\((.*),(.*)\\)");
		Matcher vectorMatcher = vectorPattern.matcher(s);
		if(vectorMatcher.matches())
		{
			double x = Double.parseDouble(vectorMatcher.group(1));
			double y = Double.parseDouble(vectorMatcher.group(2));
			return new CartesianVector(x, y);
		}
		return null;
	}

	public boolean equals(Object obj)
	{
		if(obj instanceof CartesianVector)
		{
			CartesianVector v = (CartesianVector) obj;
			if(v.x == x && v.y == y)
				return true;
		}
		return false;
	}

	public double getX()
	{
		return x;
	}

	public double getY()
	{
		return y;
	}

	public CartesianVector add(CartesianVector v)
	{
		return new CartesianVector(v.getX() + x, v.getY() + y);
	}
}
