// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System;
using System.Net.Sockets;
using NUnit.Framework;

namespace fitnesse.fitserver
{
	[TestFixture]
	public class SocketUtilsTest
	{
		[Test]
		public void TestReceiveStringOfLength()
		{
			string[] strings = new string[] {"just one"};
			MockSocketWrapper wrapper = new MockSocketWrapper(strings);
			Assert.AreEqual(strings[0], SocketUtils.ReceiveStringOfLength(wrapper, strings[0].Length));
		}

		[Test]
		public void TestReceiveStringOfLengthWithMoreThanOneRead()
		{
			string[] strings = new string[] {"the first", "the second"};
			MockSocketWrapper wrapper = new MockSocketWrapper(strings);
			Assert.AreEqual(strings[0] + strings[1], SocketUtils.ReceiveStringOfLength(wrapper, strings[0].Length + strings[1].Length));
		}

	}

	public class MockSocketWrapper : ISocketWrapper
	{
		private string[] strings;
		private int currentString = 0;

		public MockSocketWrapper(string[] strings)
		{
			this.strings = strings;
		}

		public int Receive(byte[] buffer, int offset, int size, SocketFlags flags)
		{
			int currentIndex = offset;
			char[] chars = strings[currentString++].ToCharArray();
			foreach (char c in chars)
			{
				buffer[currentIndex++] = Convert.ToByte(c);
			}
			return chars.Length;
		}
	}
}