// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

import fit.*;

import java.io.*;

public class FitFilter
{

	public String input;
	public Parse tables;
	public Fixture fixture = new Fixture();
	public PrintWriter output;

	public static void main(String argv[])
	{
		new FitFilter().run(argv);
	}

	public void run(String argv[])
	{
		args(argv);
		process();
		exit();
	}

	public void process()
	{
		try
		{
			tables = new Parse(input);
			fixture.doTables(tables);
		}
		catch(Exception e)
		{
			exception(e);
		}
		tables.print(output);
	}

	public void args(String[] argv)
	{
		if(argv.length != 0)
		{
			System.err.println("usage: java fitnesse.FitFilter");
			System.exit(-1);
		}
		try
		{
			input = read();
			output = new PrintWriter(System.out);
		}
		catch(IOException e)
		{
			System.err.println(e.getMessage());
			System.exit(-1);
		}
	}

	protected String read() throws IOException
	{
		StringBuffer buffer = new StringBuffer();
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		String line;
		while((line = br.readLine()) != null)
			buffer.append(line).append("\n");
		return buffer.toString();
	}

	protected void exception(Exception e)
	{
		tables = new Parse("body", "Unable to parse input. Input ignored.", null, null);
		fixture.exception(tables, e);
	}

	protected void exit()
	{
		output.close();
		System.exit(fixture.counts.wrong + fixture.counts.exceptions);
	}

}
