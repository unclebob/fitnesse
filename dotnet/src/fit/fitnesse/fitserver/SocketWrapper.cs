// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System.Net.Sockets;

namespace fitnesse.fitserver
{
	public class SocketWrapper : ISocketWrapper
	{
		private Socket socket;

		public SocketWrapper(System.Net.Sockets.Socket socket)
		{
			this.socket = socket;
		}

		public int Receive(byte[] buffer, int offset, int size, SocketFlags flags)
		{
			return socket.Receive(buffer, offset, size, flags);
		}
	}
}