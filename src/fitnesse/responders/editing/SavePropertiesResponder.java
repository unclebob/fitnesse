// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.

package fitnesse.responders.editing;

import fitnesse.*;
import fitnesse.authentication.*;
import fitnesse.responders.*;
import fitnesse.components.*;
import fitnesse.wiki.*;
import fitnesse.http.*;
import java.util.*;

public class SavePropertiesResponder implements SecureResponder
{
	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		SimpleResponse response = new SimpleResponse();
		String resource = request.getResource();
    WikiPagePath path = PathParser.parse(resource);
    WikiPage page = context.root.getPageCrawler().getPage(context.root, path);
		if(page == null)
			return new NotFoundResponder().makeResponse(context, request);
		PageData data = page.getData();
		saveAttributes(request, data);
		VersionInfo commitRecord = page.commit(data);
		response.addHeader("Previous-Version", commitRecord.getName());
		RecentChanges.updateRecentChanges(data);
		response.redirect(resource);

		return response;
	}

	private void saveAttributes(Request request, PageData data) throws Exception
	{
    List attrs = new LinkedList();
    attrs.addAll(Arrays.asList(WikiPage.STANDARD_ATTRIBUTES));
    attrs.addAll(Arrays.asList(WikiPage.SECURITY_ATTRIBUTES));

    for(Iterator i = attrs.iterator(); i.hasNext();)
    {
      String attribute = (String) i.next();
      if (isChecked(request, attribute))
        data.setAttribute(attribute);
      else
        data.removeAttribute(attribute);
    }

		String value = (String)request.getInput(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE);
		if(!value.equals(data.getAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE)))
		{
			WikiPage page = data.getWikiPage();
			if(page.hasExtension(VirtualCouplingExtension.NAME))
			{
				VirtualCouplingExtension extension = (VirtualCouplingExtension)page.getExtension(VirtualCouplingExtension.NAME);
				extension.resetVirtualCoupling();
			}
		}
		if("".equals(value) || value == null)
			data.removeAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE);
		else
			data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, value);
	}

	private boolean isChecked(Request request, String name)
	{
		return (request.getInput(name) != null);
	}

	public SecureOperation getSecureOperation()
	{
		return new AlwaysSecureOperation();
	}
}

