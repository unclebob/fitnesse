// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.run;

import fitnesse.http.ResponseSender;

public interface ResponsePuppeteer
{
	void readyToSend(ResponseSender sender) throws Exception;
}
