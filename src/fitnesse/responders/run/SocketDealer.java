// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class SocketDealer {
  private Map<Integer, SocketSeeker> waitingList = new HashMap<Integer, SocketSeeker>(17);
  private int ticketCounter = 1;

  public int seekingSocket(SocketSeeker seeker) {
    int ticket = ticketCounter++;
    waitingList.put(new Integer(ticket), seeker);
    return ticket;
  }

  public Collection<SocketSeeker> getWaitingList() {
    return waitingList.values();
  }

  public void dealSocketTo(int ticket, SocketDoner doner) throws Exception {
    Integer key = new Integer(ticket);
    SocketSeeker seeker = waitingList.get(key);
    seeker.acceptSocketFrom(doner);
    waitingList.remove(key);
  }

  public boolean isWaiting(int ticket) {
    return waitingList.containsKey(new Integer(ticket));
  }
}
