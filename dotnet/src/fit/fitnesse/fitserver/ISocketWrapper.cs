// Modified or written by Object Mentor, Inc. for inclusion with FitNesse.
// Copyright (c) 2002 Cunningham & Cunningham, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
using System.Net.Sockets;

namespace fitnesse.fitserver
{
	public interface ISocketWrapper
	{
		/**
		 * Allows us to test methods against a MockSocketWrapper - would not be necessary
		 * if .NET used interfaces for things like sockets and connections, etc. 
		 */
		int Receive(byte[] buffer, int offset, int size, SocketFlags flags);
	}
}
