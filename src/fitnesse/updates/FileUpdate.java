// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.updates;

import java.io.*;
import java.net.URL;

public class FileUpdate implements Update
{
	private static final String slash = "/";

	protected String destination;
	protected String source;
	protected File destinationDir;
	protected String rootDir;
	protected String filename;

	public FileUpdate(Updater updater, String source, String destination) throws Exception
	{
		this.destination = destination;
		this.source = source;
		rootDir = updater.context.rootPagePath;
		destinationDir = new File(new File(rootDir), destination);

		filename = new File(source).getName();
	}

	public void doUpdate() throws Exception
	{
		makeSureDirectoriesExist();
		copyResource();
	}

	private void makeSureDirectoriesExist()
	{
		String[] subDirectories = destination.split(slash);
		String currentDirPath = rootDir;

		for(int i = 0; i < subDirectories.length; i++)
		{
			String subDirectory = subDirectories[i];
			currentDirPath = currentDirPath + slash + subDirectory;
			File directory = new File(currentDirPath);
			directory.mkdir();
		}
	}

	private void copyResource() throws Exception
	{
		URL url = getResource(source);
		if(url != null)
		{
			InputStream input = url.openStream();
			OutputStream output = new FileOutputStream(destinationFile());

			int b;
			while((b = input.read()) != -1)
				output.write(b);

			input.close();
			output.close();
		}
		else
			throw new Exception("Could not load resource: " + source);
	}

	protected URL getResource(String resource)
	{
		return ClassLoader.getSystemResource(resource);
	}

	public String getMessage()
	{
		return "Installing file: " + destinationFile();
	}

	protected File destinationFile()
	{
		return new File(destinationDir, filename);
	}

	public String getName()
	{
		return "FileUpdate(" + filename + ")";
	}

	public boolean shouldBeApplied() throws Exception
	{
		return !destinationFile().exists();
	}
}
