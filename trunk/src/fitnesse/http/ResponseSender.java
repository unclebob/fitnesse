// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.http;

import java.net.Socket;

public interface ResponseSender
{
	public void send(byte[] bytes) throws Exception;

	public void close() throws Exception;

	public Socket getSocket() throws Exception; //TODO-MdM maybe get rid of this method.
}
