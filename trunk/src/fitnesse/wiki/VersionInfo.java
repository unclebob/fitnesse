// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.*;

public class VersionInfo implements Comparable, Serializable
{
	public static final Pattern COMPEX_NAME_PATTERN = Pattern.compile("(?:([a-zA-Z][^\\-]*)-)?(?:\\d+-)?(\\d{14})");
	private static int counter = 0;

	public static SimpleDateFormat makeVersionTimeFormat()
	{
		//SimpleDateFormat is not thread safe, so we need to create each instance independently.
		return new SimpleDateFormat("yyyyMMddHHmmss");
	}

	public static int nextId()
	{
		return counter++;
	}

	private String name;
	private String author;
	private Date creationTime;

	public VersionInfo(String name, String author, Date creationTime)
	{
		this.name = name;
		this.author = author;
		this.creationTime = creationTime;
	}

	public VersionInfo(String complexName) throws Exception
	{
		this(complexName, "", new Date());
		Matcher match = COMPEX_NAME_PATTERN.matcher(complexName);
		if(match.find())
		{
			author = match.group(1);
			if(author == null)
				author = "";
			creationTime = makeVersionTimeFormat().parse(match.group(2));
		}
	}

	public String getAuthor()
	{
		return author;
	}

	public Date getCreationTime()
	{
		return creationTime;
	}

	public String getName()
	{
		return name;
	}

	public static String getVersionNumber(String complexName)
	{
		Matcher match = COMPEX_NAME_PATTERN.matcher(complexName);
		match.find();
		return match.group(2);
	}

	public int compareTo(Object o)
	{
		VersionInfo otherVersion;
		if(o instanceof VersionInfo)
		{
			otherVersion = ((VersionInfo) o);
			return getCreationTime().compareTo(otherVersion.getCreationTime());
		}
		else
			return 0;
	}

	public String toString()
	{
		return getName();
	}

	public boolean equals(Object o)
	{
		if(o != null && o instanceof VersionInfo)
		{
			VersionInfo otherVersion = (VersionInfo) o;
			return getName().equals(otherVersion.getName());
		}
		else
			return false;
	}

	public int hashCode()
	{
		return getName().hashCode();
	}
}