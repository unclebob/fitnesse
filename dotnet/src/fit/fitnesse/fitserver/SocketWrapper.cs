using System.Net.Sockets;

namespace fit
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