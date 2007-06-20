// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.RowFixture;

import java.util.ArrayList;

public class PrimeFactorsFixture extends RowFixture
{
	public static class Factor
	{
		public Factor(int factor)
		{
			this.factor = factor;
		}

		public int factor;
	}

	public Object[] query()
	{
		int n = Integer.parseInt(args[0]);
		ArrayList factors = new ArrayList();
		for(int f = 2; n > 1; f++)
			for(; n % f == 0; n /= f)
				factors.add(new Factor(f));
		return (Factor[]) factors.toArray(new Factor[0]);
	}

	public Class getTargetClass()             // get expected type of row
	{
		return Factor.class;
	}
}
