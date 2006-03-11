// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System.Text.RegularExpressions;
using fit;

namespace fitnesse.handlers
{
	public class EndsWithHandler : AbstractCellHandler
	{
		private static Regex matchExpression = 
			new Regex("^\\.\\.+.*[^\\.\\.]$");
		public override bool Match(string searchString, System.Type type)
		{
			return matchExpression.IsMatch(searchString);
		}

		public override bool HandleEvaluate(Fixture fixture, Parse cell, Accessor accessor)
		{
			return GetActual(accessor, fixture) != null && GetActual(accessor, fixture).ToString().EndsWith(GetExpected(cell));
		}

		private string GetExpected(Parse cell)
		{
			return cell.Text.Substring(2, cell.Text.Length - 2);
		}
	}
}