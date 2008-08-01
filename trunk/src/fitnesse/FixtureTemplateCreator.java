// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

import fit.*;

import java.lang.reflect.*;
import java.util.*;

// This command-line tool takes in a fit.Fixture class name and prints out a FitNesse table template.
public class FixtureTemplateCreator
{
	public static void main(String[] args) throws Exception
	{
		if(args.length < 1)
			return;

		new FixtureTemplateCreator().run(args[0]);
	}

	public void run(String fixtureName) throws Exception
	{
		String defaultTableTemplate = "!|" + fixtureName + "|";

		try
		{
			Class fixtureClass = ClassLoader.getSystemClassLoader().loadClass(fixtureName);
			Object fixtureInstance = fixtureClass.newInstance();

			if(!Fixture.class.isInstance(fixtureInstance))
				throw new InstantiationException();
			else if(RowFixture.class.isInstance(fixtureInstance))
				System.out.println(makeRowFixtureTemplate(defaultTableTemplate, fixtureClass));
			else if(ColumnFixture.class.isInstance(fixtureInstance))
				System.out.println(makeColumnFixtureTemplate(defaultTableTemplate, fixtureClass));
			else
				System.out.println(defaultTableTemplate);
		}
		catch(ClassNotFoundException e)
		{
			System.out.println("# Could not find " + fixtureName + " in the classpath. #");
		}
		catch(InstantiationException ie)
		{
			System.out.println("# " + fixtureName + " is not a valid fixture! #");
		}
	}

	private StringBuffer makeRowFixtureTemplate(String defaultTableTemplate, Class fixtureClass)
	{
		Class targetClass = getTargetClassFromRowFixture(fixtureClass);
		return makeFixtureTemplate(defaultTableTemplate, targetClass, Object.class);
	}

	private StringBuffer makeColumnFixtureTemplate(String defaultTableTemplate, Class fixtureClass)
	{
		return makeFixtureTemplate(defaultTableTemplate, fixtureClass, ColumnFixture.class);
	}

	private StringBuffer makeFixtureTemplate(String defaultTableTemplate, Class fixtureClass, Class stopClass)
	{
		StringBuffer tableTemplate = new StringBuffer(defaultTableTemplate + "\n");
		List publicFieldsFound = new ArrayList();
		List publicMethodsFound = new ArrayList();
		getPublicMembers(fixtureClass, publicFieldsFound, publicMethodsFound, stopClass);
		addCellsForColumnFixture(tableTemplate, publicFieldsFound, publicMethodsFound);

		return tableTemplate;
	}

	private void getPublicMembers(Class aClass, List publicFields, List publicMethods, Class stopClass)
	{
		Class currentClass = aClass;
		while(currentClass != stopClass)
		{
			Field[] fields = currentClass.getDeclaredFields();
			for(int i = 0; i < fields.length; i++)
			{
				Field field = fields[i];
				if(Modifier.isPublic(field.getModifiers()))
					publicFields.add(field);
			}

			Method[] methods = currentClass.getDeclaredMethods();
			for(int i = 0; i < methods.length; i++)
			{
				Method method = methods[i];
				String methodName = method.getName();
				if("reset".equals(methodName) || "execute".equals(methodName))
					continue;
				if(Modifier.isAbstract(method.getModifiers()))
					continue;
				if(Modifier.isPublic(method.getModifiers()))
					publicMethods.add(method);
			}
			currentClass = currentClass.getSuperclass();
		}
	}

	private void addCellsForColumnFixture(StringBuffer tableTemplate, List publicFields, List publicMethods)
	{
		StringBuffer headerRow = new StringBuffer("|");
		StringBuffer valueRow = new StringBuffer("|");

		addCellsForFieldNamesAndTypes(publicFields, headerRow, valueRow);
		addCellsForMethodNamesAndReturnTypes(publicMethods, headerRow, valueRow);

		tableTemplate.append(headerRow).append("\n").append(valueRow).append("\n");
	}

	private void addCellsForFieldNamesAndTypes(List publicFields, StringBuffer headerRow, StringBuffer valueRow)
	{
		for(Iterator f = publicFields.iterator(); f.hasNext();)
		{
			Field field = (Field) f.next();
			String name = field.getName();
			String type = getShortClassName(field.getType().getName());

			String pad = createSpaces(Math.abs(name.length() - type.length()));
			if(name.length() < type.length())
				name += pad;
			else if(type.length() < name.length())
				type += pad;

			headerRow.append(name).append("|");
			valueRow.append(type).append("|");
		}
	}

	private void addCellsForMethodNamesAndReturnTypes(List publicMethods, StringBuffer headerRow, StringBuffer valueRow)
	{
		for(Iterator m = publicMethods.iterator(); m.hasNext();)
		{
			Method method = (Method) m.next();
			String name = method.getName() + "()";
			String type = getShortClassName(method.getReturnType().getName());
			type = fixClassName(type);

			String pad = createSpaces(Math.abs(name.length() - type.length()));
			if(name.length() < type.length())
				name += pad;
			else if(type.length() < name.length())
				type += pad;

			headerRow.append(name).append("|");
			valueRow.append(type).append("|");
		}
	}

	private String createSpaces(int numSpaces)
	{
		StringBuffer spaces = new StringBuffer("");
		for(int j = 0; j < numSpaces; j++)
			spaces.append(" ");
		return spaces.toString();
	}

	protected String getShortClassName(String fullyQualifiedClassName)
	{
		String[] parts = fullyQualifiedClassName.split("\\.");
		return parts[parts.length - 1];
	}

	protected String fixClassName(String className)
	{
		if(className.endsWith(";"))
			className = className.substring(0, className.length() - 1) + "[]";
		return className;
	}

	protected Class getTargetClassFromRowFixture(Class rowFixtureClass)
	{
		Class targetClass = null;

		try
		{
			Method method_getTargetClass = rowFixtureClass.getMethod("getTargetClass");
			targetClass = (Class) method_getTargetClass.invoke(rowFixtureClass.newInstance());
		}
		catch(NoSuchMethodException nsme)
		{
			return null;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
		return targetClass;
	}
}
