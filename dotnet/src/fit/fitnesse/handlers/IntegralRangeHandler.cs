// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Text.RegularExpressions;
using fit;

namespace fitnesse.handlers
{
	public class IntegralRangeHandler : AbstractCellHandler
	{
		public override bool Match(string searchString, Type type)
		{
			return type == typeof (int) && Regex.IsMatch(searchString, "^-?[0-9]*\\.\\.-?[0-9]*$");
		}

		public override bool HandleEvaluate(Fixture fixture, Parse cell, Accessor accessor)
		{
			return IsInRange(Actual(accessor, fixture), LowEnd(Args(cell)), HighEnd(Args(cell)));
		}

		private string[] Args(Parse cell)
		{
			return cell.Text.Split('.');
		}

		private int Actual(Accessor accessor, Fixture fixture)
		{
			return (int) accessor.Get(fixture);
		}

		private int HighEnd(string[] args)
		{
			return Convert.ToInt32(args[args.Length - 1]);
		}

		private int LowEnd(string[] args)
		{
			return Convert.ToInt32(args[0]);
		}

		private bool IsInRange(int actual, int low, int high)
		{
			return actual >= low && actual <= high;
		}
	}
}