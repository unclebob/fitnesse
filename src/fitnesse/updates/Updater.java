// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.updates;

import fitnesse.FitNesseContext;
import fitnesse.wiki.WikiPage;

import java.io.*;
import java.util.Properties;

public class Updater
{
	public static boolean testing = false;

	public FitNesseContext context;
	public Properties rootProperties;

	public Update[] updates;

	public Updater(FitNesseContext context) throws Exception
	{
		this.context = context;
		rootProperties = loadProperties();

		updates = new Update[]{
			new ReplacingFileUpdate(this, "files/images/FitNesseLogo.gif", "files/images"),
			new ReplacingFileUpdate(this, "files/images/FitNesseLogoMedium.jpg", "files/images"),
			new ReplacingFileUpdate(this, "files/images/virtualPage.jpg", "files/images"),
			new ReplacingFileUpdate(this, "files/images/importedPage.jpg", "files/images"),
			new ReplacingFileUpdate(this, "files/images/collapsableOpen.gif", "files/images"),
			new ReplacingFileUpdate(this, "files/images/collapsableClosed.gif", "files/images"),
			new ReplacingFileUpdate(this, "files/images/folder.gif", "files/images"),
			new ReplacingFileUpdate(this, "files/images/executionStatus/ok.gif", "files/images/executionStatus"),
			new ReplacingFileUpdate(this, "files/images/executionStatus/output.gif", "files/images/executionStatus"),
			new ReplacingFileUpdate(this, "files/images/executionStatus/error.gif", "files/images/executionStatus"),
			new ReplacingFileUpdate(this, "files/css/fitnesse_base.css", "files/css"),
			new FileUpdate(this, "files/css/fitnesse.css", "files/css"),
			new FileUpdate(this, "files/css/fitnesse_print.css", "files/css"),
			new ReplacingFileUpdate(this, "files/javascript/fitnesse.js", "files/javascript"),
			new ReplacingFileUpdate(this, "files/javascript/clientSideSort.js", "files/javascript"),
			new ReplacingFileUpdate(this, "files/javascript/SpreadsheetTranslator.js", "files/javascript"),
			new ReplacingFileUpdate(this, "files/javascript/spreadsheetSupport.js", "files/javascript"),
			new PropertiesToXmlUpdate(this),
			new AttributeAdderUpdate(this, "RecentChanges"),
			new AttributeAdderUpdate(this, "WhereUsed"),
			new AttributeAdderUpdate(this, "Files"),
			new SymLinkPropertyFormatUpdate(this),
			new WikiImportPropertyFormatUpdate(this),
			new VirtualWikiDeprecationUpdate(this),
			new FrontPageUpdate(this)
		};
	}

	public void update() throws Exception
	{
		Update[] updates = getUpdates();
		for(int i = 0; i < updates.length; i++)
		{
			Update update = updates[i];
			if(update.shouldBeApplied())
				performUpdate(update);
		}
		saveProperties();
	}

	private void performUpdate(Update update) throws Exception
	{
		try
		{
			print(update.getMessage());
			update.doUpdate();
			print("...done\n");
		}
		catch(Exception e)
		{
			print("\n\t" + e + "\n");
		}
	}

	private Update[] getUpdates() throws Exception
	{
		return updates;
	}

	public WikiPage getRoot()
	{
		return context.root;
	}

	public Properties getProperties()
	{
		return rootProperties;
	}

	public Properties loadProperties() throws Exception
	{
		Properties properties = new Properties();
		File propFile = getPropertiesFile();
		if(propFile.exists())
		{
			InputStream is = new FileInputStream(propFile);
			properties.load(is);
			is.close();
		}
		return properties;
	}

	private File getPropertiesFile() throws Exception
	{
		String filename = context.rootPagePath + "/properties";
		return new File(filename);
	}

	public void saveProperties() throws Exception
	{
		File propFile = getPropertiesFile();
		OutputStream os = new FileOutputStream(propFile);
		rootProperties.store(os, "FitNesse properties");
		os.close();
	}

	private void print(String message)
	{
		if(!testing)
			System.out.print(message);

	}

}
