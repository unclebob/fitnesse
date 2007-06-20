// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.ColumnFixture;
import fitnesse.wikitext.Utils;

import java.util.StringTokenizer;
import java.util.regex.*;

public class ResponseExaminer extends ColumnFixture
{
	public String type;
	public String pattern;
	public String value;
	public int number;
	private Matcher matcher;
	private int currentLine = 0;

	public String contents() throws Exception
	{
		return Utils.escapeText(FitnesseFixtureContext.sender.sentData());
	}

	public String fullContents() throws Exception
	{
		return Utils.escapeText(FitnesseFixtureContext.sender.sentData());
	}

	public boolean inOrder() throws Exception
	{
		if(value == null)
		{
			return false;
		}
		String pageContent = FitnesseFixtureContext.sender.sentData();
		String[] lines = arrayifyLines(pageContent);
		for(int i = currentLine; i < lines.length; i++)
		{
			if(value.equals(lines[i].trim()))
			{
				currentLine = i;
				return true;
			}
		}
		return false;
	}

	public int matchCount() throws Exception
	{
		Pattern p = Pattern.compile(Utils.escapeText(pattern), Pattern.MULTILINE + Pattern.DOTALL);
		value = null;
		if(type.equals("contents"))
			value = contents();
		else if(type.equals("fullContents"))
			value = fullContents();
		else if(type.equals("status"))
			value = "" + FitnesseFixtureContext.response.getStatus();
		else if(type.equals("headers"))
		{
			String text = FitnesseFixtureContext.sender.sentData();
			int headerEnd = text.indexOf("\r\n\r\n");
			value = text.substring(0, headerEnd + 2);
		}

		matcher = p.matcher(value);
		int matches = 0;
		for(matches = 0; matcher.find(); matches++) ;
		return matches;
	}

	public boolean matches() throws Exception
	{
		return matchCount() > 0;
	}

	public String string() throws Exception
	{
		String value = null;
		if(type.equals("contents"))
		{
			return FitnesseFixtureContext.page.getData().getHtml();
		}
		else if(type.equals("line"))
		{
			String pageContent = FitnesseFixtureContext.page.getData().getHtml();
			String lineizedContent = convertBreaksToLineSeparators(pageContent);
			StringTokenizer tokenizedLines = tokenizeLines(lineizedContent);
			for(int i = number; i != 0; i--)
				value = tokenizedLines.nextToken();
			return value.trim();
		}
		else
		{
			throw new Exception("Bad type in ResponseExaminer");
		}
	}

	private StringTokenizer tokenizeLines(String lineizedContent)
	{
		return new StringTokenizer(lineizedContent, System.getProperty("line.separator"));
	}

	private String[] arrayifyLines(String lineizedContent)
	{
		return lineizedContent.split(System.getProperty("line.separator"));
	}

	private String convertBreaksToLineSeparators(String pageContent)
	{
		String lineizedContent = pageContent.replaceAll("<br>", System.getProperty("line.separator"));
		return lineizedContent;
	}

	public String found()
	{
		return matcher.group(0);
	}

	public String source()
	{
		return value;
	}
}
