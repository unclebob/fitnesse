// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.

package fitnesse.components;

import java.io.*;
import java.text.*;
import java.util.*;

public class Logger
{
	private File directory;

	public static SimpleDateFormat makeLogFormat()
	{
		//SimpleDateFormat is not thread safe, so we need to create each instance independently.
		return new SimpleDateFormat("dd/MMM/yyyy:HH:mm:ss Z");
	}

	public static SimpleDateFormat makeFileNameFormat()
	{
		//SimpleDateFormat is not thread safe, so we need to create each instance independently.
		return new SimpleDateFormat("yyyyMMddHHmmss");
	}

	private PrintWriter writer;
	private GregorianCalendar currentFileCreationDate;

	public Logger(String dirPath)
	{
		directory = new File(dirPath);
		directory.mkdir();
	}

	public File getDirectory()
	{
		return directory;
	}

	String formatLogLine(LogData data)
	{
		StringBuffer line = new StringBuffer();
		line.append(data.host).append(" - ");
		line.append(data.username == null ? "-" : data.username);
		line.append(" [").append(format(makeLogFormat(), data.time)).append("] ");
		line.append('"').append(data.requestLine).append("\" ");
		line.append(data.status).append(" ");
		line.append(data.size);
		return line.toString();
	}

	static String makeLogFileName(Calendar calendar)
	{
		StringBuffer name = new StringBuffer();
		name.append("fitnesse").append(format(makeFileNameFormat(), calendar)).append(".log");
		return name.toString();
	}

	public void log(LogData data) throws Exception
	{
		if(needNewFile(data.time))
			openNewFile(data);
		writer.println(formatLogLine(data));
		writer.flush();
	}

	private boolean needNewFile(GregorianCalendar time)
	{
		if(writer == null)
			return true;
		else
		{
			boolean different = (time.get(Calendar.DAY_OF_YEAR) != currentFileCreationDate.get(Calendar.DAY_OF_YEAR))
				|| (time.get(Calendar.YEAR) != currentFileCreationDate.get(Calendar.YEAR));
			return different;
		}

	}

	private void openNewFile(LogData data) throws FileNotFoundException
	{
		if(writer != null)
			writer.close();
		currentFileCreationDate = data.time;
		String filename = makeLogFileName(data.time);
		File file = new File(directory, filename);
		FileOutputStream outputStream = new FileOutputStream(file);
		writer = new PrintWriter(outputStream);
	}

	public void close()
	{
		if(writer != null)
			writer.close();
	}

	private static String format(DateFormat format, Calendar calendar)
	{
		DateFormat tmpFormat = (DateFormat) format.clone();
		tmpFormat.setTimeZone(calendar.getTimeZone());
		return tmpFormat.format(calendar.getTime());
	}

	public String toString()
	{
		return getDirectory().getAbsolutePath();
	}
}
