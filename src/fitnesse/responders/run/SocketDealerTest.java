// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import java.net.Socket;
import java.util.Collection;

import junit.framework.TestCase;
import fitnesse.testutil.MockSocket;
import fitnesse.testutil.SimpleSocketSeeker;

public class SocketDealerTest extends TestCase {
  private SocketDealer dealer;
  private SimpleSocketSeeker seeker;
  private int ticket;
  private SimpleDoner doner;

  public void setUp() throws Exception {
    dealer = new SocketDealer();
  }

  public void tearDown() throws Exception {
  }

  public static class SimpleDoner implements SocketDoner {
    public MockSocket socket = new MockSocket("");
    boolean finished = false;

    public Socket donateSocket() {
      return socket;
    }

    public void finishedWithSocket() {
      finished = true;
    }
  }

  public void testAddSeeker() throws Exception {
    SocketSeeker seeker = new SimpleSocketSeeker();
    dealer.seekingSocket(seeker);

    Collection<SocketSeeker> waiting = dealer.getWaitingList();
    assertEquals(1, waiting.size());
    assertTrue(waiting.contains(seeker));
  }

  public void testUniqueTicketNumber() throws Exception {
    int ticketNumber1 = dealer.seekingSocket(new SimpleSocketSeeker());
    int ticketNumber2 = dealer.seekingSocket(new SimpleSocketSeeker());
    assertTrue(ticketNumber1 != ticketNumber2);
  }

  public void testDealSocketTo() throws Exception {
    doSimpleDealing();
    assertSame(doner.socket, seeker.socket);
  }

  private void doSimpleDealing() throws Exception {
    seeker = new SimpleSocketSeeker();
    ticket = dealer.seekingSocket(seeker);
    doner = new SimpleDoner();
    dealer.dealSocketTo(ticket, doner);
  }

  public void testDealSocketToMultipleSeekers() throws Exception {
    SimpleSocketSeeker seeker1 = new SimpleSocketSeeker();
    SimpleSocketSeeker seeker2 = new SimpleSocketSeeker();
    int ticket1 = dealer.seekingSocket(seeker1);
    int ticket2 = dealer.seekingSocket(seeker2);
    SimpleDoner doner1 = new SimpleDoner();
    SimpleDoner doner2 = new SimpleDoner();
    dealer.dealSocketTo(ticket1, doner1);
    dealer.dealSocketTo(ticket2, doner2);

    assertSame(doner1.socket, seeker1.socket);
    assertSame(doner2.socket, seeker2.socket);
  }

  public void testSeekerRemovedAfterDeltTo() throws Exception {
    doSimpleDealing();
    Collection<SocketSeeker> waiting = dealer.getWaitingList();
    assertEquals(0, waiting.size());
  }

  public void testSeekerIsWaiting() throws Exception {
    assertFalse(dealer.isWaiting(23));
    int ticket = dealer.seekingSocket(new SimpleSocketSeeker());
    assertTrue(dealer.isWaiting(ticket));
  }
}
