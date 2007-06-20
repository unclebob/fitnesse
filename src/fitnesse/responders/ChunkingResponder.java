// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders;

import fitnesse.*;
import fitnesse.http.*;
import fitnesse.wiki.*;

import java.net.SocketException;

public abstract class ChunkingResponder implements Responder
{
	protected WikiPage root;
	public WikiPage page;
	protected WikiPagePath path;
	protected Request request;
	protected ChunkedResponse response;
	protected FitNesseContext context;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		this.context = context;
		this.request = request;
		this.root = context.root;
		response = new ChunkedResponse();

		getRequestedPage(request);
		if(page == null && shouldRespondWith404())
			return pageNotFoundResponse(context, request);

		Thread respondingThread = new Thread(new RespondingRunnable(), getClass() + ": Responding Thread");
		respondingThread.start();

		return response;
	}

	private void getRequestedPage(Request request) throws Exception
	{
		path = PathParser.parse(request.getResource());
		page = getPageCrawler().getPage(root, path);
	}

	protected PageCrawler getPageCrawler()
	{
		return root.getPageCrawler();
	}

	private Response pageNotFoundResponse(FitNesseContext context, Request request) throws Exception
	{
		return new NotFoundResponder().makeResponse(context, request);
	}

	protected boolean shouldRespondWith404()
	{
		return true;
	}

	private void startSending()
	{
		try
		{
			doSending();
		}
		catch(SocketException e)
		{
			// normal. someone stoped the request.
		}
		catch(Exception e)
		{
			try
			{
				response.add(ErrorResponder.makeExceptionString(e));
				response.closeAll();
			}
			catch(Exception e1)
			{
				//Give me a break!
			}
		}
	}

	protected String getRenderedPath()
	{
		if(path != null)
			return PathParser.render(path);
		else
			return request.getResource();
	}

	protected class RespondingRunnable implements Runnable
	{
		public void run()
		{
			while(!response.isReadyToSend())
			{
				try
				{
					synchronized(response)
					{
						response.wait();
					}
				}
				catch(InterruptedException e)
				{
					//ok
				}
			}
			startSending();
		}
	}

	public void setRequest(Request request)
	{
		this.request = request;
	}

	protected abstract void doSending() throws Exception;
}
