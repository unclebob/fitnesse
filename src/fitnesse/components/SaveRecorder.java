// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.components;

import fitnesse.wiki.*;
import fitnesse.responders.editing.EditResponder;
import java.util.Random;

public class SaveRecorder
{
	public static Random ticketNumGen = new Random();

	public static long pageSaved(PageData data) throws Exception
	{
		long id = newIdNumber();
		data.setAttribute(EditResponder.SAVE_ID, id + "");
		return id;
	}

	public static boolean changesShouldBeMerged(long time, long ticket, PageData data) throws Exception
	{
		boolean returnValue = false;
		String ticketAttrib = data.getAttribute(EditResponder.TICKET_ID);
		String saveAttrib = data.getAttribute(EditResponder.SAVE_ID);

		if(saveAttrib != null)
		{
			long pageSaveId = Long.parseLong(saveAttrib);
			if(pageSaveId > time)
			{
				returnValue = true;
			}
		}

		if(ticketAttrib != null)
		{
			long pageTicketId = Long.parseLong(ticketAttrib);
			if(pageTicketId == ticket)
			{
				returnValue = false;
			}
		}
		return returnValue;
	}

	public static long newIdNumber()
	{
		return System.currentTimeMillis();
	}

	public static long newTicket()
	{
		return ticketNumGen.nextLong();
	}
}
