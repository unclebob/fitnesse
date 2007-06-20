// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import fit.Counts;
import fitnesse.util.StreamReader;

import java.io.*;
import java.text.DecimalFormat;

public class FitProtocol
{
	public static final DecimalFormat format = new DecimalFormat("0000000000");

	public static void writeData(String data, OutputStream output) throws Exception
	{
		byte[] bytes = data.getBytes("UTF-8");
		writeData(bytes, output);
	}

	public static void writeData(byte[] bytes, OutputStream output) throws IOException
	{
		int length = bytes.length;
		writeSize(length, output);
		output.write(bytes);
		output.flush();
	}

	public static void writeSize(int length, OutputStream output) throws IOException
	{
		String formattedLength = format.format(length);
		byte[] lengthBytes = formattedLength.getBytes();
		output.write(lengthBytes);
		output.flush();
	}

	public static void writeCounts(Counts count, OutputStream output) throws IOException
	{
		writeSize(0, output);
		writeSize(count.right, output);
		writeSize(count.wrong, output);
		writeSize(count.ignores, output);
		writeSize(count.exceptions, output);
	}

	public static int readSize(StreamReader reader) throws Exception
	{
		String sizeString = reader.read(10);
		if(sizeString.length() < 10)
			throw new Exception("A size value could not be read. Fragment=|" + sizeString + "|");
		else
			return Integer.valueOf(sizeString).intValue();
	}

	public static String readDocument(StreamReader reader, int size) throws Exception
	{
		return reader.read(size);
	}

	public static Counts readCounts(StreamReader reader) throws Exception
	{
		Counts counts = new Counts();
		counts.right = readSize(reader);
		counts.wrong = readSize(reader);
		counts.ignores = readSize(reader);
		counts.exceptions = readSize(reader);
		return counts;
	}
}
