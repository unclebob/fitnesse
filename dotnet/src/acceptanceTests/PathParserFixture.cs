// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using fit;
using fitnesse.fitserver;

namespace fitnesse.acceptanceTests
{
	public class PathParserFixture : ColumnFixture
	{
		public string PathString;
		public string[] AssemblyPaths;
		public string ConfigFilePath;

		public override void Execute()
		{
			PathParser parser = new PathParser(PathString);
			AssemblyPaths = new string[parser.AssemblyPaths.Count];
			int index = 0;
			foreach(string assemblyPath in parser.AssemblyPaths)
				AssemblyPaths[index++] = assemblyPath;
			ConfigFilePath = parser.ConfigFilePath;
		}

	}
}
