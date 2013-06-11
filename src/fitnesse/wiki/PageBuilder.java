package fitnesse.wiki;

import java.util.List;

/**
 * Nice thing for building page paths.
 */
public class PageBuilder {

  public WikiPage addPage(WikiPage context, WikiPagePath path, String content) {
    WikiPage page = addPage(context, path);
    if (page != null) {
      PageData data = new PageData(page);
      data.setContent(content);
      page.commit(data);
    }
    return page;
  }

  public WikiPage addPage(WikiPage context, WikiPagePath path) {
    return getOrMakePage(context, path.getNames());
  }

  private WikiPage getOrMakePage(WikiPage context, List<?> namePieces) {
    String first = (String) namePieces.get(0);
    List<?> rest = namePieces.subList(1, namePieces.size());
    WikiPage current;
    if (context.getChildPage(first) == null)
      current = context.addChildPage(first);
    else
      current = context.getChildPage(first);
    if (rest.size() == 0)
      return current;
    return getOrMakePage(current, rest);
  }
}
