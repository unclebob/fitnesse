using System;
using System.Text;

namespace fit
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
