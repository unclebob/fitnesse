// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;

namespace fitnesse.handlers
{
	public class CellHandlerLoaderFixture : Fixture
	{
		public override void DoRow(Parse row)
		{
			switch (GetTypeOfOperation(row))
			{
				case "load":
					CellOperation.LoadHandler(GetHandler(row.Parts.More.Text));
					break;
				case "remove":
					CellOperation.RemoveHandler(GetHandler(row.Parts.More.Text));
					break;
				case "clear":
					CellOperation.ClearHandlers();
					break;
				case "loaddefaults":
					CellOperation.LoadDefaultHandlers();
					break;
			}
		}

		private static string GetTypeOfOperation(Parse row)
		{
			return row.Parts.Text.ToLower();
		}

		private ICellHandler GetHandler(string name)
		{
			ObjectFactory factory = new ObjectFactory("fixture");
			if (factory.IsTypeAvailable(name))
			{
				return factory.CreateInstance(name) as ICellHandler;
			}
			return null;
		}

	}
}