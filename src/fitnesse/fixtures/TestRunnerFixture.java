// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.*;
import fitnesse.runner.TestRunner;
import fitnesse.wikitext.Utils;
import java.io.*;
import java.util.*;

public class TestRunnerFixture extends ColumnFixture
{
	public String pageName;
	public String args;
	private TestRunner runner;
	private ByteArrayOutputStream outputBytes;

	public void execute() throws Exception
	{
		outputBytes = new ByteArrayOutputStream();
		runner = new TestRunner(new PrintStream(outputBytes));
		runner.run(buildArgs());
	}

	private String[] buildArgs()
	{
		List list = new LinkedList();
		if(args != null && !args.equals(""))
		{
			String[] startingArg = args.split(" ");
			for(int i = 0; i < startingArg.length; i++)
				list.add(startingArg[i]);
		}
		list.add("localhost");
		list.add(FitnesseFixtureContext.context.port + "");
		list.add(pageName);

		return (String[])list.toArray(new String[]{});
	}

	public int exitCode()
	{
		return runner.exitCode();
	}

	public String output()
	{
		String output = outputBytes.toString();
    output = output.replaceAll("\r", "");
    output = output.replaceAll("\n", "\\\\n");
		output = Utils.escapeText(output);
		
		return output;
	}
}
