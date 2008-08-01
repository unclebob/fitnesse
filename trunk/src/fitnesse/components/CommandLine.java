// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import java.util.*;
import java.util.regex.*;

public class CommandLine extends Option
{
	private static Pattern optionPattern = Pattern.compile("\\[-(\\w+)((?: \\w+)*)\\]");
	private Map possibleOptions = new HashMap();

	public CommandLine(String optionDescriptor)
	{
		int optionEndIndex = 0;
		Matcher matcher = optionPattern.matcher(optionDescriptor);
		while(matcher.find())
		{
			Option option = new Option();
			option.parseArgumentDescriptor(matcher.group(2));
			possibleOptions.put(matcher.group(1), option);
			optionEndIndex = matcher.end();
		}

		String remainder = optionDescriptor.substring(optionEndIndex);
		parseArgumentDescriptor(remainder);
	}

	public boolean parse(String[] args)
	{
		boolean successfulParse = true;
		Option currentOption = this;
		for(int i = 0; successfulParse && i < args.length; i++)
		{
			String arg = args[i];

			if(currentOption != this && !currentOption.needsMoreArguments())
				currentOption = this;

			if(arg.startsWith("-"))
			{
				if(currentOption.needsMoreArguments() && currentOption != this)
					successfulParse = false;
				else
				{
					String argName = arg.substring(1);
					currentOption = (Option) possibleOptions.get(argName);
					if(currentOption != null)
						currentOption.active = true;
					else
						successfulParse = false;
				}
			}
			else if(currentOption.needsMoreArguments())
				currentOption.addArgument(arg);
			else // too many args
				successfulParse = false;
		}
		if(successfulParse && currentOption.needsMoreArguments())
			successfulParse = false;
		return successfulParse;
	}

	public boolean hasOption(String optionName)
	{
		Option option = (Option) possibleOptions.get(optionName);
		if(option == null)
			return false;

		return option.active;
	}

	public String getOptionArgument(String optionName, String argName)
	{
		Option option = (Option) possibleOptions.get(optionName);
		if(option == null)
			return null;
		else
			return option.getArgument(argName);
	}
}

class Option
{
	public boolean active = false;
	protected String[] argumentNames;
	protected String[] argumentValues;
	protected int argumentIndex = 0;

	protected void parseArgumentDescriptor(String arguments)
	{
		String[] tokens = split(arguments);
		argumentNames = tokens;
		argumentValues = new String[argumentNames.length];
	}

	public String getArgument(String argName)
	{
		for(int i = 0; i < argumentNames.length; i++)
		{
			String requiredArgumentName = argumentNames[i];
			if(requiredArgumentName.equals(argName))
				return argumentValues[i];
		}
		return null;
	}

	public boolean needsMoreArguments()
	{
		return argumentIndex < argumentNames.length;
	}

	public void addArgument(String value)
	{
		argumentValues[argumentIndex++] = value;
	}

	protected String[] split(String value)
	{
		String[] tokens = value.split(" ");
		List usableTokens = new LinkedList();
		for(int i = 0; i < tokens.length; i++)
		{
			String token = tokens[i];
			if(token.length() > 0)
				usableTokens.add(token);
		}
		return (String[]) usableTokens.toArray(new String[]{});
	}
}
