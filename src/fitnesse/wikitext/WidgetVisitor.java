// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext;

import fitnesse.wikitext.widgets.*;

public interface WidgetVisitor
{
	public void visit(WikiWidget widget) throws Exception;

	public void visit(WikiWordWidget widget) throws Exception;

	public void visit(AliasLinkWidget widget) throws Exception;
}
