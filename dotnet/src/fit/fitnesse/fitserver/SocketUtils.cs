using System.Net.Sockets;
using System.Text;

namespace fitnesse.fitserver
{
	public class SocketUtils
	{
		public static string ReceiveStringOfLength(ISocketWrapper socketWrapper, int stringLength)
		{
			byte[] stringBytes = new byte[stringLength];
			char[] stringCharacters = new char[stringLength];
			int received = 0;
			SocketFlags flags = new SocketFlags();
			while ((received = socketWrapper.Receive(stringBytes, received, stringLength - received, flags) + received) < stringLength)
			{
			}
			Encoding.ASCII.GetDecoder().GetChars(stringBytes, 0, stringLength, stringCharacters, 0);
			return new StringBuilder(stringLength).Append(stringCharacters).ToString();
		}
	}
}
