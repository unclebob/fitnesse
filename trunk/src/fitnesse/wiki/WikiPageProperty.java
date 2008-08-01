package fitnesse.wiki;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

public class WikiPageProperty implements Serializable
{
	private String value;
	protected HashMap<String, WikiPageProperty> children;

	public WikiPageProperty()
	{
	}

	public WikiPageProperty(String value)
	{
		this.value = value;
	}

	public String getValue()
	{
		return value;
	}

	public void setValue(String value)
	{
		this.value = value;
	}

	public void set(String name, WikiPageProperty child)
	{
		if(children == null)
			children = new HashMap<String, WikiPageProperty>();
		children.put(name, child);
	}

	public WikiPageProperty set(String name, String value)
	{
		WikiPageProperty child = new WikiPageProperty(value);
		set(name, child);
		return child;
	}

	public WikiPageProperty set(String name)
	{
		return set(name, (String) null);
	}

	public void remove(String name)
	{
		children.remove(name);
	}

	public WikiPageProperty getProperty(String name)
	{
		if(children == null)
			return null;
		else
			return children.get(name);
	}

	public String get(String name)
	{
		WikiPageProperty child = getProperty(name);
		return child == null ? null : child.getValue();
	}

	public boolean has(String name)
	{
		return children != null && children.containsKey(name);
	}

	public Set keySet()
	{
		return children == null ? new HashSet() : children.keySet();
	}

	public String toString()
	{
		return toString("WikiPageProperty root", 0);
	}

	protected String toString(String key, int depth)
	{
		StringBuffer buffer = new StringBuffer();

		for(int i = 0; i < depth; i++)
			buffer.append("\t");
		buffer.append(key);
		if(value != null)
			buffer.append(" = ").append(value);
		buffer.append("\n");

		for(Iterator iterator = keySet().iterator(); iterator.hasNext();)
		{
			String childKey = (String) iterator.next();
			WikiPageProperty value = getProperty(childKey);
			if(value != null)
				buffer.append(value.toString(childKey, depth + 1));
		}
		return buffer.toString();
	}

	public boolean hasChildren()
	{
		return children != null && children.size() > 0;
	}

	public static SimpleDateFormat getTimeFormat()
	{
		//SimpleDateFormat is not thread safe, so we need to create each instance independently.
		return new SimpleDateFormat("yyyyMMddHHmmss");
	}
}
