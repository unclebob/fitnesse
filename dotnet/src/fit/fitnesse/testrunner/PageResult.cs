// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Text;
using System.Text.RegularExpressions;

namespace fit
{
	public class PageResult
	{
		private static readonly Regex countsRegex = new Regex("(\\d+)[^,]*, (\\d+)[^,]*, (\\d+)[^,]*, (\\d+)[^,]*");

		private StringBuilder contentBuffer = new StringBuilder();
		private Counts counts;
		private string title;

		public PageResult(String title)
		{
			this.title = title;
		}

		public PageResult(String title, Counts counts, String startingContent) : this(title)
		{
			this.counts = counts;
			Append(startingContent);
		}

		public String Content()
		{
			return contentBuffer.ToString();
		}

		public void Append(String data)
		{
			contentBuffer.Append(data);
		}

		public String Title()
		{
			return title;
		}

		public Counts Counts()
		{
			return counts;
		}

		public void setCounts(Counts counts)
		{
			this.counts = counts;
		}

		public override string ToString()
		{
			StringBuilder buffer = new StringBuilder();
			buffer.Append(title).Append("\n");
			buffer.Append(counts.ToString()).Append("\n");
			buffer.Append(contentBuffer);
			return buffer.ToString();
		}

		public static PageResult Parse(String resultString)
		{
			int firstEndlIndex = resultString.IndexOf('\n');
			int secondEndlIndex = resultString.IndexOf('\n', firstEndlIndex + 1);

			String title = resultString.Substring(0, firstEndlIndex);
			string countSubstring = resultString.Substring(firstEndlIndex + 1, secondEndlIndex - firstEndlIndex);
			Counts counts = ParseCounts(countSubstring);
			String content = resultString.Substring(secondEndlIndex + 1);

			return new PageResult(title, counts, content);
		}

		private static Counts ParseCounts(String countString)
		{
			Match matcher = countsRegex.Match(countString);
			if(matcher.Success)
			{
				int right = Int32.Parse(matcher.Groups[1].Value);
				int wrong = Int32.Parse(matcher.Groups[2].Value);
				int ignores = Int32.Parse(matcher.Groups[3].Value);
				int exceptions = Int32.Parse(matcher.Groups[4].Value);
				return new Counts(right, wrong, ignores, exceptions);
			}
			else
				return null;
		}
	}
}
