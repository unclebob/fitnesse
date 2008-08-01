// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import java.net.Socket;

public interface SocketDoner
{
	public Socket donateSocket();

	public void finishedWithSocket() throws Exception;
}
