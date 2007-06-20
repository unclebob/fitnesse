// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.updates;

import java.io.*;
import java.net.URL;

public class ReplacingFileUpdate extends FileUpdate
{
	public ReplacingFileUpdate(Updater updater, String source, String destination) throws Exception
	{
		super(updater, source, destination);
	}

	public void doUpdate() throws Exception
	{
		if(destinationFile().exists())
			destinationFile().delete();
		super.doUpdate();
	}

	public boolean shouldBeApplied() throws Exception
	{
		if(super.shouldBeApplied())
			return true;
		else
		{
			URL resource = getResource(source);
			if(resource != null)
			{
				long sourceSum = checkSum(resource.openStream());
				long destinationSum = checkSum(new FileInputStream(destinationFile()));

				return sourceSum != destinationSum;
			}
			else
				return false;
		}
	}

	private long checkSum(InputStream input) throws IOException
	{
		long sum = 0;
		int b;
		while((b = input.read()) != -1)
			sum += b;
		input.close();

		return sum;
	}
}
