package fitnesse.http;

import java.io.*;
import fitnesse.util.StreamReader;

public class InputStreamResponse extends Response
{
	private StreamReader reader;
	private int contentSize = 0;

	public void readyToSend(ResponseSender sender) throws Exception
	{
		addStandardHeaders();
		sender.send(makeHttpHeaders().getBytes());
		while(!reader.isEof())
			sender.send(reader.readBytes(1000));
		reader.close();
		sender.close();
	}

	protected void addSpecificHeaders()
	{
		addHeader("Content-Length", getContentSize() + "");
	}

	public int getContentSize()
	{
		return contentSize;
	}

	public void setBody(InputStream input, int size)
	{
		reader = new StreamReader(input);
		contentSize = size;
	}

	public void setBody(File file) throws Exception
	{
		FileInputStream input = new FileInputStream(file);
		int size = (int)file.length();
		setBody(input, size);
	}
}
