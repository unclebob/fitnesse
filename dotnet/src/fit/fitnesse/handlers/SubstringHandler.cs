// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Text.RegularExpressions;
using fit;

namespace fitnesse.handlers
{
	public class SubstringHandler : AbstractCellHandler
	{
		public override bool Match(string searchString, Type type)
		{
			return Regex.IsMatch(searchString, "^\\.\\..*\\.\\.$");
		}

		public override bool HandleEvaluate(Fixture fixture, Parse cell, Accessor accessor)
		{
			if (GetActual(accessor, fixture) == null)
			{
				return false;
			}
			return GetActual(accessor, fixture).ToString().IndexOf(ExtractExpectedSubstring(cell)) > -1;
		}

		private static string ExtractExpectedSubstring(Parse cell)
		{
			return cell.Text.Substring(2, cell.Text.Length - 4);
		}
	}
}