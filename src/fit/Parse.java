// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fit;

// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

import java.io.*;
import fit.exception.FitParseException;

public class Parse
{

	public String leader;
	public String tag;
	public String body;
	public String end;
	public String trailer;

	public Parse more;
	public Parse parts;

	public Parse(String tag, String body, Parse parts, Parse more)
	{
		this.leader = "\n";
		this.tag = "<" + tag + ">";
		this.body = body;
		this.end = "</" + tag + ">";
		this.trailer = "";
		this.parts = parts;
		this.more = more;
	}

	public static String tags[] = {"table", "tr", "td"};

	public Parse(String text) throws FitParseException
	{
		this(text, tags, 0, 0);
	}

	public Parse(String text, String tags[]) throws FitParseException
	{
		this(text, tags, 0, 0);
	}

	public Parse(String text, String tags[], int level, int offset) throws FitParseException
	{
		String lc = text.toLowerCase();
		int startTag = lc.indexOf("<" + tags[level]);
		int endTag = lc.indexOf(">", startTag) + 1;
		int startEnd = lc.indexOf("</" + tags[level], endTag);
		int endEnd = lc.indexOf(">", startEnd) + 1;
		int startMore = lc.indexOf("<" + tags[level], endEnd);
		if(startTag < 0 || endTag < 0 || startEnd < 0 || endEnd < 0)
		{
			throw new FitParseException("Can't find tag: " + tags[level], offset);
		}

		leader = text.substring(0, startTag);
		tag = text.substring(startTag, endTag);
		body = text.substring(endTag, startEnd);
		end = text.substring(startEnd, endEnd);
		trailer = text.substring(endEnd);

		if(level + 1 < tags.length)
		{
			parts = new Parse(body, tags, level + 1, offset + endTag);
			body = null;
		}

		if(startMore >= 0)
		{
			more = new Parse(trailer, tags, level, offset + endEnd);
			trailer = null;
		}
	}

	public int size()
	{
		return more == null ? 1 : more.size() + 1;
	}

	public Parse last()
	{
		return more == null ? this : more.last();
	}

	public Parse leaf()
	{
		return parts == null ? this : parts.leaf();
	}

	public Parse at(int i)
	{
		return i == 0 || more == null ? this : more.at(i - 1);
	}

	public Parse at(int i, int j)
	{
		return at(i).parts.at(j);
	}

	public Parse at(int i, int j, int k)
	{
		return at(i, j).parts.at(k);
	}

	public String text()
	{
		return unescape(unformat(body)).trim();
	}

	public static String unformat(String s)
	{
		int i = 0, j;
		while((i = s.indexOf('<', i)) >= 0)
		{
			if((j = s.indexOf('>', i + 1)) > 0)
			{
				s = s.substring(0, i) + s.substring(j + 1);
			}
			else
				break;
		}
		return s;
	}

	public static String unescape(String s)
	{
		int i = -1, j;
		while((i = s.indexOf('&', i + 1)) >= 0)
		{
			if((j = s.indexOf(';', i + 1)) > 0)
			{
				String from = s.substring(i + 1, j).toLowerCase();
				String to = null;
				if((to = replacement(from)) != null)
				{
					s = s.substring(0, i) + to + s.substring(j + 1);
				}
			}
		}
		return s;
	}

	public static String replacement(String from)
	{
		if(from.equals("lt"))
			return "<";
		else if(from.equals("gt"))
			return ">";
		else if(from.equals("amp"))
			return "&";
		else if(from.equals("nbsp"))
			return " ";
		else
			return null;
	}

	public void addToTag(String text)
	{
		int last = tag.length() - 1;
		tag = tag.substring(0, last) + text + ">";
	}

	public void addToBody(String text)
	{
		body = body + text;
	}

	public void print(PrintWriter out)
	{
		out.print(leader);
		out.print(tag);
		if(parts != null)
		{
			parts.print(out);
		}
		else
		{
			out.print(body);
		}
		out.print(end);
		if(more != null)
		{
			more.print(out);
		}
		else
		{
			out.print(trailer);
		}
	}
}
