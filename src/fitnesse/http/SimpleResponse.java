// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.http;

import java.nio.ByteBuffer;

public class SimpleResponse extends Response
{
	private byte[] content = new byte[0];

	public SimpleResponse()
	{
	}

	public SimpleResponse(int status)
	{
		super(status);
	}

	public void readyToSend(ResponseSender sender) throws Exception
	{
		byte[] bytes = getBytes();
		sender.send(bytes);
		sender.close();
	}

	public void setContent(String value) throws Exception
	{
		content = getEncodedBytes(value);
	}

	public void setContent(byte[] value)
	{
		content = value;
	}

	public String getContent()
	{
		return new String(content);
	}

	public byte[] getContentBytes()
	{
		return content;
	}

	public String getText()
	{
		return new String(getBytes());
	}

	public byte[] getBytes()
	{
		addStandardHeaders();
		byte[] headerBytes = makeHttpHeaders().getBytes();
		ByteBuffer bytes = ByteBuffer.allocate(headerBytes.length + getContentSize());
		bytes.put(headerBytes).put(content);
		return bytes.array();
	}

	public int getContentSize()
	{
		return content.length;
	}

	protected void addSpecificHeaders()
	{
		addHeader("Content-Length", String.valueOf(getContentSize()));
	}
}