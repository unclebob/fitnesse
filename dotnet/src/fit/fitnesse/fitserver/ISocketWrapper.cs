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
