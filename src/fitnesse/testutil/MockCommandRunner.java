// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.testutil;

import fitnesse.components.CommandRunner;

public class MockCommandRunner extends CommandRunner
{
	public MockCommandRunner()
	{
		super("", "");
	}

	public MockCommandRunner(String command, int exitCode)
	{
		super(command, "");
		this.exitCode = exitCode;
	}

	public void setOutput(String output)
	{
		outputBuffer = new StringBuffer(output);
	}

	public void setError(String error)
	{
		errorBuffer = new StringBuffer(error);
	}

	public void addException(Exception e)
	{
		exceptions.add(e);
	}

	public void setExitCode(int i)
	{
		exitCode = i;
	}
}
