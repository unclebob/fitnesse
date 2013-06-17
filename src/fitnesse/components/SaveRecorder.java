// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import util.Clock;

import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;

public class SaveRecorder {
  public static final Random ticketNumGen = new Random();
  private static final Map<String, Long> ticketRegistry = new HashMap<String, Long>();
  private static final Map<String, Long> saveTime = new HashMap<String, Long>();

  public static long pageSaved(PageData data, long ticketNumber) {
    long timeStamp = timeStamp();
    WikiPage page = data.getWikiPage();
    String name = page.getPageCrawler().getFullPath().toString();
    ticketRegistry.put(name, ticketNumber);
    saveTime.put(name, timeStamp);
    return timeStamp;
  }

  public static boolean changesShouldBeMerged(long thisEditTime, long ticket, PageData data) {
    return new MergeDeterminer(thisEditTime, ticket, data).shouldMerge();
  }

  public static long timeStamp() {
    return Clock.currentTimeInMillis();
  }

  public static long newTicket() {
    return ticketNumGen.nextLong();
  }

  // Called by tests to simulate clean environment.
  public static void clear() {
    ticketRegistry.clear();
    saveTime.clear();
  }

  private static class MergeDeterminer {
    private long thisEditTime;
    private long ticket;
    private WikiPage page;
    private String fullPageName;

    public MergeDeterminer(long thisEditTime, long ticket, PageData data) {
      this.thisEditTime = thisEditTime;
      this.ticket = ticket;
      page = data.getWikiPage();
      fullPageName = page.getPageCrawler().getFullPath().toString();
    }

    public boolean shouldMerge() {
      return isSaveOutOfOrder() && !isSameEditSession();
    }

    private boolean isSameEditSession() {
      boolean sameEdit = false;
      if (ticketRegistry.containsKey(fullPageName)) {
        long pageTicketId = ticketRegistry.get(fullPageName);
        if (pageTicketId == ticket) {
          sameEdit = true;
        }
      }
      return sameEdit;
    }

    private boolean isSaveOutOfOrder() {
      boolean returnValue;
      returnValue = false;
      if (saveTime.containsKey(fullPageName)) {
        long lastSaveTime = saveTime.get(fullPageName);
        if (lastSaveTime > thisEditTime) {
          returnValue = true;
        }
      }
      return returnValue;
    }
  }
}
