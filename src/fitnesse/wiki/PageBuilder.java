package fitnesse.wiki;

/**
 * Nice thing for building page paths.
 */
public interface PageBuilder {
  WikiPage addPage(WikiPage context, WikiPagePath path, String content);

  WikiPage addPage(WikiPage context, WikiPagePath path);
}
