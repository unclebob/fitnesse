// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.authentication;

import fitnesse.util.FileUtil;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.*;

public class PasswordFile
{
	private File passwordFile;
	private Map passwordMap = new HashMap();
	private PasswordCipher cipher = new TransparentCipher();

	public PasswordFile(String filename) throws Exception
	{
		passwordFile = new File(filename);
		loadFile();
	}

	public PasswordFile(String filename, PasswordCipher cipher) throws Exception
	{
		this(filename);
		this.cipher = cipher;
	}

	public Map getPasswordMap()
	{
		return passwordMap;
	}

	public String getName()
	{
		return passwordFile.getName();
	}

	public PasswordCipher getCipher()
	{
		return cipher;
	}

	public void savePassword(String user, String password) throws Exception
	{
		passwordMap.put(user, cipher.encrypt(password));
		savePasswords();
	}

	private void loadFile() throws Exception
	{
		LinkedList lines = getPasswordFileLines();
		loadCipher(lines);
		loadPasswords(lines);
	}

	private void loadPasswords(LinkedList lines)
	{
		for(Iterator iterator = lines.iterator(); iterator.hasNext();)
		{
			String line = (String) iterator.next();
			if(!"".equals(line))
			{
				String[] tokens = line.split(":");
				passwordMap.put(tokens[0], tokens[1]);
			}
		}
	}

	private void loadCipher(LinkedList lines) throws Exception
	{
		if(lines.size() > 0)
		{
			String firstLine = lines.getFirst().toString();
			if(firstLine.startsWith("!"))
			{
				String cipherClassName = firstLine.substring(1);
				instantiateCipher(cipherClassName);
				lines.removeFirst();
			}
		}
	}

	public PasswordCipher instantiateCipher(String cipherClassName) throws Exception
	{
		Class cipherClass = Class.forName(cipherClassName);
		Constructor constructor = cipherClass.getConstructor(new Class[]{});
		cipher = (PasswordCipher) constructor.newInstance(new Object[]{});
		return cipher;
	}

	private void savePasswords() throws Exception
	{
		List lines = new LinkedList();
		lines.add("!" + cipher.getClass().getName());
		for(Iterator iterator = passwordMap.keySet().iterator(); iterator.hasNext();)
		{
			Object user = iterator.next();
			Object password = passwordMap.get(user);
			lines.add(user + ":" + password);
		}
		FileUtil.writeLinesToFile(passwordFile, lines);
	}

	private LinkedList getPasswordFileLines() throws Exception
	{
		LinkedList lines = new LinkedList();
		if(passwordFile.exists())
			lines = FileUtil.getFileLines(passwordFile);
		return lines;
	}
}
