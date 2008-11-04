// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WidgetBuilder;

import java.util.regex.Matcher;
import java.util.List;  //Parent Literals

public class TextIgnoringWidgetRoot extends WidgetRoot
{
   //Refactored for isGathering parameter.
	public TextIgnoringWidgetRoot(String value, WikiPage page, WidgetBuilder builder) throws Exception
	{
		super(value, page, builder, /*isGatheringInfo=*/ true);
	}

   //Parent Literals: T'I'W'Root ctor with parent's literals
   public TextIgnoringWidgetRoot(String value, WikiPage page, List<String> literals, WidgetBuilder builder) throws Exception
   {
      super(null, page, builder, /*isGatheringInfo=*/ true);
      if (literals != null)  this.setLiterals(literals);
      this.buildWidgets(value);
   }


	public void addChildWidgets(String value) throws Exception
	{
		Matcher m = getBuilder().getWidgetPattern().matcher(value);
		if(m.find())
		{
			getBuilder().makeWidget(this, m);
			String postString = value.substring(m.end(), value.length());
			if(!postString.equals(""))
				addChildWidgets(postString);
		}
	}
}

