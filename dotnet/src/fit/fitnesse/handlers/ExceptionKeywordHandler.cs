// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Reflection;
using System.Text.RegularExpressions;
using fit;

namespace fitnesse.handlers
{
	public class ExceptionKeywordHandler : AbstractCellHandler
	{
		public override bool Match(string searchString, Type type)
		{
			return Regex.IsMatch(searchString, "^exception\\[.*\\]$");
		}

		public override void HandleCheck(Fixture fixture, Parse cell, Accessor accessor)
		{
			string exceptionContent = cell.Text.Substring("exception[".Length, cell.Text.Length - ("exception[".Length + 1));
			try
			{
				GetActual(accessor, fixture);
			}
			catch (TargetInvocationException e)
			{
				if (isMessageOnly(exceptionContent))
				{
					evaluateException(e.InnerException.Message == exceptionContent.Substring(1, exceptionContent.Length - 2), fixture, cell, e);
				}
				else if (isExceptionTypeNameOnly(exceptionContent))
				{
					string actual = e.InnerException.GetType().Name + ": \"" + e.InnerException.Message + "\"";
					evaluateException(exceptionContent == actual, fixture, cell, e);
				}
				else
				{
					evaluateException(e.InnerException.GetType().Name == exceptionContent, fixture, cell, e);
				}
			}
		}

		private bool isExceptionTypeNameOnly(string exceptionContent)
		{
			return Regex.IsMatch(exceptionContent, "^.*: \".*\"$");
		}

		private bool isMessageOnly(string exceptionContent)
		{
			return Regex.IsMatch(exceptionContent, "^\".*\"$");
		}

		private void evaluateException(bool expression, Fixture fixture, Parse cell, TargetInvocationException e)
		{
			if (expression)
			{
				fixture.Right(cell);
			}
			else
			{
				fixture.Wrong(cell, "exception[" + e.InnerException.GetType().Name + ": \"" + e.InnerException.Message + "\"]");
			}
		}
	}
}