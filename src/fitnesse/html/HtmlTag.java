// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.html;

import java.util.*;

public class HtmlTag extends HtmlElement
{
	public LinkedList childTags = new LinkedList();
	protected List attributes = new LinkedList();
	protected String tagName = "youreIt";
	public String tail;
	public String head;

	public HtmlTag(String tagName)
	{
		this.tagName = tagName;
	}

	public HtmlTag(String tagName, String content)
	{
		this(tagName);
		add(content);
	}

	public HtmlTag(String tagName, HtmlElement child)
	{
		this(tagName);
		add(child);
	}

	public String tagName()
	{
		return tagName;
	}

	public String html() throws Exception
	{
		return html(0);
	}

	public String html(int depth) throws Exception
	{
		StringBuffer buffer = new StringBuffer();
		addTabs(depth, buffer);

		if(head != null)
			buffer.append(head);

		buffer.append("<").append(tagName());
		addAttributes(buffer);

		if(hasChildren())
		{
	  	buffer.append(">");
			boolean tagWasAdded = addChildHtml(buffer, depth);
			if(tagWasAdded)
				addTabs(depth, buffer);
			buffer.append("</").append(tagName()).append(">");
		}
		else
			buffer.append("/>");

		if(tail != null)
			buffer.append(tail);

		buffer.append(endl);

		return buffer.toString();
	}

	private void addAttributes(StringBuffer buffer)
	{
		for(Iterator iterator = attributes.iterator(); iterator.hasNext();)
		{
			Attribute attribute = (Attribute) iterator.next();
			buffer.append(" ").append(attribute.name).append("=\"").append(attribute.value).append("\"");
		}
	}

	protected void addTabs(int depth, StringBuffer buffer)
	{
		for(int i = 0; i < depth; i++)
			buffer.append('\t');
	}

	private boolean addChildHtml(StringBuffer buffer, int depth)throws Exception
	{
		boolean addedTag = false;
		boolean lastAddedWasNonTag = false;
		int i = 0;
		for(Iterator iterator = childTags.iterator(); iterator.hasNext(); i++)
		{
			HtmlElement element = (HtmlElement) iterator.next();
			if(element instanceof HtmlTag)
			{
				if(i == 0 || lastAddedWasNonTag)
						buffer.append(endl);
				buffer.append(((HtmlTag)element).html(depth + 1));
				addedTag = true;
				lastAddedWasNonTag = false;
			}
			else
			{
				buffer.append(element.html());
				lastAddedWasNonTag = true;
			}
		}

		return addedTag;
	}

	private boolean hasChildren()
	{
		return childTags.size() > 0;
	}

	public void add(String s)
	{
		add(new RawHtml(s));
	}

	public void add(HtmlElement element)
	{
		childTags.add(element);
	}

	public void addAttribute(String key, String value)
	{
		attributes.add(new Attribute(key, value));
	}

	public void use(String s)
	{
		use(new RawHtml(s));
	}

	public void use(HtmlElement element)
	{
		childTags.clear();
		add(element);
	}

	public String getAttribute(String key)
	{
		for(Iterator iterator = attributes.iterator(); iterator.hasNext();)
		{
			Attribute attribute = (Attribute) iterator.next();
			if(key != null && key.equals(attribute.name))
				return attribute.value;
		}
		return null;
	}

	public static class Attribute
	{
		public String name;
		public String value;

		public Attribute(String name, String value)
		{
			this.name = name;
			this.value = value;
		}
	}
}
