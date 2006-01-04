// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.

using System;
using System.IO;
using System.Text;

namespace fit 
{
	public class FileRunner 
	{
		public string input;
		public Parse tables;
		public Fixture fixture = new Fixture();
		public TextWriter output;

		public virtual void Run(String[] argv)
		{
			Args(argv);
			Process();
			Exit();
		}

		public virtual void Process()
		{
			try 
			{
				//tables = new Parse(input);
                tables = HtmlParser.Instance.Parse(input);
				fixture.DoTables(tables);
			} 
			catch (Exception e) 
			{
				Exception(e);
			}
			tables.Print(output);
		}

		public virtual void Args(String[] argv) 
		{
			if (argv.Length != 2 && argv.Length != 3) 
			{
				Console.Error.WriteLine("usage: runFile input-file output-file [assembly-list]");
				Console.Error.WriteLine("assembly-list: A semicolon separated list of assembly filenames");
				Console.Error.WriteLine("Example: runFile doc\\arithmetic.html report\\arithmetic.html bin\\math.dll");
				Environment.Exit(-1);
			}
			string inFile = argv[0];
			string outFile = argv[1];
			if (argv.Length > 2) 
			{
				foreach(string filename in argv[2].Split(';'))
					ObjectFactory.AddAssembly(filename);
			}
			fixture.Summary["input file"] = inFile;
			fixture.Summary["input update"] = File.GetLastWriteTime(inFile);
			fixture.Summary["output file"] = outFile;
			fixture.Summary["assemblies used"] = AssemblyList;
			try
			{
				input = Read(inFile);
				output = new StreamWriter(outFile);
			} 
			catch (IOException e)
			{
				Console.Error.WriteLine(e.Message);
				Environment.Exit(-1);
			}
		}

		private string AssemblyList 
		{
			get
			{
				StringBuilder builder = new StringBuilder();
				foreach(string assembly in ObjectFactory.AssemblyList)
					builder.Append(assembly).Append(";");

				return builder.ToString();
			}
		}

		protected virtual string Read(string input)
		{
			StreamReader reader = new StreamReader(input);
			try 
			{
				return reader.ReadToEnd();
			}
			finally 
			{
				reader.Close();
			}
		}

		protected virtual void Exception(Exception e)
		{
			tables = new Parse("body","Unable to parse input. Input ignored.", null, null);
			fixture.Exception(tables, e);
		}

		protected virtual void Exit()
		{
			output.Close();
			Console.Error.WriteLine(fixture.Counts.ToString());
			Environment.Exit(fixture.Counts.Wrong + fixture.Counts.Exceptions);
		}
	}
}
