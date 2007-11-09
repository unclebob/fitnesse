// Derived from copyrighted code by Object Mentor, Inc.
// Released under the terms of the GNU General Public License version 2 or later.
//[acd] EvaluatorWidget: Created using VariableWidget & Expression
package fitnesse.wikitext.widgets;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import fitnesse.html.HtmlUtil;
import fitnesse.util.Expression;

public class EvaluatorWidget extends ParentWidget
{
   public static final String REGEXP = "\\$\\{=[^=]*=\\}";
   public static final Pattern pattern = Pattern.compile("\\$\\{=([^=]*)=\\}", Pattern.MULTILINE + Pattern.DOTALL);
   public static final Pattern formatParser = Pattern.compile("^[ \\t]*([^:]+:)?[ \\t]*(.*)$");
	private String name = null;
   private String formatSpec = null;
	private String renderedText;
	private boolean rendered;

	public EvaluatorWidget(ParentWidget parent, String text)
	{
		super(parent);
		name = text;
		Matcher match = pattern.matcher(text);
		if (match.find()) name = match.group(1);
	}

	public String render() throws Exception
	{
		if(!rendered) doRender();
		return renderedText;
	}

	private void doRender() throws Exception
	{
		addChildWidgets(name.trim());
		parseOutFormat(childHtml());
		
		if (renderedText.length() > 0)
		{
	      try { evaluateAndFormat(); }
	      catch (Exception e)
	      { 
	         renderedText = makeInvalidVariableExpression(name);
	      }
		}
		rendered = true;
	}

	private void parseOutFormat (String expr)
	{
		Matcher match = formatParser.matcher(expr);
		if (match.find())
      {
         formatSpec   = (match.group(1) == null)? null : match.group(1).trim();
         renderedText = (match.group(2) == null)? ""   : match.group(2).trim();
      }
		else
		{
			formatSpec = null;
			renderedText = expr.trim();
		}
	}
	
	private void evaluateAndFormat () throws Exception
	{
      Double result = (new Expression(renderedText)).evaluate();
      Long iResult = new Long(Math.round(result));
      
      if (formatSpec == null)
         renderedText = (result.equals(iResult.doubleValue()))? iResult.toString() : result.toString();
      else
      {
         if (formatSpec.length() == 2) //...must a letter + ':'
            formatSpec = "%" + formatSpec.charAt(0) ;
         else  ///...take "as is" less the ':'
            formatSpec = formatSpec.substring(0, formatSpec.length() - 1);
         
         char conversion = formatSpec.charAt(formatSpec.length() - 1);
         if ("doOxX".indexOf(conversion) >= 0) //...use the integer
            renderedText = String.format(formatSpec, iResult);
         else //...use the double
            renderedText = String.format(formatSpec, result);
      }
	}
	
   private String makeInvalidVariableExpression(String name) throws Exception
   {
      return HtmlUtil.metaText("invalid expression: " + name);
   }

	public String asWikiText() throws Exception
	{
		return "${=" + name + "=}";
	}
}


