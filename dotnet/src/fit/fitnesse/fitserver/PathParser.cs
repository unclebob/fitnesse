// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Collections;

namespace fitnesse.fitserver
{
	public class PathParser
	{
		private ArrayList assemblyPaths = new ArrayList();
		private string configFilePath;

		public PathParser(string pathNames)
		{
			if (pathNames != null && pathNames.Length > 0)
				foreach (string pathName in pathNames.Split(';'))
				{
					if (pathName.EndsWith("config"))
					{
						if (configFilePath == null)
							configFilePath = pathName;
						else
							throw new ArgumentException("Please check the path. There should only be one config file on the path and there are at least two.");
					}
					else
						assemblyPaths.Add(pathName);
				}
		}

		public IList AssemblyPaths
		{
			get { return assemblyPaths; }
		}

		public bool HasConfigFilePath()
		{
			return configFilePath != null;
		}

		public string ConfigFilePath
		{
			get { return configFilePath; }
		}
	}
}
