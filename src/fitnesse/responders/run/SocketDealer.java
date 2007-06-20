// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import java.util.*;

public class SocketDealer
{
	private Map waitingList = new HashMap(17);
	private int ticketCounter = 1;

	public int seekingSocket(SocketSeeker seeker)
	{
		int ticket = ticketCounter++;
		waitingList.put(new Integer(ticket), seeker);
		return ticket;
	}

	public Collection getWaitingList()
	{
		return waitingList.values();
	}

	public void dealSocketTo(int ticket, SocketDoner doner) throws Exception
	{
		Integer key = new Integer(ticket);
		SocketSeeker seeker = (SocketSeeker) waitingList.get(key);
		seeker.acceptSocketFrom(doner);
		waitingList.remove(key);
	}

	public boolean isWaiting(int ticket)
	{
		return waitingList.containsKey(new Integer(ticket));
	}
}
