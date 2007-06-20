// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.http;

import fitnesse.components.Base64;
import fitnesse.util.FileUtil;
import junit.framework.TestCase;

import java.io.*;

public class RequestTest extends TestCase
{
	PipedOutputStream output;
	Request request;
	public Thread parseThread;
	public Exception exception;

	public void setUp() throws Exception
	{
		output = new PipedOutputStream();
		request = new Request(new PipedInputStream(output));
	}

	public void tearDown() throws Exception
	{
		output.close();
	}

	private void writeToPipe(String value) throws Exception
	{
		byte[] bytes = value.getBytes();
		output.write(bytes);
	}

	public void testMultilevelRequest() throws Exception
	{
		startParsing();
		writeToPipe("GET /SomePage.SubPage?edit HTTP/1.1\r\n");
		writeToPipe("\r\n");
		finishParsing();
		assertEquals("SomePage.SubPage", request.getResource());
	}

	public void testSimpleRequest() throws Exception
	{
		assertFalse(request.hasBeenParsed());
		startParsing();
		writeToPipe("GET /request-uri HTTP/1.1\r\n");
		writeToPipe("\r\n");
		finishParsing();
		assertTrue(request.hasBeenParsed());
		assertEquals("/request-uri", request.getRequestUri());
	}

	public void testMalformedRequestLine() throws Exception
	{
		startParsing();
		writeToPipe("/resource HTTP/1.1\r\n");
		writeToPipe("\r\n");
		finishParsing();
		assertNotNull("no exception was thrown", exception);
		assertEquals("The request string is malformed and can not be parsed", exception.getMessage());
	}

	public void testBadMethod() throws Exception
	{
		startParsing();
		writeToPipe("DELETE /resource HTTP/1.1\r\n");
		writeToPipe("\r\n");
		finishParsing();
		assertNotNull("no exception was thrown", exception);
		assertEquals("The DELETE method is not currently supported", exception.getMessage());
	}

	public void testQueryStringValueWithNoQueryString() throws Exception
	{
		startParsing();
		writeToPipe("GET /resource HTTP/1.1\r\n");
		writeToPipe("\r\n");
		finishParsing();
		assertEquals("", request.getQueryString());
	}

	public void testParsingRequestUri() throws Exception
	{
		startParsing();
		writeToPipe("GET /resource?queryString HTTP/1.1\r\n");
		writeToPipe("\r\n");
		finishParsing();
		assertEquals("resource", request.getResource());
		assertEquals("queryString", request.getQueryString());
	}

	public void testCanGetQueryStringValues() throws Exception
	{
		startParsing();
		writeToPipe("GET /resource?key1=value1&key2=value2 HTTP/1.1\r\n");
		writeToPipe("\r\n");
		finishParsing();
		checkInputs();
	}

	public void testHeaders() throws Exception
	{
		startParsing();
		writeToPipe("GET /something HTTP/1.1\r\n");
		writeToPipe("Content-Length: 0\r\n");
		writeToPipe("Accept: text/html\r\n");
		writeToPipe("Connection: Keep-Alive\r\n");
		writeToPipe("\r\n");
		finishParsing();
		assertEquals(true, request.hasHeader("Content-Length"));
		assertEquals("0", request.getHeader("Content-Length"));
		assertEquals(true, request.hasHeader("Accept"));
		assertEquals("text/html", request.getHeader("Accept"));
		assertEquals(true, request.hasHeader("Connection"));
		assertEquals("Keep-Alive", request.getHeader("Connection"));
		assertEquals(false, request.hasHeader("Something-Else"));
		assertEquals(null, request.getHeader("Something-Else"));
	}

	public void testEntityBodyWithoutContentLength() throws Exception
	{
		startParsing();
		writeToPipe("GET /something HTTP/1.1\r\n");
		writeToPipe("\r\n");
		writeToPipe("This is the Entity Body");
		finishParsing();
		assertEquals("", request.getBody());
	}

	public void testEntityBodyIsRead() throws Exception
	{
		startParsing();
		writeToPipe("GET /something HTTP/1.1\r\n");
		writeToPipe("Content-Length: 23\r\n");
		writeToPipe("\r\n");
		writeToPipe("This is the Entity Body");
		finishParsing();
		assertEquals("This is the Entity Body", request.getBody());
	}

	public void testEntityBodyParametersAreParsed() throws Exception
	{
		startParsing();
		writeToPipe("GET /something HTTP/1.1\r\n");
		writeToPipe("Content-Length: 23\r\n");
		writeToPipe("\r\n");
		writeToPipe("key1=value1&key2=value2");
		finishParsing();
		checkInputs();
	}

	private void checkInputs()
	{
		assertEquals(true, request.hasInput("key1"));
		assertEquals("value1", request.getInput("key1"));
		assertEquals(true, request.hasInput("key2"));
		assertEquals("value2", request.getInput("key2"));
		assertEquals(false, request.hasInput("someOtherKey"));
		assertEquals(null, request.getInput("someOtherKey"));
	}

	public void testPostMethod() throws Exception
	{
		startParsing();
		writeToPipe("POST /something HTTP/1.1\r\n");
		writeToPipe("\r\n");
		finishParsing();
		assertNull("POST method should be allowed", exception);
	}

	public void testSimpleInputStyle() throws Exception
	{
		startParsing();
		writeToPipe("GET /abc?something HTTP/1.1\r\n");
		writeToPipe("\r\n");
		finishParsing();
		assertEquals(true, request.hasInput("something"));
	}

	public void testOperaPostRequest() throws Exception
	{
		startParsing();
		writeToPipe("POST /HelloThere HTTP/1.1\r\n");
		writeToPipe("User-Agent: Mozilla/4.0 (compatible; MSIE 6.0; MSIE 5.5; Windows NT 5.1) Opera 7.02  [en]\r\n");
		writeToPipe("Host: localhost:75\r\n");
		writeToPipe("Accept: text/html, image/png, image/jpeg, image/gif, image/x-xbitmap, */*;q=0.1\r\n");
		writeToPipe("Accept-Language: en\r\n");
		writeToPipe("Accept-Charset: windows-1252, utf-8, utf-16, iso-8859-1;q=0.6, *;q=0.1\r\n");
		writeToPipe("Accept-Encoding: deflate, gzip, x-gzip, identity, *;q=0\r\n");
		writeToPipe("Referer: http://localhost:75/HeloThere?edit=\r\n");
		writeToPipe("Connection: Keep-Alive, TE\r\n");
		writeToPipe("TE: deflate, gzip, chunked, identity, trailers\r\n");
		writeToPipe("Content-type: application/x-www-form-urlencoded\r\n");
		writeToPipe("Content-length: 67\r\n");
		writeToPipe("\r\n");
		writeToPipe("saveId=1046584670887&Edit=on&Search=on&Test=on&Suite=on&content=abc");

		finishParsing();

		assertTrue(request.hasInput("saveId"));
		assertTrue(request.hasInput("Edit"));
		assertTrue(request.hasInput("Search"));
		assertTrue(request.hasInput("Test"));
		assertTrue(request.hasInput("Suite"));
		assertTrue(request.hasInput("content"));
	}

	public void testBigPosts() throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < 10; i++)
		{
			for(int j = 0; j < 1000; j++)
				buffer.append(i);
		}

		startParsing();
		writeToPipe("POST /HelloThere HTTP/1.1\r\n");
		writeToPipe("Content-length: 10021\r\n");
		writeToPipe("\r\n");
		writeToPipe("saveId=12345&content=");
		writeToPipe(buffer.toString());

		finishParsing();
		assertEquals(buffer.toString(), request.getInput("content"));
	}

	public void testMultiPartForms() throws Exception
	{
		String content = "----bob\r\n" +
			"Content-Disposition: form-data; name=\"key1\"\r\n" +
			"\r\n" +
			"value1\r\n" +
			"----bob\r\n" +
			"Content-Disposition: form-data; name=\"key3\"\r\n" +
			"\r\n" +
			"some\r\nmulti-line\r\nvalue\r\n\r\n" +
			"----bob\r\n" +
			"Content-Disposition: form-data; name=\"key2\"\r\n" +
			"\r\n" +
			"value2\r\n" +
			"----bob\r\n" +
			"Content-Disposition: form-data; name=\"key4\"\r\n" +
			"\r\n" +
			"\r\n" +
			"----bob--\r\n";

		startParsing();
		writeToPipe("GET /request-uri HTTP/1.1\r\n");
		writeToPipe("Content-Length: " + content.length() + "\r\n");
		writeToPipe("Content-Type: multipart/form-data; boundary=--bob\r\n");
		writeToPipe("\r\n");
		writeToPipe(content);
		finishParsing();

		if(exception != null)
		{
			throw exception;
		}
		checkInputs();
		assertEquals(true, request.hasInput("key3"));
		assertEquals("some\r\nmulti-line\r\nvalue\r\n", request.getInput("key3"));

		assertEquals(true, request.hasInput("key4"));
		assertEquals("", request.getInput("key4"));
	}

	public void testUploadingFile() throws Exception
	{
		String content = "----bob\r\n" +
			"Content-Disposition: form-data; name=\"file1\"; filename=\"mike dile.txt\"\r\n" +
			"Content-Type: text/plain\r\n" +
			"\r\n" +
			"file contents\r\n" +
			"----bob--\r\n";

		startParsing();
		writeToPipe("GET /request-uri HTTP/1.1\r\n");
		writeToPipe("Content-Length: " + content.length() + "\r\n");
		writeToPipe("Content-Type: multipart/form-data; boundary=--bob\r\n");
		writeToPipe("\r\n");
		writeToPipe(content);
		finishParsing();

		testUploadedFile("file1", "mike dile.txt", "text/plain", "file contents");
	}

	public void testUploadingTwoFiles() throws Exception
	{
		String content = "-----------------------------7d32df3a80058\r\n" +
			"Content-Disposition: form-data; name=\"file\"; filename=\"C:\\test.txt\"\r\n" +
			"Content-Type: text/plain\r\n" +
			"\r\n" +
			"test\r\n" +
			"-----------------------------7d32df3a80058\r\n" +
			"Content-Disposition: form-data; name=\"file2\"; filename=\"C:\\test2.txt\"\r\n" +
			"Content-Type: text/plain\r\n" +
			"\r\n" +
			"test2\r\n" +
			"-----------------------------7d32df3a80058--\r\n";

		startParsing();
		writeToPipe("GET /request-uri HTTP/1.1\r\n");
		writeToPipe("Content-Length: " + content.length() + "\r\n");
		writeToPipe("Content-Type: multipart/form-data; boundary=---------------------------7d32df3a80058\r\n");
		writeToPipe("\r\n");
		writeToPipe(content);
		finishParsing();

		testUploadedFile("file", "C:\\test.txt", "text/plain", "test");
		testUploadedFile("file2", "C:\\test2.txt", "text/plain", "test2");
	}

	private void testUploadedFile(String name, String filename, String contentType, String content) throws Exception
	{
		assertEquals(true, request.hasInput(name));
		UploadedFile file = (UploadedFile) request.getInput(name);
		assertNotNull(file);
		assertEquals(filename, file.getName());
		assertEquals(contentType, file.getType());
		assertEquals(content, new String(FileUtil.getFileContent(file.getFile())));
	}

	public void testUploadingBinaryFile() throws Exception
	{
		startParsing();
		writeToPipe("GET /request-uri HTTP/1.1\r\n");
		writeToPipe("Content-Length: " + (83) + "\r\n");
		writeToPipe("Content-Type: multipart/form-data; boundary=--bob\r\n");
		writeToPipe("\r\n");

		writeToPipe("----bob\r\n");
		writeToPipe("Content-Disposition: name=\"n\"; filename=\"f\"\r\n");
		writeToPipe("\r\n");
		output.write(new byte[]{(byte) 0x9D, (byte) 0x90, (byte) 0x81});
		output.write("file contents".getBytes());
		writeToPipe("\r\n");

		writeToPipe("----bob--");
		finishParsing();

		UploadedFile file = (UploadedFile) request.getInput("n");
		assertNotNull(file);

		byte[] contents = FileUtil.getFileBytes(file.getFile());
		assertEquals((byte) 0x9D, contents[0]);
		assertEquals((byte) 0x90, contents[1]);
		assertEquals((byte) 0x81, contents[2]);
		assertEquals("file contents", new String(contents, 3, contents.length - 3));
	}

	public void testCanGetCredentials() throws Exception
	{
		startParsing();
		writeToPipe("GET /abc?something HTTP/1.1\r\n");
		writeToPipe("Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==\r\n");
		writeToPipe("\r\n");
		finishParsing();
		request.getCredentials();
		assertEquals("Aladdin", request.getAuthorizationUsername());
		assertEquals("open sesame", request.getAuthorizationPassword());
	}

	public void testDoenstChokeOnMissingPassword() throws Exception
	{
		startParsing();
		writeToPipe("GET /abc?something HTTP/1.1\r\n");
		writeToPipe("Authorization: Basic " + Base64.encode("Aladin") + "\r\n");
		writeToPipe("\r\n");
		finishParsing();
		try
		{
			request.getCredentials();
		}
		catch(Exception e)
		{
			fail("Exception: " + e.getMessage());
		}
	}

	public void testGetUserpass() throws Exception
	{
		assertEquals("Aladdin:open sesame", request.getUserpass("Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ=="));
	}

	public void testUnicodeCharacters() throws Exception
	{
		startParsing();
		writeToPipe("GET /?key=%EB%AA%80%EB%AA%81%EB%AA%82%EB%AA%83 HTTP/1.1\r\n\r\n");
		finishParsing();
		assertEquals("\uba80\uba81\uba82\uba83", request.getInput("key"));
	}

	public void testParsingProgress() throws Exception
	{
		startParsing();
		writeToPipe("GET /something HTTP/1.1\r\n");
		output.flush();
		Thread.sleep(20);
		assertEquals(25, request.numberOfBytesParsed());
		writeToPipe("Content-Length: 23\r\n");
		output.flush();
		Thread.sleep(20);
		assertEquals(45, request.numberOfBytesParsed());
		writeToPipe("\r\n");
		writeToPipe("This is ");
		output.flush();
		Thread.sleep(20);
		assertEquals(55, request.numberOfBytesParsed());
		writeToPipe("the Entity Body");
		output.flush();
		Thread.sleep(20);
		assertEquals(70, request.numberOfBytesParsed());
		finishParsing();
	}

	private void startParsing()
	{
		parseThread = new Thread()
		{
			public synchronized void run()
			{
				try
				{
					request.parse();
				}
				catch(Exception e)
				{
					exception = e;
				}
			}
		};
		parseThread.start();
	}

	private void finishParsing() throws Exception
	{
		parseThread.join();
	}
}