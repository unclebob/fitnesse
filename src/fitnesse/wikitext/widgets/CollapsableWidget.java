package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.html.RawHtml;

import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CollapsableWidget extends ParentWidget
{
	private static final String ENDL = LineBreakWidget.REGEXP;
	public static final String REGEXP = "!\\*+>? .*?" + ENDL + ".*?" + ENDL + "\\*+!" + ENDL + "?";
	private static final Pattern pattern = Pattern.compile("!\\*+(>)? (.*?)" + ENDL + "(.*?)" + ENDL + "\\*+!", Pattern.MULTILINE + Pattern.DOTALL);

	private static Random random = new Random();

	private String cssClass = "collapse_rim";
	private ParentWidget titleWidget;
	public boolean expanded = true;

	private static final String collapsableOpenCss = "collapsable";
	private static final String collapsableClosedCss = "hidden";
	private static final String collapsableOpenImg = "/files/images/collapsableOpen.gif";
	private static final String collapsableClosedImg = "/files/images/collapsableClosed.gif";

	public CollapsableWidget(ParentWidget parent)
	{
		super(parent);
	}

	public CollapsableWidget(ParentWidget parent, String text) throws Exception
	{
		this(parent);
		Matcher match = pattern.matcher(text);
		match.find();
		expanded = match.group(1) == null;
		String title = match.group(2);
		String body = match.group(3);
		init(title, body);
	}

	public CollapsableWidget(ParentWidget parent, String title, String body, String cssClass) throws Exception
	{
		this(parent);
		init(title, body);
		this.cssClass = cssClass;
	}

	private void init(String title, String body) throws Exception
	{
		titleWidget = new BlankParentWidget(this, "!meta " + title);
		addChildWidgets(body);
	}

	public String render() throws Exception
	{
		HtmlElement titleElement = new RawHtml("&nbsp;" + titleWidget.childHtml());
		HtmlElement bodyElement = new RawHtml(childHtml());
		HtmlElement html = makeCollapsableSection(titleElement, bodyElement);
		return html.html();
	}

	public HtmlTag makeCollapsableSection(HtmlElement title, HtmlElement content)
	{
		String id = random.nextLong() + "";
		HtmlTag outerDiv = HtmlUtil.makeDivTag(cssClass);

		HtmlTag image = new HtmlTag("img");
		image.addAttribute("src", imageSrc());
		image.addAttribute("class", "left");
		image.addAttribute("id", "img" + id);
		HtmlTag anchor = new HtmlTag("a", image);
		anchor.addAttribute("href", "javascript:toggleCollapsable('" + id + "');");
		outerDiv.add(anchor);
		outerDiv.add(title);

		HtmlTag collapsablediv = makeCollapsableDiv();
		collapsablediv.addAttribute("id", id);
		collapsablediv.add(content);
		outerDiv.add(collapsablediv);

		return outerDiv;
	}

	private HtmlTag makeCollapsableDiv()
	{
		if(!expanded)
			return HtmlUtil.makeDivTag(collapsableClosedCss);
		else
			return HtmlUtil.makeDivTag(collapsableOpenCss);
	}

	private String imageSrc()
	{
		if(expanded)
			return collapsableOpenImg;
		else
			return collapsableClosedImg;
	}
}
