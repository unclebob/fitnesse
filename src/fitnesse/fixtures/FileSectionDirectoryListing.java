// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fit.RowFixture;

import java.io.File;

public class FileSectionDirectoryListing extends RowFixture
{

	public Object[] query() throws Exception
	{
		File[] files = FileSection.getFileSection().listFiles();
		Object[] fileWrappers = new Object[files.length];
		for(int i = 0; i < files.length; i++)
		{
			fileWrappers[i] = new FileWrapper(files[i]);
		}
		return fileWrappers;
	}

	public Class getTargetClass()
	{
		return FileWrapper.class;
	}

	public class FileWrapper
	{
		private File file;

		public FileWrapper(File file)
		{
			this.file = file;
		}

		public String path()
		{
			int subStringLength = FileSection.getFileSection().getPath().length();
			return file.getPath().substring(subStringLength + 1);
		}
	}
}
