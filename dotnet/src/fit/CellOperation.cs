// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Collections;
using fitnesse.handlers;

namespace fit
{
	public class CellOperation
	{
		private static IList handlers = new ArrayList();
		private static ICellHandler defaultHandler = null;

		static CellOperation()
		{
			LoadDefaultHandlers();
		}

		public static IList Handlers
		{
			get
			{
				IList copy = new ArrayList();
				if (defaultHandler != null)
					copy.Add(defaultHandler);
				foreach (ICellHandler handler in handlers)
					copy.Add(handler);
				return copy;
			}
		}

		public static void ClearHandlers()
		{
			handlers.Clear();
			defaultHandler = null;
		}

		public static void LoadDefaultHandlers()
		{
			LoadDefaultHandler(new DefaultCellHandler());
			LoadHandler(new BlankKeywordHandler());
			LoadHandler(new NullKeywordHandler());
			LoadHandler(new ErrorKeywordHandler());
			LoadHandler(new EmptyCellHandler());
			LoadHandler(new BoolHandler());
			LoadHandler(new SymbolSaveHandler());
			LoadHandler(new SymbolRecallHandler());
			LoadHandler(new ExceptionKeywordHandler());
			LoadHandler(new FailKeywordHandler());
		}

		public static void Input(Fixture fixture, string memberName, Parse cell) {
			Accessor accessor = GetAccessor(fixture, memberName);
			ICellHandler handler = GetHandler(cell, accessor);
			handler.HandleInput(fixture, cell, accessor);
		}

		public static void Check(Fixture fixture, string memberName, Parse cell) {
			Accessor accessor = GetAccessor(fixture, memberName);
			ICellHandler handler = GetHandler(cell, accessor);
			handler.HandleCheck(fixture, cell, accessor);
		}

		public static void Execute(Fixture fixture, string memberName, Parse cell) {
			Accessor accessor = GetAccessor(fixture, memberName);
			ICellHandler handler = GetHandler(cell, accessor);
			handler.HandleExecute(fixture, cell, accessor);
		}

		public static bool Evaluate(Fixture fixture, string memberName, Parse cell) {
			Accessor accessor = GetAccessor(fixture, memberName);
			ICellHandler handler = GetHandler(cell, accessor);
			return handler.HandleEvaluate(fixture, cell, accessor);
		}

		public static void Surplus(Fixture fixture, string memberName, Parse cell)
		{
			Accessor accessor = GetAccessor(fixture, memberName);
			cell.AddToBody(Fixture.Gray(GetTextRepresentationOfValue(accessor.Get(fixture))));
		}

		private static string GetTextRepresentationOfValue(object value)
		{
			return value == null ? "null" : value.ToString();
		}

		public static ICellHandler GetHandler(Parse cell, Accessor accessor)
		{
			return GetHandler(cell.Text, accessor.TypeAdapter.type);
		}

		private static Accessor GetAccessor(Fixture fixture, string memberName)
		{
			return AccessorFactory.Create(fixture.GetTargetObject().GetType(), memberName);
		}

		public static void LoadHandler(ICellHandler handler)
		{
			if (null == GetInstanceOfType(handlers, handler))
				handlers.Insert(0, handler);
			return;
		}

		public static void LoadDefaultHandler(ICellHandler handler)
		{
			defaultHandler = handler;
		}

		public static ICellHandler GetHandler(string searchString, Type type)
		{
			foreach (ICellHandler handler in handlers)
				if (handler.Match(searchString, type))
					return handler;
			return defaultHandler;
		}

		public static ICellHandler DefaultHandler
		{
			get { return defaultHandler; }
		}

		public static void RemoveHandler(ICellHandler handlerToRemove)
		{
			if (handlerToRemove != null)
			{
				if (defaultHandler.GetType() == handlerToRemove.GetType())
				{
					defaultHandler = null;
					return;
				}
				else
				{
					handlers.Remove(GetInstanceOfType(handlers, handlerToRemove));
				}
			}
		}

		private static object GetInstanceOfType(IList list, object obj)
		{
			foreach (object existingObject in list)
				if (existingObject.GetType() == obj.GetType())
					return existingObject;
			return null;
		}
	}
}