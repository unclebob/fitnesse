// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.*;
import fitnesse.util.FileUtil;

import java.io.File;

public class FileSection extends Fixture
{
	private static File fileSection;

	public static File getFileSection()
	{
		return fileSection;
	}

	public void doTable(Parse table)
	{
		try
		{
			String arg = getArgs()[0];
			if("setup".equals(arg.toLowerCase()))
			{
				new File(FitnesseFixtureContext.baseDir).mkdir();
				File dir = new File(FitnesseFixtureContext.baseDir + "/" + FitnesseFixtureContext.root.getName());
				dir.mkdir();
				fileSection = new File(dir, "files");
				fileSection.mkdir();
			}
			else
			{
				FileUtil.deleteFileSystemDirectory(FitnesseFixtureContext.baseDir);
				fileSection = null;
			}
		}
		catch(Exception e)
		{
			exception(table, e);
		}
	}
}
