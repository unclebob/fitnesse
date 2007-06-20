// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.Fixture;

import java.io.File;

public class FileSystemPageTearDown extends Fixture
{
	public FileSystemPageTearDown() throws Exception
	{
		fitnesse.util.FileUtil.deleteFileSystemDirectory(new File(FitnesseFixtureContext.baseDir));
		FitnesseFixtureContext.root = null;

	}
}
