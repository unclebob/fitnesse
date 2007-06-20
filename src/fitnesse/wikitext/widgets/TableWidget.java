// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import java.util.Iterator;
import java.util.regex.*;

public class TableWidget extends ParentWidget
{
	public static final String LF = LineBreakWidget.REGEXP;
	public static final String REGEXP = "^!?(?:\\|[^\r\n]*?\\|" + LF + ")+";
	private static final Pattern pattern = Pattern.compile("(!?)(\\|[^\r\n]*?)\\|" + LF);

	public boolean isTestTable;
	private int columns = 0;

	public int getColumns()
	{
		return columns;
	}

	public TableWidget(ParentWidget parent, String text) throws Exception
	{
		super(parent);
		Matcher match = pattern.matcher(text);
		if(match.find())
		{
			isTestTable = "!".equals(match.group(1));
			addRows(text);
			getMaxNumberOfColumns();
		}
		else
			; // throw Exception?
	}

	private void getMaxNumberOfColumns()
	{
		for(Iterator i = children.iterator(); i.hasNext();)
		{
			TableRowWidget rowWidget = (TableRowWidget) i.next();
			columns = Math.max(columns, rowWidget.getColumns());
		}
	}

	public String render() throws Exception
	{
		StringBuffer html = new StringBuffer("<table border=\"1\" cellspacing=\"0\">\n");
		html.append(childHtml()).append("</table>\n");

		return html.toString();
	}

	private void addRows(String text) throws Exception
	{
		Matcher match = pattern.matcher(text);
		if(match.find())
		{
			new TableRowWidget(this, match.group(2), isTestTable);
			addRows(text.substring(match.end()));
		}
	}
}
