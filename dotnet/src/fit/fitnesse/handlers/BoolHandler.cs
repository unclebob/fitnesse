// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Text.RegularExpressions;
using fit;

namespace fitnesse.handlers
{
	public class BoolHandler : AbstractCellHandler
	{
		public override bool Match(string searchString, Type type)
		{
			return ImpliesTrue(searchString) || ImpliesFalse(searchString);
		}

		public override void HandleInput(Fixture fixture, Parse cell, Accessor accessor)
		{
			if (accessor.TypeAdapter.type == typeof (bool))
			{
				if (ImpliesTrue(cell.Text))
				{
					accessor.Set(fixture, "true");
				}
				else
				{
					accessor.Set(fixture, "false");
				}
			}
			else
			{
				base.HandleInput(fixture, cell, accessor);
			}
		}

		

		public override bool HandleEvaluate(Fixture fixture, Parse cell, Accessor accessor)
		{
			if (accessor.TypeAdapter.type == typeof (bool))
			{
				if (ImpliesTrue(accessor.Get(fixture).ToString()) && ImpliesTrue(cell.Text))
				{
					return true;
				}
				else if (ImpliesFalse(accessor.Get(fixture).ToString()) && ImpliesFalse(cell.Text))
				{
					return true;
				}
				else
				{
					return false;
				}
			}
			else
			{
				return base.HandleEvaluate(fixture, cell, accessor);
			}
		}


		private bool ImpliesTrue(string possibleTrue)
		{
			return Regex.IsMatch(possibleTrue, "^([Yy]|[Yy][Ee][Ss]|[Tt][Rr][Uu][Ee])$");
		}

		private bool ImpliesFalse(string possibleFalse)
		{
			return Regex.IsMatch(possibleFalse, "^([Nn]|[Nn][Oo]|[Ff][Aa][Ll][Ss][Ee])$");
		}
	}
}