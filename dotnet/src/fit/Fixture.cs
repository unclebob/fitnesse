// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Collections;

namespace fit
{
	public class Fixture
	{
		private static Hashtable saveAndRecall = new Hashtable();
		private static Fixture lastFixtureLoaded = null;
		private string[] args;

		public static Fixture LastFixtureLoaded
		{
			get { return lastFixtureLoaded; }
		}

		public Hashtable Summary = new Hashtable();
		public Counts Counts = new Counts();
		public FixtureListener Listener = new NullFixtureListener();

		// Traversal //////////////////////////

		public virtual void DoTables(Parse tables)
		{
			InitializeNamespaces();
			Summary["run date"] = DateTime.Now;
			Summary["run elapsed time"] = new RunTime();
			while (tables != null)
			{
				Parse heading = tables.At(0, 0, 0);
				if (heading != null)
				{
					try
					{
						Fixture fixture = LoadFixture(heading.Text);
						fixture.Counts = Counts;
						fixture.Summary = Summary;
						fixture.GetArgsForTable(tables);
						fixture.DoTable(tables);
					}
					catch (Exception e)
					{
						Exception(heading, e);
					}
				}
				Listener.TableFinished(tables);
				tables = tables.More;
			}
			Listener.TablesFinished(Counts);
		}

		public virtual void DoTable(Parse table)
		{
			DoRows(table.Parts.More);
		}

		public virtual void DoRows(Parse rows)
		{
			while (rows != null)
			{
				Parse more = rows.More;
				DoRow(rows);
				rows = more;
			}
		}

		public virtual void DoRow(Parse row)
		{
			DoCells(row.Parts);
		}

		public virtual void DoCells(Parse cells)
		{
			for (int i = 0; cells != null; i++)
			{
				try
				{
					DoCell(cells, i);
				}
				catch (Exception e)
				{
					Exception(cells, e);
				}
				cells = cells.More;
			}
		}

		public virtual void DoCell(Parse cell, int columnNumber)
		{
			Ignore(cell);
		}

		// Annotation ///////////////////////////////

		public virtual void Right(Parse cell)
		{
			cell.AddToTag(" class=\"pass\"");
			Counts.Right++;
		}

		public virtual void Wrong(Parse cell)
		{
			cell.AddToTag(" class=\"fail\"");
			Counts.Wrong++;
		}

		public virtual void Wrong(Parse cell, string actual)
		{
			Wrong(cell);
			cell.AddToBody(Label("expected") + "<hr>" + Escape(actual) + Label("actual"));
		}

		public virtual void Ignore(Parse cell)
		{
			cell.AddToTag(" class=\"ignore\"");
			Counts.Ignores++;
		}

		public virtual void Exception(Parse cell, Exception Exception)
		{
			cell.AddToBody("<hr><pre><div class=\"fit_stacktrace\">" + Exception + "</div></pre>");
			cell.AddToTag(" class=\"error\"");
			Counts.Exceptions++;
		}

		// Utility //////////////////////////////////

		public static string Label(string text)
		{
			return " <span class=\"fit_label\">" + text + "</span>";
		}

		public static string Gray(string text)
		{
			return " <span class=\"fit_grey\">" + text + "</span>";
		}

		public static string Escape(string text)
		{
			return Escape(Escape(text, '&', "&amp;"), '<', "&lt;");
		}

		public static string Escape(string text, char from, string to)
		{
			int i = -1;
			while ((i = text.IndexOf(from, i + 1)) >= 0)
			{
				if (i == 0)
					text = to + text.Substring(1);
				else if (i == text.Length)
					text = text.Substring(0, i) + to;
				else
					text = text.Substring(0, i) + to + text.Substring(i + 1);
			}
			return text;
		}

		public void InitializeNamespaces()
		{
			if (ObjectFactory.Namespaces.Count == 0)
			{
				ResetNamespaces();
			}
		}

		protected internal Fixture LoadFixture(string className)
		{
			ObjectFactory factory = new ObjectFactory("fixture");
			Fixture fixture = null;
			try
			{
				fixture = (Fixture) factory.CreateInstance(className);
			}
			catch (InvalidCastException e)
			{
				throw new ApplicationException("Couldn't cast " + className + " to Fixture.  Did you remember to extend Fixture?", e);
			}
			return lastFixtureLoaded = fixture;
		}

		public static void ResetNamespaces()
		{
			ObjectFactory.ClearNamespaces();
			ObjectFactory.AddNamespace("fit");
			ObjectFactory.AddNamespace("fitnesse.handlers");
		}

		public static object Recall(string key)
		{
			return saveAndRecall[key];
		}

		public static void Save(string key, object value)
		{
			saveAndRecall[key] = value;
		}

		public static void ClearSaved()
		{
			saveAndRecall.Clear();
		}

		public virtual object GetTargetObject()
		{
			return this;
		}

		public string[] Args
		{
			get { return args; }
		}

		public void GetArgsForTable(Parse table)
		{
			ArrayList list = new ArrayList();
			list.Clear();
			Parse parameters = table.Parts.Parts.More;
			for (; parameters != null; parameters = parameters.More)
				list.Add(parameters.Text);
			args = new string[list.Count];
			for (int i = 0; i < list.Count; i++)
				args[i] = (string) list[i];
		}
	}
}