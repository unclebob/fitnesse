// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wiki;

import fitnesse.util.*;
import fitnesse.wikitext.widgets.WikiWordWidget;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;
import java.util.zip.*;

public class FileSystemPage extends CachingPage
{
	public static final String contentFilename = "/content.txt";
	public static final String propertiesFilename = "/properties.xml";

	private String path;

	protected FileSystemPage(String path, String name, WikiPage parent) throws Exception
	{
		super(name, parent);
		this.path = path;
	}

	public static WikiPage makeRoot(String path, String name) throws Exception
	{
		return new FileSystemPage(path, name, null);
	}

	public void removeChildPage(String name) throws Exception
	{
		super.removeChildPage(name);
		File fileToBeDeleted = new File(getFileSystemPath() + "/" + name);
		FileUtil.deleteFileSystemDirectory(fileToBeDeleted);
	}

	public boolean hasChildPage(String pageName) throws Exception
	{
		File f = new File(getFileSystemPath() + "/" + pageName);
		if(f.exists())
		{
			addChildPage(pageName);
			return true;
		}
		else
			return false;
	}

	protected synchronized void saveContent(String content) throws Exception
	{
		if(content == null)
			return;

		String separator = System.getProperty("line.separator");

		if(content.endsWith("|"))
			content += separator;

		content = content.replaceAll("\r\n", separator);

		File output = new File(getFileSystemPath() + contentFilename);
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(new FileOutputStream(output), "UTF-8");
			writer.write(content);
		} finally {
			if (writer != null)
				writer.close();
		}
	}

	protected synchronized void saveAttributes(WikiPageProperties attributes) throws Exception
	{
		OutputStream output = null;
		String propertiesFileName  = "<unknown>";
		try {
			propertiesFileName = getFileSystemPath() + propertiesFilename;
			output = new FileOutputStream(propertiesFileName);
			attributes.save(output);
		} catch (Exception e) {
			System.err.println("Failed to save properties file: \""+propertiesFileName+"\" (exception: "+e+").");
			e.printStackTrace();
			throw e;
		} finally {
			if (output != null)
				output.close();
		}
	}

	protected WikiPage createChildPage(String name) throws Exception
	{
		FileSystemPage newPage = new FileSystemPage(getFileSystemPath(), name, this);
		new File(newPage.getFileSystemPath()).mkdirs();
		return newPage;
	}

	private void loadContent(PageData data) throws Exception
	{
		String content = "";

		String name = getFileSystemPath() + contentFilename;
		File input = new File(name);
		if(input.exists())
		{
			byte[] bytes = readContentBytes(input);
			content = new String(bytes, "UTF-8");
		}
		data.setContent(content);
	}

	private byte[] readContentBytes(File input) throws FileNotFoundException,
			IOException {
		FileInputStream inputStream = null;
		try {
			byte[] bytes = new byte[(int) input.length()];
			inputStream = new FileInputStream(input);
			inputStream.read(bytes);
			return bytes;
		} finally {
			if (inputStream != null) 
				inputStream.close();
		}
	}

	protected void loadChildren() throws Exception
	{
		File thisDir = new File(getFileSystemPath());
		if(thisDir.exists())
		{
			String[] subFiles = thisDir.list();
			for(int i = 0; i < subFiles.length; i++)
			{
				String subFile = subFiles[i];
				if(fileIsValid(subFile, thisDir) && !children.containsKey(subFile))
					children.put(subFile, getChildPage(subFile));
			}
		}
	}

	private boolean fileIsValid(String filename, File dir)
	{
		if(WikiWordWidget.isWikiWord(filename))
		{
			File f = new File(dir, filename);
			if(f.isDirectory())
				return true;
		}
		return false;
	}

	private String getParentFileSystemPath() throws Exception
	{
		return (parent != null) ? ((FileSystemPage) parent).getFileSystemPath() : path;
	}

	public String getFileSystemPath() throws Exception
	{
		return getParentFileSystemPath() + "/" + getName();
	}

	private void loadAttributes(PageData data) throws Exception
	{
		File file = new File(getFileSystemPath() + propertiesFilename);
		if(file.exists())
		{
			try
			{
				attemptToReadPropertiesFile(file, data);
			}
			catch(Exception e)
			{
				System.err.println("Could not read properties file:" + file.getPath());
				e.printStackTrace();
			}
		}
	}

	private void attemptToReadPropertiesFile(File file, PageData data) throws Exception
	{
		InputStream input = null;
		try {
			WikiPageProperties props = new WikiPageProperties();
			input = new FileInputStream(file);
			props.loadFromXmlStream(input);
			data.setProperties(props);
		} finally {
			if (input != null)
				input.close();
		}
	}

	public void doCommit(PageData data) throws Exception
	{
		data.getProperties().setLastModificationTime(new Date());
		saveContent(data.getContent());
		saveAttributes(data.getProperties());
		PageVersionPruner.pruneVersions(this, loadVersions());
	}

	protected PageData makePageData() throws Exception
	{
		PageData pagedata = new PageData(this);
		loadContent(pagedata);
		loadAttributes(pagedata);
		pagedata.addVersions(loadVersions());
		return pagedata;
	}

	public PageData getDataVersion(String versionName) throws Exception
	{
		String filename = getFileSystemPath() + "/" + versionName + ".zip";
		File file = new File(filename);
		if(!file.exists())
			throw new NoSuchVersionException("There is no version '" + versionName + "'");

		ZipFile zipFile = null;
		try {
			PageData data = new PageData(this);
			zipFile = new ZipFile(file);
			loadVersionContent(zipFile, data);
			loadVersionAttributes(zipFile, data);
			data.addVersions(loadVersions());
			return data;
		} finally {
			if (zipFile != null)
				zipFile.close();
		}
	}

	private Collection loadVersions() throws Exception
	{
		File dir = new File(getFileSystemPath());
		File[] files = dir.listFiles();
		Set<VersionInfo> versions = new HashSet<VersionInfo>();
		if(files != null)
		{
			for(int i = 0; i < files.length; i++)
			{
				File file = files[i];
				if(isVersionFile(file))
					versions.add(new VersionInfo(makeVersionName(file)));
			}
		}
		return versions;
	}

	protected VersionInfo makeVersion() throws Exception
	{
		PageData data = getData();
		return makeVersion(data);
	}

	protected VersionInfo makeVersion(PageData data) throws Exception
	{
		String dirPath = getFileSystemPath();
		Set filesToZip = getFilesToZip(dirPath);

		VersionInfo version = makeVersionInfo(data);

		if(filesToZip.size() == 0)
			return new VersionInfo("first_commit", "", new Date());
		ZipOutputStream zos = null; 
		try {
			String filename = makeVersionFileName(version.getName());
			zos = new ZipOutputStream(new FileOutputStream(filename));
	
			for(Iterator iterator = filesToZip.iterator(); iterator.hasNext();)
				addToZip((File) iterator.next(), zos);
			zos.finish();
			return new VersionInfo(version.getName());
		} finally {
			if (zos != null)
				zos.close();
		}
	}

	protected VersionInfo makeVersionInfo(PageData data) throws Exception
	{
		Date time = data.getProperties().getLastModificationTime();
		String versionName = VersionInfo.nextId() + "-" + dateFormat().format(time);
		String user = data.getAttribute(WikiPage.LAST_MODIFYING_USER);
		if(user != null && !"".equals(user))
			versionName = user + "-" + versionName;

		return new VersionInfo(versionName, user, time);
	}

	public static SimpleDateFormat dateFormat()
	{
		return new SimpleDateFormat("yyyyMMddHHmmss");
	}

	protected String makeVersionFileName(String name) throws Exception
	{
		return getFileSystemPath() + "/" + name + ".zip";
	}

	protected String makeVersionName(File file)
	{
		String name = file.getName();
		return name.substring(0, name.length() - 4);
	}

	protected boolean isVersionFile(File file)
	{
		return Pattern.matches("(\\S+)?\\d+\\.zip", file.getName());
	}

	protected void removeVersion(String versionName) throws Exception
	{
		String versionFileName = makeVersionFileName(versionName);
		File versionFile = new File(versionFileName);
		versionFile.delete();
	}

	protected Set getFilesToZip(String dirPath)
	{
		Set<File> filesToZip = new HashSet<File>();
		File dir = new File(dirPath);
		File[] files = dir.listFiles();
		if(files == null)
			return filesToZip;
		for(int i = 0; i < files.length; i++)
		{
			File file = files[i];
			if(!(isVersionFile(file) || file.isDirectory()))
				filesToZip.add(file);
		}
		return filesToZip;
	}

	private void addToZip(File file, ZipOutputStream zos) throws IOException
	{
		ZipEntry entry = new ZipEntry(file.getName());
		zos.putNextEntry(entry);
		FileInputStream is = null;
		try {
			is = new FileInputStream(file);
			int size = (int) file.length();
			byte[] bytes = new byte[size];
			is.read(bytes);
			zos.write(bytes, 0, size);
		} finally {
			if (is != null)
				is.close();
		}
	}

	protected void loadVersionContent(ZipFile zipFile, PageData data) throws Exception
	{
		String content = "";
		ZipEntry contentEntry = zipFile.getEntry("content.txt");
		if(contentEntry != null)
		{
			content = readContentEntry(zipFile, contentEntry);
		}
		data.setContent(content);
	}

	private String readContentEntry(ZipFile zipFile, ZipEntry contentEntry)
			throws IOException, Exception {
		String content;
		StreamReader reader = null;
		try {
			InputStream contentIS = zipFile.getInputStream(contentEntry);
			reader = new StreamReader(contentIS);
			content = reader.read((int) contentEntry.getSize());
			return content;
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	protected void loadVersionAttributes(ZipFile zipFile, PageData data) throws Exception
	{
		ZipEntry attributes = zipFile.getEntry("properties.xml");
		if(attributes != null)
		{
			InputStream attributeIS = null;
			try {
				attributeIS = zipFile.getInputStream(attributes);
				WikiPageProperties props = new WikiPageProperties(attributeIS);
				data.setProperties(props);
			} finally {
				if (attributeIS != null)
					attributeIS.close();
			}
		}
	}

	public String toString()
	{
		try
		{
			return getClass().getName() + " at " + this.getFileSystemPath();
		}
		catch(Exception e)
		{
			return super.toString();
		}
	}
}
