// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using NUnit.Framework;
using System.IO;
using System.Text;

namespace fit 
{
	[TestFixture]
	public class FitServerTest : Assertion
	{
		private FitServer fitServer;

		private static string ReadStreamContent(Stream readFrom)
		{
			long OldPosition = readFrom.Position;
			readFrom.Position = 0;

			StreamReader Reader = new StreamReader(readFrom);
			int StreamContentLength = (int) readFrom.Length;
			char[] StreamContent = new char[StreamContentLength];

			Reader.Read(StreamContent, 0, StreamContentLength);
			readFrom.Position = OldPosition;

			return new StringBuilder().Append(StreamContent).ToString();
		}


		private static void WriteToStream(string thisContent, Stream writeTo) 
		{
			long OldPosition = writeTo.Position;

			StreamWriter Writer = new StreamWriter(writeTo);
			Writer.Write(thisContent); Writer.Flush();

			writeTo.Position = OldPosition;
		}

		[SetUp]
		public void Initialize() 
		{
			fitServer = new FitServer();
		}

		[TearDown]
		public void ClearUp()
		{
		}

		[Test]
		public void ReadIntegerFrom() 
		{
			MemoryStream Stream = new MemoryStream();
			StreamWriter Writer = new StreamWriter(Stream);
			Writer.Write("0000001234Restofthestring");
			Writer.Flush();
			Stream.Position = 0;
			StreamReader Reader = new StreamReader(Stream);
			AssertEquals(1234, fitServer.ReadIntegerFrom(Reader));
		}


		[Test]
		public void WriteIntegerToIntegerString()
		{
			AssertEquals("0000000009", FitServer.EncodeInteger(9));
			AssertEquals("0000000123", FitServer.EncodeInteger(123));
			AssertEquals("0000004444", FitServer.EncodeInteger(4444));
		}


		[Test]
		public void ReadStreamContent()
		{
			string Expected = "Hello 123 !!";

			MemoryStream Stream = new MemoryStream();
			StreamWriter Writer = new StreamWriter(Stream);
			Writer.Write(Expected); Writer.Flush();

			string Actual = ReadStreamContent(Stream);
			AssertEquals(Expected, Actual);
		}


		[Test]
		public void TestWriteToStream()
		{
			string Content = "This is content of length 28";
			string Expected = "0000000028" + Content;

			MemoryStream Stream = new MemoryStream();
			StreamWriter Writer = new StreamWriter(Stream);
			fitServer.WriteTo(Writer, Content);

			string Actual = ReadStreamContent(Stream);
			AssertEquals(Expected, Actual);
		}


		[Test]
		public void TestReadFromStream()
		{
			string Expected = "This is content of length 28";
			string Content = "0000000028" + Expected;

			MemoryStream Stream = new MemoryStream();
			WriteToStream(Content, Stream);
			StreamReader Reader = new StreamReader(Stream);

			string Actual = fitServer.ReadFrom(Reader);
			AssertEquals(Expected, Actual);
		}


		[Test]
		public void ToTransmissionDocument() 
		{
			string Content = "Here is some Text of Length 30";
			string Expected = "0000000030" + Content;

			string Actual = FitServer.EncodeString(Content);

            		AssertEquals(Expected, Actual);
		}
	}
}
