// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Text;
using fit;

namespace fitnesse.fitserver
{
	public class Protocol
	{
		public static string FormatInteger(int encodeInteger)
		{
			string numberPartOfString = "" + encodeInteger;
			return new String('0', 10 - numberPartOfString.Length) + numberPartOfString;
		}

		public static string FormatDocument(string document)
		{
			return Protocol.FormatInteger(document.Length) + document;
		}

		public static String FormatCounts(Counts counts)
		{
			StringBuilder builder = new StringBuilder();
			builder.Append(FormatInteger(0));
			builder.Append(FormatInteger(counts.Right));
			builder.Append(FormatInteger(counts.Wrong));
			builder.Append(FormatInteger(counts.Ignores));
			builder.Append(FormatInteger(counts.Exceptions));
			return builder.ToString();
		}
	}
}
