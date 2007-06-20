// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

public class Base64
{
	private static final byte[] base64Alphabet =
		"ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".getBytes();
	private static final byte pad = '=';

	public static String decode(String value) throws Exception
	{
		return new String(decode(value.getBytes("UTF-8")));
	}

	public static byte[] decode(byte[] bytes)
	{
		int lengthOfDecoding = getLengthOfDecoding(bytes);
		byte[] decoding = new byte[lengthOfDecoding];
		int decodingIndex = 0;

		for(int index = 0; index < bytes.length; index += 4)
		{
			byte v1 = getValueFor(bytes[index]);
			byte v2 = getValueFor(bytes[index + 1]);
			byte v3 = getValueFor(bytes[index + 2]);
			byte v4 = getValueFor(bytes[index + 3]);

			byte c1 = (byte) ((v1 << 2) + (v2 >> 4));
			byte c2 = (byte) ((v2 << 4) + (v3 >> 2));
			byte c3 = (byte) ((v3 << 6) + v4);

			decoding[decodingIndex++] = c1;
			if(c2 != 0)
				decoding[decodingIndex++] = c2;
			if(c3 != 0)
				decoding[decodingIndex++] = c3;
		}
		return decoding;
	}

	public static String encode(String value) throws Exception
	{
		return new String(encode(value.getBytes()));
	}

	public static byte[] encode(byte[] bytes)
	{
		int inputLength = bytes.length;

		int lengthOfEncoding = getLengthOfEncoding(bytes);
		byte[] encoding = new byte[lengthOfEncoding];
		int encodingIndex = 0;

		int index = 0;
		while(index < inputLength)
		{
			byte c1 = bytes[index++];
			byte c2 = index >= inputLength ? 0 : bytes[index++];
			byte c3 = index >= inputLength ? 0 : bytes[index++];

			byte v1 = abs((byte) (c1 >> 2));
			byte v2 = abs((byte) (((c1 << 4) & 63) + (c2 >> 4)));
			byte v3 = abs((byte) (((c2 << 2) & 63) + (c3 >> 6)));
			byte v4 = abs((byte) (c3 & 63));

			encoding[encodingIndex++] = base64Alphabet[v1];
			encoding[encodingIndex++] = base64Alphabet[v2];
			if(v3 != 0)
				encoding[encodingIndex++] = base64Alphabet[v3];
			else
				encoding[encodingIndex++] = '=';
			if(v4 != 0)
				encoding[encodingIndex++] = base64Alphabet[v4];
			else
				encoding[encodingIndex++] = '=';
		}
		return encoding;
	}

	private static int getLengthOfDecoding(byte[] bytes)
	{
		if(bytes.length == 0)
			return 0;

		int lengthOfOutput = (int) (bytes.length * .75);

		for(int i = bytes.length - 1; bytes[i] == pad; i--)
			lengthOfOutput--;

		return lengthOfOutput;
	}

	private static int getLengthOfEncoding(byte[] bytes)
	{
		boolean needsPadding = (bytes.length % 3 != 0);

		int length = ((int) (bytes.length / 3)) * 4;
		if(needsPadding)
			length += 4;

		return length;
	}

	public static byte getValueFor(byte b)
	{
		if(b == pad)
			return (byte) 0;
		for(int i = 0; i < base64Alphabet.length; i++)
		{
			if(base64Alphabet[i] == b)
				return (byte) i;
		}
		return -1;
	}

	private static byte abs(byte b)
	{
		return (byte) Math.abs(b);
	}
}