// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Collections;
using System.Configuration;
using System.IO;
using System.Reflection;
using System.Text;

namespace fit
{
	public class ObjectFactory
	{
		private string possibleSuffix;
		private static IList assemblyList = new ArrayList();
		private static IList loadedAssemblies = new ArrayList();
		private static IList namespaces = new ArrayList();
		private GracefulNameConverter converter = new GracefulNameConverter();

		public ObjectFactory() : this(null)
		{}

		public ObjectFactory(string possibleSuffix)
		{
			this.possibleSuffix = possibleSuffix;
		}

		public static IList AssemblyList
		{
			get { return assemblyList; }
		}

		public static IList LoadedAssemblies
		{
			get { return loadedAssemblies; }
		}

		public static IList Namespaces
		{
			get { return namespaces; }
			set { namespaces = value; }
		}

		public static void AddAssembly(string assemblyPath)
		{
			if (assemblyPath != null && !assemblyList.Contains(assemblyPath))
			{
				try
				{
					Assembly assembly = Assembly.LoadFrom(assemblyPath);
					loadedAssemblies.Add(assembly);
					assemblyList.Add(assemblyPath);
					FileInfo fileInfo = new FileInfo(assemblyPath);
					DirectoryInfo directoryInfo = fileInfo.Directory;
					foreach (FileInfo configFileInfo in directoryInfo.GetFiles("*.config"))
					{
						AppDomain.CurrentDomain.SetData("APP_CONFIG_FILE",configFileInfo);
					}
				}
					//TODO - explain why we need to catch exceptions but don't
					//need to do anything with them
				catch (Exception)
				{}
			}
		}

		public static void AddNamespace(string name)
		{
			namespaces.Add(name);
		}

		public static void ClearNamespaces()
		{
			namespaces.Clear();
		}

		public bool IsTypeAvailable(string submittedName)
		{
			return GetTypeOrInstance(GetTypeName(submittedName), new GetTypeOrInstanceDelegate(GetType)) != null;
		}

		public object CreateInstance(string submittedName)
		{
			return GetTypeOrInstance(GetTypeName(submittedName), new GetTypeOrInstanceDelegate(GetInstance));
		}

		private delegate object GetTypeOrInstanceDelegate(TypeName typeName, Assembly assembly, Type type);

		private object GetInstance(TypeName typeName, Assembly assembly, Type type)
		{
			try
			{
				return assembly.CreateInstance(type.FullName);
			}
			catch(NullReferenceException)
			{
				throw new ApplicationException(GetCouldNotFindTypeMessage(typeName.OriginalName));
			}
		}

		private object GetType(TypeName typeName, Assembly assembly, Type type)
		{
			return type;
		}

		private object GetTypeOrInstance(TypeName typeName, GetTypeOrInstanceDelegate getTypeOrInstance)
		{
			foreach (Assembly assembly in loadedAssemblies)
			{
				foreach (Type type in assembly.GetExportedTypes())
				{
					if (type.Name.ToLower() == typeName.Name.ToLower() || type.Name.ToLower() == (typeName.Name + possibleSuffix).ToLower())
					{
						if (typeName.IsFullyQualified())
						{
							if (typeName.Namespace.Equals(type.Namespace))
							{
								return getTypeOrInstance(typeName, assembly, type);
							}
						}
						else if (type.Namespace == null || namespaces.Contains(type.Namespace))
						{
							return getTypeOrInstance(typeName, assembly, type);
						}
					}
				}
			}
			return getTypeOrInstance(typeName, null, null);
		}

		private TypeName GetTypeName(string submittedName)
		{
			TypeName typeName = new TypeName(submittedName);
			if (!typeName.IsFullyQualified())
			{
				typeName = new TypeName(converter.GetConvertedName(submittedName));
			}
			return typeName;
		}

		private string GetCouldNotFindTypeMessage(string submittedName)
		{
			StringBuilder builder = new StringBuilder();
			builder.Append("Type '");
			builder.Append(submittedName);
			builder.Append("' could not be found in assemblies.\n");
			builder.Append("Assemblies searched:");
			foreach (Assembly assembly in loadedAssemblies)
			{
				builder.Append("    ");
				builder.Append(assembly.CodeBase);
				builder.Append("\n");
			}
			return builder.ToString();
		}
	}
}