// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Collections;
using NUnit.Framework;

namespace fitnesse.fitserver
{
	[TestFixture]
	public class PathParserTest
	{
		[Test]
		public void TestEmptyString()
		{
			PathParser parser = new PathParser("");
			Assert.AreEqual(0, parser.AssemblyPaths.Count);
			Assert.IsFalse(parser.HasConfigFilePath());
			Assert.IsNull(parser.ConfigFilePath);
		}

		[Test]
		public void TestNull()
		{
			PathParser parser = new PathParser(null);
			verifyAssemblyPaths(new string[]{}, parser.AssemblyPaths);
			Assert.IsFalse(parser.HasConfigFilePath());
			Assert.IsNull(parser.ConfigFilePath);
		}

		[Test]
		public void TestOneAssembly()
		{
			string path = "d:\\path\\to\\assembly.dll";
			PathParser parser = new PathParser(path);
			verifyAssemblyPaths(new string[]{path}, parser.AssemblyPaths);
			Assert.IsFalse(parser.HasConfigFilePath());
			Assert.IsNull(parser.ConfigFilePath);
		}

		[Test]
		public void TestTwoAssemblies()
		{
			string path1 = "d:\\path\\to\\assembly.dll";
			string path2 = "d:\\path\\to\\another\\assembly.dll";
			string paths = path1 + ";" + path2;
			PathParser parser = new PathParser(paths);
			verifyAssemblyPaths(new string[]{path1, path2}, parser.AssemblyPaths);
			Assert.IsFalse(parser.HasConfigFilePath());
			Assert.IsNull(parser.ConfigFilePath);
		}

		[Test]
		public void TestOneConfigFile()
		{
			string path = "d:\\path\\to\\assembly.dll.config";
			PathParser parser = new PathParser(path);
			verifyAssemblyPaths(new string[]{}, parser.AssemblyPaths);
			Assert.IsTrue(parser.HasConfigFilePath());
			Assert.AreEqual(path, parser.ConfigFilePath);
		}

		[Test]
		[ExpectedException(typeof(ArgumentException), "Please check the path. There should only be one config file on the path and there are at least two.")]
		public void TestExceptionOnSecondConfigFile()
		{
			string path1 = "d:\\path\\to\\assembly.dll.config";
			string path2 = "d:\\path\\to\\another\\assembly.dll.config";
			string paths = path1 + ";" + path2;
			PathParser parser = new PathParser(paths);
		}

		public void TestOneConfigFileAndOneAssemblyFile()
		{
			string assemblyPath = "d:\\path\\to\\assembly.dll";
			string configFilePath = "d:\\path\\to\\assembly.dll.config";
			string paths = assemblyPath + ";" + configFilePath;
			PathParser parser = new PathParser(paths);
			verifyAssemblyPaths(new string[]{assemblyPath}, parser.AssemblyPaths);
			Assert.IsTrue(parser.HasConfigFilePath());
			Assert.AreEqual(configFilePath, parser.ConfigFilePath);
		}

		private void verifyAssemblyPaths(string[] expected, IList list2)
		{
			IList list1 = new ArrayList(expected);
			Assert.AreEqual(list1.Count, list2.Count);
			foreach (object obj in list1)
				Assert.IsTrue(list2.Contains(obj));
		}


	}
}
