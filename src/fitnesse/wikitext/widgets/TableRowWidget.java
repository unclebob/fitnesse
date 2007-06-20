// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import java.util.regex.*;

public class TableRowWidget extends ParentWidget
{
	private static final Pattern pattern = Pattern.compile("\\|([^\\|\n\r]*)");
	private TableWidget parentTable;

	private boolean isLiteral;

	public TableRowWidget(TableWidget parentTable, String text, boolean isLiteral) throws Exception
	{
		super(parentTable);
		this.parentTable = parentTable;
		this.isLiteral = isLiteral;
		addCells(text);
	}

	public int getColumns()
	{
		return numberOfChildren();
	}

	public TableWidget getParentTable()
	{
		return parentTable;
	}

	public String render() throws Exception
	{
		StringBuffer html = new StringBuffer("<tr>");
		html.append(childHtml()).append("</tr>\n");
		return html.toString();
	}

	private void addCells(String text) throws Exception
	{
		Matcher match = pattern.matcher(text);
		if(match.find())
		{
			new TableCellWidget(this, match.group(1), isLiteral);
			addCells(text.substring(match.end()));
		}
	}
}

