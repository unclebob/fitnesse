package fitnesse.wikitext;

public class EchoWidgetInterceptor implements WidgetInterceptor
{
	public void intercept(WikiWidget widget) throws Exception
	{
		System.out.println(widget.getClass() + ": " + widget.asWikiText() + " -> " + widget.render());
	}
}
