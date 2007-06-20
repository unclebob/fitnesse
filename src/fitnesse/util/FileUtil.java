// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.util;

import java.io.*;
import java.util.*;

public class FileUtil
{
	public static final String ENDL = System.getProperty("line.separator");

	public static File createFile(String path, String content)
	{
		return createFile(new File(path), content);
	}

	public static File createFile(File file, String content)
	{
		try
		{
			FileOutputStream fileOutput = new FileOutputStream(file);
			fileOutput.write(content.getBytes());
			fileOutput.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		return file;
	}

	public static boolean makeDir(String path)
	{
		return new File(path).mkdir();
	}

	public static void deleteFileSystemDirectory(String dirPath)
	{
		deleteFileSystemDirectory(new File(dirPath));
	}

	public static void deleteFileSystemDirectory(File current)
	{
		File[] files = current.listFiles();

		for(int i = 0; files != null && i < files.length; i++)
		{
			File file = files[i];
			if(file.isDirectory())
				deleteFileSystemDirectory(file);
			else
				deleteFile(file);
		}
		deleteFile(current);
	}

	public static void deleteFile(String filename)
	{
		deleteFile(new File(filename));
	}

	public static void deleteFile(File file)
	{
		if(!file.exists())
			return;
		if(!file.delete())
			throw new RuntimeException("Could not delete '" + file.getAbsoluteFile() + "'");
		waitUntilFileDeleted(file);
	}

	private static void waitUntilFileDeleted(File file)
	{
		int i = 10;
		while(file.exists())
		{
			if(--i <= 0)
			{
				System.out.println("Breaking out of delete wait");
				break;
			}
			try
			{
				Thread.sleep(500);
			}
			catch(InterruptedException e)
			{
			}
		}
	}

	public static String getFileContent(String path) throws Exception
	{
		File input = new File(path);
		return getFileContent(input);
	}

	public static String getFileContent(File input) throws Exception
	{
		return new String(getFileBytes(input));
	}

	public static byte[] getFileBytes(File input) throws Exception
	{
		long size = input.length();
		FileInputStream stream = new FileInputStream(input);
		byte[] bytes = new StreamReader(stream).readBytes((int) size);
		stream.close();
		return bytes;
	}

	public static LinkedList getFileLines(String filename) throws Exception
	{
		return getFileLines(new File(filename));
	}

	public static LinkedList getFileLines(File file) throws Exception
	{
		LinkedList lines = new LinkedList();
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String line;
		while((line = reader.readLine()) != null)
			lines.add(line);

		reader.close();
		return lines;
	}

	public static void writeLinesToFile(String filename, List lines) throws Exception
	{
		writeLinesToFile(new File(filename), lines);
	}

	public static void writeLinesToFile(File file, List lines) throws Exception
	{
		PrintStream output = new PrintStream(new FileOutputStream(file));
		for(Iterator iterator = lines.iterator(); iterator.hasNext();)
		{
			String line = (String) iterator.next();
			output.println(line);
		}
		output.close();
	}

	public static void copyBytes(InputStream input, OutputStream output) throws Exception
	{
		StreamReader reader = new StreamReader(input);
		while(!reader.isEof())
			output.write(reader.readBytes(1000));
	}

	public static File createDir(String path)
	{
		makeDir(path);
		return new File(path);
	}

	public static File[] getDirectoryListing(File dir)
	{
		SortedSet dirSet = new TreeSet();
		SortedSet fileSet = new TreeSet();
		File[] files = dir.listFiles();
		for(int i = 0; i < files.length; i++)
		{
			if(files[i].isDirectory())
				dirSet.add(files[i]);
			else
				fileSet.add(files[i]);
		}
		List fileList = new LinkedList();
		fileList.addAll(dirSet);
		fileList.addAll(fileSet);
		return (File[]) fileList.toArray(new File[]{});
	}

	public static String buildPath(String[] parts)
	{
		return StringUtil.join(Arrays.asList(parts), System.getProperty("file.separator"));
	}
}
